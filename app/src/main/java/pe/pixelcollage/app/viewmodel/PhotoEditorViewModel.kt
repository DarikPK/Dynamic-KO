package pe.pixelcollage.app.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

sealed class PhotoEditorUiState {
    object Idle : PhotoEditorUiState()
    object Loading : PhotoEditorUiState()
    data class Success(val bitmap: Bitmap, val isSaved: Boolean = false, val savedUri: Uri? = null) : PhotoEditorUiState()
    data class Error(val message: String) : PhotoEditorUiState()
}

// Representa las transformaciones acumuladas aplicadas a la foto
data class PhotoTransformations(
    val rotationDegrees: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val cropRect: RectF? = null, // Coordenadas normalizadas (0.0f a 1.0f) relativas a la imagen para aplicar a alta resolución
    val brightness: Float = 0f,   // -100f a 100f
    val contrast: Float = 0f,     // -100f a 100f (mapeado a escala)
    val saturation: Float = 0f,   // -100f a 100f
    val temperature: Float = 0f,  // -100f a 100f
    val exposure: Float = 0f      // -100f a 100f
)

class PhotoEditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PhotoEditorUiState>(PhotoEditorUiState.Idle)
    val uiState: StateFlow<PhotoEditorUiState> = _uiState.asStateFlow()

    private val _currentTransformations = MutableStateFlow(PhotoTransformations())
    val currentTransformations: StateFlow<PhotoTransformations> = _currentTransformations.asStateFlow()

    // Historial para Deshacer/Rehacer de transformaciones
    private val undoStack = Stack<PhotoTransformations>()
    private val redoStack = Stack<PhotoTransformations>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var originalImageUri: Uri? = null
    private var previewOriginalBitmap: Bitmap? = null
    private var currentAdjustedBitmap: Bitmap? = null

    // Variable para controlar el estado de procesamiento visual
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun loadPhoto(context: Context, uri: Uri) {
        originalImageUri = uri
        _uiState.value = PhotoEditorUiState.Loading
        undoStack.clear()
        redoStack.clear()
        updateHistoryState()

        viewModelScope.launch {
            try {
                val bitmap = loadSampledBitmap(context, uri, 1200)
                if (bitmap != null) {
                    previewOriginalBitmap = bitmap
                    _currentTransformations.value = PhotoTransformations()
                    applyCurrentTransformations()
                } else {
                    _uiState.value = PhotoEditorUiState.Error("No se pudo decodificar la imagen.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = PhotoEditorUiState.Error(e.localizedMessage ?: "Error al cargar la imagen.")
            }
        }
    }

    private fun loadSampledBitmap(context: Context, uri: Uri, maxDim: Int): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputStream, null, options)

                var srcWidth = options.outWidth
                var srcHeight = options.outHeight

                var inSampleSize = 1
                while (srcWidth / 2 >= maxDim || srcHeight / 2 >= maxDim) {
                    srcWidth /= 2
                    srcHeight /= 2
                    inSampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }

                context.contentResolver.openInputStream(uri)?.use { finalStream ->
                    BitmapFactory.decodeStream(finalStream, null, decodeOptions)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Aplica las transformaciones actuales sobre la imagen base (previewOriginalBitmap) de forma asíncrona y fluida
    private fun applyCurrentTransformations() {
        val original = previewOriginalBitmap ?: return
        viewModelScope.launch(Dispatchers.Default) {
            val transformations = _currentTransformations.value
            val transformed = processBitmap(original, transformations)
            withContext(Dispatchers.Main) {
                currentAdjustedBitmap = transformed
                _uiState.value = PhotoEditorUiState.Success(transformed)
            }
        }
    }

    // Función pura que procesa un bitmap dado con una serie de transformaciones
    private fun processBitmap(src: Bitmap, transform: PhotoTransformations): Bitmap {
        var bitmap = src

        // 1. Aplicar Recorte si existe (coordenadas normalizadas)
        transform.cropRect?.let { rect ->
            val left = (rect.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
            val top = (rect.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
            val right = (rect.right * bitmap.width).toInt().coerceIn(left + 1, bitmap.width)
            val bottom = (rect.bottom * bitmap.height).toInt().coerceIn(top + 1, bitmap.height)
            val width = right - left
            val height = bottom - top
            if (width > 0 && height > 0) {
                bitmap = Bitmap.createBitmap(bitmap, left, top, width, height)
            }
        }

        // 2. Aplicar Rotación y Volteo
        if (transform.rotationDegrees != 0f || transform.flipHorizontal || transform.flipVertical) {
            val matrix = Matrix()
            if (transform.rotationDegrees != 0f) {
                matrix.postRotate(transform.rotationDegrees)
            }
            val scaleX = if (transform.flipHorizontal) -1f else 1f
            val scaleY = if (transform.flipVertical) -1f else 1f
            if (transform.flipHorizontal || transform.flipVertical) {
                matrix.postScale(scaleX, scaleY)
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        // 3. Aplicar Ajustes Básicos (Brillo, Contraste, Saturación, Temperatura, Exposición) vía ColorMatrix
        val hasAdjustments = transform.brightness != 0f || transform.contrast != 0f ||
                transform.saturation != 0f || transform.temperature != 0f || transform.exposure != 0f

        if (hasAdjustments) {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            // Crear ColorMatrix combinada
            val combinedMatrix = ColorMatrix()

            // A. Brillo (-100 a 100) -> Añadido como offset
            val bOffset = transform.brightness
            val brightnessMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, bOffset,
                0f, 1f, 0f, 0f, bOffset,
                0f, 0f, 1f, 0f, bOffset,
                0f, 0f, 0f, 1f, 0f
            ))
            combinedMatrix.postConcat(brightnessMatrix)

            // B. Contraste (-100 a 100) -> Escalado alrededor de un valor medio de 128 (0.5)
            val cVal = if (transform.contrast >= 0) {
                1f + (transform.contrast / 100f) * 1.5f // 1.0 a 2.5
            } else {
                1f + (transform.contrast / 100f) * 0.8f // 0.2 a 1.0
            }
            val translate = 127.5f * (1f - cVal)
            val contrastMatrix = ColorMatrix(floatArrayOf(
                cVal, 0f, 0f, 0f, translate,
                0f, cVal, 0f, 0f, translate,
                0f, 0f, cVal, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
            combinedMatrix.postConcat(contrastMatrix)

            // C. Saturación (-100 a 100) -> 0.0 (escala de grises) a 3.0 (super saturado)
            val sVal = if (transform.saturation >= 0) {
                1f + (transform.saturation / 100f) * 2.0f // 1.0 a 3.0
            } else {
                1f + (transform.saturation / 100f) // 0.0 a 1.0
            }
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(sVal)
            combinedMatrix.postConcat(satMatrix)

            // D. Temperatura (-100 a 100) -> Calentar (aumentar Rojo/Amarillo) o Enfriar (aumentar Azul)
            val tVal = transform.temperature / 100f
            val rScale = 1f + (tVal * 0.15f).coerceAtLeast(0f)
            val gScale = 1f + (tVal * 0.05f).coerceAtLeast(0f)
            val bScale = 1f + (-tVal * 0.15f).coerceAtLeast(0f)
            val tempMatrix = ColorMatrix(floatArrayOf(
                rScale, 0f, 0f, 0f, 0f,
                0f, gScale, 0f, 0f, 0f,
                0f, 0f, bScale, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            combinedMatrix.postConcat(tempMatrix)

            // E. Exposición (-100 a 100) -> Multiplicador directo
            val eVal = if (transform.exposure >= 0) {
                1f + (transform.exposure / 100f) * 1.5f
            } else {
                1f + (transform.exposure / 100f) * 0.7f
            }
            val exposureMatrix = ColorMatrix(floatArrayOf(
                eVal, 0f, 0f, 0f, 0f,
                0f, eVal, 0f, 0f, 0f,
                0f, 0f, eVal, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            combinedMatrix.postConcat(exposureMatrix)

            paint.colorFilter = ColorMatrixColorFilter(combinedMatrix)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            bitmap = output
        }

        return bitmap
    }

    // Guarda el estado actual en el historial de deshacer y limpia rehacer
    fun saveTransformToHistory() {
        val current = _currentTransformations.value
        undoStack.push(current)
        redoStack.clear()
        updateHistoryState()
    }

    private fun updateHistoryState() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val current = _currentTransformations.value
            redoStack.push(current)
            val previous = undoStack.pop()
            _currentTransformations.value = previous
            updateHistoryState()
            applyCurrentTransformations()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val current = _currentTransformations.value
            undoStack.push(current)
            val next = redoStack.pop()
            _currentTransformations.value = next
            updateHistoryState()
            applyCurrentTransformations()
        }
    }

    // Funciones de Transformación Directa (Registran en historial consolidado)
    fun rotate90Degrees(right: Boolean) {
        saveTransformToHistory()
        val change = if (right) 90f else -90f
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(
            rotationDegrees = (current.rotationDegrees + change) % 360f
        )
        applyCurrentTransformations()
    }

    fun flipHorizontal() {
        saveTransformToHistory()
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(
            flipHorizontal = !current.flipHorizontal
        )
        applyCurrentTransformations()
    }

    fun flipVertical() {
        saveTransformToHistory()
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(
            flipVertical = !current.flipVertical
        )
        applyCurrentTransformations()
    }

    fun applyCrop(rect: RectF) {
        saveTransformToHistory()
        val current = _currentTransformations.value
        // Acumular recorte sobre el recorte existente si lo hay
        val baseRect = current.cropRect ?: RectF(0f, 0f, 1f, 1f)
        val newLeft = baseRect.left + rect.left * baseRect.width()
        val newTop = baseRect.top + rect.top * baseRect.height()
        val newRight = baseRect.left + rect.right * baseRect.width()
        val newBottom = baseRect.top + rect.bottom * baseRect.height()

        _currentTransformations.value = current.copy(
            cropRect = RectF(newLeft, newTop, newRight, newBottom)
        )
        applyCurrentTransformations()
    }

    fun updateBrightness(value: Float) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(brightness = value)
        applyCurrentTransformations()
    }

    fun updateContrast(value: Float) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(contrast = value)
        applyCurrentTransformations()
    }

    fun updateSaturation(value: Float) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(saturation = value)
        applyCurrentTransformations()
    }

    fun updateTemperature(value: Float) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(temperature = value)
        applyCurrentTransformations()
    }

    fun updateExposure(value: Float) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(exposure = value)
        applyCurrentTransformations()
    }

    fun resetAdjustments() {
        saveTransformToHistory()
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(
            brightness = 0f,
            contrast = 0f,
            saturation = 0f,
            temperature = 0f,
            exposure = 0f
        )
        applyCurrentTransformations()
    }

    fun resetAll() {
        saveTransformToHistory()
        _currentTransformations.value = PhotoTransformations()
        applyCurrentTransformations()
    }

    // Guarda de forma definitiva la imagen aplicando la cola de transformaciones a alta resolución
    fun savePhoto(context: Context) {
        val uri = originalImageUri ?: return
        _isProcessing.value = true

        viewModelScope.launch {
            try {
                val savedUri = withContext(Dispatchers.IO) {
                    // 1. Cargar el bitmap original de alta resolución
                    val originalHighRes = loadSampledBitmap(context, uri, 3000)
                    if (originalHighRes == null) {
                        null
                    } else {
                        // 2. Procesar las transformaciones en alta resolución
                        val transformations = _currentTransformations.value
                        val resultBitmap = processBitmap(originalHighRes, transformations)

                        // 3. Determinar formato de guardado
                        // Si hay recorte o es transparente, guardamos PNG. Sino, JPG.
                        val hasCrop = transformations.cropRect != null
                        val isPng = hasCrop || context.contentResolver.getType(uri)?.contains("png") == true
                        val mimeType = if (isPng) "image/png" else "image/jpeg"
                        val ext = if (isPng) "png" else "jpg"

                        val format = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                        val quality = 95

                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val fileName = "Pixel_Photos_$timeStamp.$ext"

                        val resolver = context.contentResolver
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PixelCollage")
                            }
                        }

                        val targetUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (targetUri != null) {
                            resolver.openOutputStream(targetUri)?.use { outputStream ->
                                resultBitmap.compress(format, quality, outputStream)
                            }
                            targetUri
                        } else {
                            null
                        }
                    }
                }

                if (savedUri != null) {
                    _uiState.value = PhotoEditorUiState.Success(currentAdjustedBitmap ?: previewOriginalBitmap!!, isSaved = true, savedUri = savedUri)
                } else {
                    _uiState.value = PhotoEditorUiState.Error("No se pudo guardar la imagen procesada.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = PhotoEditorUiState.Error("Error al guardar: ${e.localizedMessage}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
