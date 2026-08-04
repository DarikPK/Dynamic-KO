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
import pe.pixelcollage.app.utils.BackgroundRemovalResult
import pe.pixelcollage.app.utils.MediaPipeInteractiveSegmentationEngine
import pe.pixelcollage.app.utils.SegmentationEngine
import pe.pixelcollage.app.utils.SegmentationStatus
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
    val exposure: Float = 0f,     // -100f a 100f

    // Remoción de Fondo acumulada
    val bgRemovalActive: Boolean = false,
    val bgOption: String = "transparent", // transparent, color, gradient, other_photo, blur_original
    val bgColor: Int = Color.BLACK,
    val bgGradientIndex: Int = 0,
    val bgImageUri: String? = null,
    val blurRadius: Float = 10f,
    val outlineEnabled: Boolean = false,
    val outlineColor: Int = Color.WHITE,
    val outlineSize: Float = 12f,
    val shadowEnabled: Boolean = false,
    val shadowColor: Int = Color.BLACK,
    val shadowRadius: Float = 15f
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

    // --- ESTADOS DE REMOCIÓN DE FONDO ---
    // Motor principal interactivo basado en MediaPipe
    private val removalEngine: SegmentationEngine = MediaPipeInteractiveSegmentationEngine()

    private val _segmentationStatus = MutableStateFlow(SegmentationStatus.NOT_INITIALIZED)
    val segmentationStatus: StateFlow<SegmentationStatus> = _segmentationStatus.asStateFlow()

    // Controla si el usuario está en el flujo de selección táctil interactiva
    private val _isInteractiveSelecting = MutableStateFlow(false)
    val isInteractiveSelecting: StateFlow<Boolean> = _isInteractiveSelecting.asStateFlow()

    // Control de Modos Explícitos: Agregar (true) y Quitar (false)
    private val _isSelectionModeAdd = MutableStateFlow(true)
    val isSelectionModeAdd: StateFlow<Boolean> = _isSelectionModeAdd.asStateFlow()

    // Control de Tolerancia (0 a 100)
    private val _tolerance = MutableStateFlow(50f)
    val tolerance: StateFlow<Float> = _tolerance.asStateFlow()

    private var originalImageUri: Uri? = null
    private var previewOriginalBitmap: Bitmap? = null
    private var currentAdjustedBitmap: Bitmap? = null

    // Máscaras binarias: la de IA corregible, la de IA sin cambios y la acumulada pura táctil (baseCombinedMask)
    private var rawAiMaskBitmap: Bitmap? = null
    private var correctedMaskBitmap: Bitmap? = null
        set(value) {
            field = value
            _currentMask.value = value
        }
    private var baseCombinedMask: Bitmap? = null

    private val _currentMask = MutableStateFlow<Bitmap?>(null)
    val currentMask: StateFlow<Bitmap?> = _currentMask.asStateFlow()

    // Historial independiente para corrección manual de máscara (pinceladas o toques)
    private val maskUndoStack = Stack<Bitmap>()
    private val maskRedoStack = Stack<Bitmap>()

    private val _canUndoMask = MutableStateFlow(false)
    val canUndoMask: StateFlow<Boolean> = _canUndoMask.asStateFlow()

    private val _canRedoMask = MutableStateFlow(false)
    val canRedoMask: StateFlow<Boolean> = _canRedoMask.asStateFlow()

    // Bitmap de la imagen elegida como fondo (otra foto)
    private var selectedBgImageBitmap: Bitmap? = null

    // Variable para controlar el estado de procesamiento visual
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Gradientes predefinidos elegantes locales
    val backgroundGradients = listOf(
        listOf(Color.parseColor("#1B0B2E"), Color.parseColor("#3B1E63")), // Midnight Violet
        listOf(Color.parseColor("#0F2027"), Color.parseColor("#203A43"), Color.parseColor("#2C5364")), // Ocean Breeze
        listOf(Color.parseColor("#F12711"), Color.parseColor("#F5AF19")), // Coral Sunset
        listOf(Color.parseColor("#11998e"), Color.parseColor("#38ef7d")), // Fresh Mint
        listOf(Color.parseColor("#FF416C"), Color.parseColor("#FF4B2B")), // Red Flare
        listOf(Color.parseColor("#EA80FC"), Color.parseColor("#80DEEA")), // Pink Neon
        listOf(Color.parseColor("#141419"), Color.parseColor("#2A2B36")), // Carbon Dark
        listOf(Color.parseColor("#3A6073"), Color.parseColor("#3A6073"))  // Slate Gray
    )

    fun loadPhoto(context: Context, uri: Uri) {
        originalImageUri = uri
        _uiState.value = PhotoEditorUiState.Loading
        undoStack.clear()
        redoStack.clear()
        maskUndoStack.clear()
        maskRedoStack.clear()
        updateHistoryState()
        updateMaskHistoryState()

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

    // Aplica las transformaciones actuales sobre la imagen base de forma asíncrona
    fun applyCurrentTransformations() {
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

    // Función pura que procesa un bitmap con transformaciones y remoción de fondo
    private fun processBitmap(src: Bitmap, transform: PhotoTransformations): Bitmap {
        var bitmap = src

        // 1. Aplicar Remoción de Fondo si está activa
        if (transform.bgRemovalActive && correctedMaskBitmap != null) {
            bitmap = composeCutoutWithBackground(bitmap, correctedMaskBitmap!!, transform)
        } else if (_isInteractiveSelecting.value && correctedMaskBitmap != null) {
            // Durante la selección interactiva táctil: Mostramos el original atenuando ligeramente las áreas no seleccionadas
            bitmap = composeInteractiveSelectionPreview(bitmap, correctedMaskBitmap!!)
        }

        // 2. Aplicar Recorte si existe (coordenadas normalizadas)
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

        // 3. Aplicar Rotación y Volteo
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

        // 4. Aplicar Ajustes Básicos (Brillo, Contraste, Saturación, Temperatura, Exposición) vía ColorMatrix
        val hasAdjustments = transform.brightness != 0f || transform.contrast != 0f ||
                transform.saturation != 0f || transform.temperature != 0f || transform.exposure != 0f

        if (hasAdjustments) {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            // Crear ColorMatrix combinada
            val combinedMatrix = ColorMatrix()

            // A. Brillo (-100 a 100)
            val bOffset = transform.brightness
            val brightnessMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, bOffset,
                0f, 1f, 0f, 0f, bOffset,
                0f, 0f, 1f, 0f, bOffset,
                0f, 0f, 0f, 1f, 0f
            ))
            combinedMatrix.postConcat(brightnessMatrix)

            // B. Contraste (-100 a 100)
            val cVal = if (transform.contrast >= 0) {
                1f + (transform.contrast / 100f) * 1.5f
            } else {
                1f + (transform.contrast / 100f) * 0.8f
            }
            val translate = 127.5f * (1f - cVal)
            val contrastMatrix = ColorMatrix(floatArrayOf(
                cVal, 0f, 0f, 0f, translate,
                0f, cVal, 0f, 0f, translate,
                0f, 0f, cVal, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
            combinedMatrix.postConcat(contrastMatrix)

            // C. Saturación (-100 a 100)
            val sVal = if (transform.saturation >= 0) {
                1f + (transform.saturation / 100f) * 2.0f
            } else {
                1f + (transform.saturation / 100f)
            }
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(sVal)
            combinedMatrix.postConcat(satMatrix)

            // D. Temperatura (-100 a 100)
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

            // E. Exposición (-100 a 100)
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

    // Compone la vista previa interactiva: Imagen original completa con realce de áreas seleccionadas
    private fun composeInteractiveSelectionPreview(src: Bitmap, mask: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Dibujar el original completo
        canvas.drawBitmap(src, 0f, 0f, null)

        // 2. Capa de atenuación semitransparente sobre zonas no seleccionadas (15%-25%)
        val dimLayer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val dimCanvas = Canvas(dimLayer)
        dimCanvas.drawColor(Color.parseColor("#4D000000")) // ~30% de atenuación oscura discreta

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        dimCanvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null

        canvas.drawBitmap(dimLayer, 0f, 0f, null)

        // 3. Superposición interior sutil del 20% violeta premium (#B39DDB) dentro del área seleccionada
        val overlayLayer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val overlayCanvas = Canvas(overlayLayer)
        overlayCanvas.drawColor(Color.parseColor("#33B39DDB")) // 20% de opacidad violeta

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        overlayCanvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null

        canvas.drawBitmap(overlayLayer, 0f, 0f, null)

        return output
    }

    // Realiza la mezcla o composición del sujeto recortado con la máscara y el fondo configurado
    private fun composeCutoutWithBackground(src: Bitmap, mask: Bitmap, transform: PhotoTransformations): Bitmap {
        val width = src.width
        val height = src.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Dibujar el Fondo
        when (transform.bgOption) {
            "transparent" -> {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            }
            "color" -> {
                canvas.drawColor(transform.bgColor)
            }
            "gradient" -> {
                val colors = backgroundGradients.getOrNull(transform.bgGradientIndex) ?: backgroundGradients[0]
                val shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colors.toIntArray(), null, Shader.TileMode.CLAMP)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            "other_photo" -> {
                selectedBgImageBitmap?.let { bgBitmap ->
                    val srcRect = Rect(0, 0, bgBitmap.width, bgBitmap.height)
                    val dstRect = Rect(0, 0, width, height)
                    canvas.drawBitmap(bgBitmap, srcRect, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
                } ?: run {
                    canvas.drawColor(Color.BLACK)
                }
            }
            "blur_original" -> {
                val blurred = blurBitmap(src, transform.blurRadius)
                canvas.drawBitmap(blurred, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
            }
        }

        // 2. Crear sujeto recortado usando la máscara
        val cutout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val cutoutCanvas = Canvas(cutout)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        cutoutCanvas.drawBitmap(src, 0f, 0f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        cutoutCanvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null

        // 3. Dibujar sombra o contorno opcional
        if (transform.outlineEnabled) {
            val outlineMask = mask.copy(mask.config ?: Bitmap.Config.ARGB_8888, true)
            canvas.drawBitmap(outlineMask, 0f, 0f, null)
        }

        if (transform.shadowEnabled) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = PorterDuffColorFilter(transform.shadowColor, PorterDuff.Mode.SRC_IN)
                maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawBitmap(mask, 8f, 12f, shadowPaint)
        }

        // 4. Dibujar sujeto recortado final
        canvas.drawBitmap(cutout, 0f, 0f, null)

        return output
    }

    private fun blurBitmap(src: Bitmap, radius: Float): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            alpha = 180
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    // --- INTEGRACIÓN Y INICIALIZACIÓN DE REMOCIÓN DE FONDO IA ---
    fun prepareSubjectSegmentation(context: Context) {
        _segmentationStatus.value = SegmentationStatus.DOWNLOADING_MODEL
        viewModelScope.launch {
            removalEngine.prepareModel(context)
            _segmentationStatus.value = removalEngine.getStatus()

            // Inicializar la máscara de trabajo de forma completamente transparente (vacía por defecto!)
            val original = previewOriginalBitmap
            if (original != null) {
                val width = original.width
                val height = original.height
                correctedMaskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(Color.TRANSPARENT)
                }
                rawAiMaskBitmap = correctedMaskBitmap?.copy(Bitmap.Config.ARGB_8888, true)
                baseCombinedMask = correctedMaskBitmap?.copy(Bitmap.Config.ARGB_8888, true)

                // Calcular tolerancia inicial adaptativa basada en la resolución real
                val initialTol = calculateAdaptiveInitialTolerance(original)
                _tolerance.value = initialTol
            }

            _isInteractiveSelecting.value = true
            _isSelectionModeAdd.value = true
            _currentTransformations.value = _currentTransformations.value.copy(
                bgRemovalActive = false // No aplicar recorte inmediato de fondo
            )
            applyCurrentTransformations()
        }
    }

    private fun calculateAdaptiveInitialTolerance(bitmap: Bitmap): Float {
        val minDim = Math.min(bitmap.width, bitmap.height)
        val base = if (minDim < 600) 40f else 48f
        return base.coerceIn(35f, 55f)
    }

    // Cambia el modo de selección: Agregar (true) o Quitar (false)
    fun setSelectionMode(add: Boolean) {
        _isSelectionModeAdd.value = add
    }

    // Procesa un toque en coordenadas relativas (0.0f a 1.0f) realizando unión o resta de máscaras según el modo activo
    fun handleInteractiveTouch(context: Context, point: PointF) {
        val original = previewOriginalBitmap ?: return
        val baseMask = baseCombinedMask ?: return
        _isProcessing.value = true

        viewModelScope.launch {
            // Generar una máscara de toque a partir de la segmentación interactiva
            val result = removalEngine.processTouch(context, original, point, null)
            result.onSuccess { touchMask ->
                // Guardar la máscara combinada previa en el historial
                saveMaskToHistory()

                val width = baseMask.width
                val height = baseMask.height
                val newCombined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(newCombined)

                canvas.drawBitmap(baseMask, 0f, 0f, null)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                if (_isSelectionModeAdd.value) {
                    // MODO AGREGAR: Unión de máscaras (SRC_OVER)
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                    canvas.drawBitmap(touchMask, 0f, 0f, paint)
                } else {
                    // MODO QUITAR: Resta de máscaras (DST_OUT)
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                    canvas.drawBitmap(touchMask, 0f, 0f, paint)
                }

                baseCombinedMask = newCombined

                // Aplicar la tolerancia actual (crecimiento/erosión morfológica)
                val tolerancedMask = applyToleranceMorphology(newCombined, _tolerance.value)
                correctedMaskBitmap = tolerancedMask
                rawAiMaskBitmap = tolerancedMask.copy(tolerancedMask.config ?: Bitmap.Config.ARGB_8888, true)

                _segmentationStatus.value = SegmentationStatus.SUCCESS
                applyCurrentTransformations()
            }.onFailure {
                _segmentationStatus.value = SegmentationStatus.ERROR
            }
            _isProcessing.value = false
        }
    }

    // Aplica dilatación (Tolerancia > 50) o erosión (Tolerancia < 50) en tiempo real
    private fun applyToleranceMorphology(baseMask: Bitmap, toleranceValue: Float): Bitmap {
        val width = baseMask.width
        val height = baseMask.height
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        val radius = ((toleranceValue - 50f) / 10f * 3f) // Rango de -15 a 15 píxeles de radio

        if (radius > 0.5f) {
            // Dilatación (Expandir): Dibujar la máscara desplazada en múltiples direcciones
            val r = radius.toInt().coerceAtMost(15)
            canvas.drawBitmap(baseMask, 0f, 0f, null)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            }
            for (dx in -r..r step 2) {
                for (dy in -r..r step 2) {
                    if (dx*dx + dy*dy <= r*r) {
                        canvas.drawBitmap(baseMask, dx.toFloat(), dy.toFloat(), paint)
                    }
                }
            }
        } else if (radius < -0.5f) {
            // Erosión (Contraer): Invertir, dilatar, e invertir de vuelta
            val r = (-radius).toInt().coerceAtMost(15)
            val inverted = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val invCanvas = Canvas(inverted)
            invCanvas.drawColor(Color.WHITE)
            val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            }
            invCanvas.drawBitmap(baseMask, 0f, 0f, maskPaint)

            // Dilatar la máscara invertida
            val dilatedInv = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val dilCanvas = Canvas(dilatedInv)
            dilCanvas.drawBitmap(inverted, 0f, 0f, null)
            val unionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            }
            for (dx in -r..r step 2) {
                for (dy in -r..r step 2) {
                    if (dx*dx + dy*dy <= r*r) {
                        dilCanvas.drawBitmap(inverted, dx.toFloat(), dy.toFloat(), unionPaint)
                    }
                }
            }

            // Invertir de vuelta
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(dilatedInv, 0f, 0f, maskPaint)
        } else {
            canvas.drawBitmap(baseMask, 0f, 0f, null)
        }
        return out
    }

    // Actualiza la tolerancia en tiempo real aplicando morfología sobre el último estado combinado
    fun updateToleranceSlider(value: Float) {
        _tolerance.value = value
        val base = baseCombinedMask ?: return
        viewModelScope.launch(Dispatchers.Default) {
            val toleranced = applyToleranceMorphology(base, value)
            withContext(Dispatchers.Main) {
                correctedMaskBitmap = toleranced
                applyCurrentTransformations()
            }
        }
    }

    // El usuario confirma la selección interactiva táctil presionando "Continuar"
    fun confirmInteractiveSelection() {
        _isInteractiveSelecting.value = false
        // Activa la remoción de fondo clásica con transparencia sobre checkerboard
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(
            bgRemovalActive = true,
            bgOption = "transparent"
        )
        applyCurrentTransformations()
    }

    // Vacía completamente la máscara regresando al estado inicial vacío
    fun resetInteractiveSelection() {
        val original = previewOriginalBitmap ?: return
        val width = original.width
        val height = original.height

        saveMaskToHistory()

        correctedMaskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
        }
        rawAiMaskBitmap = correctedMaskBitmap?.copy(Bitmap.Config.ARGB_8888, true)
        baseCombinedMask = correctedMaskBitmap?.copy(Bitmap.Config.ARGB_8888, true)

        // Regresar tolerancia a su valor inicial adaptativo
        val initialTol = calculateAdaptiveInitialTolerance(original)
        _tolerance.value = initialTol
        _isSelectionModeAdd.value = true

        applyCurrentTransformations()
    }

    fun executeSubjectSegmentation(context: Context) {
        prepareSubjectSegmentation(context)
    }

    // --- CORRECCIÓN MANUAL DE MÁSCARA (PINCELADA) ---
    fun saveMaskToHistory() {
        correctedMaskBitmap?.let { mask ->
            maskUndoStack.push(mask.copy(mask.config ?: Bitmap.Config.ARGB_8888, true))
            maskRedoStack.clear()
            updateMaskHistoryState()
        }
    }

    private fun updateMaskHistoryState() {
        _canUndoMask.value = maskUndoStack.isNotEmpty()
        _canRedoMask.value = maskRedoStack.isNotEmpty()
    }

    fun undoMaskStroke() {
        if (maskUndoStack.isNotEmpty()) {
            val currentMask = correctedMaskBitmap ?: return
            maskRedoStack.push(currentMask)
            val previousMask = maskUndoStack.pop()
            correctedMaskBitmap = previousMask
            baseCombinedMask = previousMask.copy(previousMask.config ?: Bitmap.Config.ARGB_8888, true)
            updateMaskHistoryState()
            applyCurrentTransformations()
        }
    }

    fun redoMaskStroke() {
        if (maskRedoStack.isNotEmpty()) {
            val currentMask = correctedMaskBitmap ?: return
            maskUndoStack.push(currentMask)
            val nextMask = maskRedoStack.pop()
            correctedMaskBitmap = nextMask
            baseCombinedMask = nextMask.copy(nextMask.config ?: Bitmap.Config.ARGB_8888, true)
            updateMaskHistoryState()
            applyCurrentTransformations()
        }
    }

    // Aplica una pincelada de corrección manual directamente sobre la máscara binaria en memoria
    fun applyManualStrokeToMask(points: List<PointF>, mode: String, brushSize: Float) {
        val mask = correctedMaskBitmap ?: return
        val width = mask.width
        val height = mask.height

        val canvas = Canvas(mask)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            if (mode == "erase") {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
            style = Paint.Style.STROKE
            strokeWidth = brushSize
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        if (points.size > 1) {
            val path = android.graphics.Path()
            path.moveTo(points[0].x * width, points[0].y * height)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x * width, points[i].y * height)
            }
            canvas.drawPath(path, paint)
        } else if (points.isNotEmpty()) {
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                if (mode == "erase") {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                }
                style = Paint.Style.FILL
            }
            canvas.drawCircle(points[0].x * width, points[0].y * height, brushSize / 2f, fillPaint)
        }

        applyCurrentTransformations()
    }

    // Restablecer la máscara manual a los resultados originales de la IA
    fun resetMaskToAi() {
        saveMaskToHistory()
        rawAiMaskBitmap?.let { raw ->
            correctedMaskBitmap = raw.copy(raw.config ?: raw.config, true)
            baseCombinedMask = raw.copy(raw.config ?: raw.config, true)
            applyCurrentTransformations()
        }
    }

    // --- GESTIÓN DE CONFIGURACIÓN DE REMOCIÓN DE FONDO ---
    fun updateBackgroundOption(option: String) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(bgOption = option)
        applyCurrentTransformations()
    }

    fun updateBackgroundColor(color: Int) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(bgColor = color)
        applyCurrentTransformations()
    }

    fun updateBackgroundGradientIndex(idx: Int) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(bgGradientIndex = idx)
        applyCurrentTransformations()
    }

    fun updateBlurRadius(radius: Float) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(blurRadius = radius)
        applyCurrentTransformations()
    }

    fun updateOutlineSettings(enabled: Boolean, color: Int = Color.WHITE, size: Float = 12f) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(
            outlineEnabled = enabled,
            outlineColor = color,
            outlineSize = size
        )
        applyCurrentTransformations()
    }

    fun updateShadowSettings(enabled: Boolean, color: Int = Color.BLACK, radius: Float = 15f) {
        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(
            shadowEnabled = enabled,
            shadowColor = color,
            shadowRadius = radius
        )
        applyCurrentTransformations()
    }

    fun loadBackgroundImage(context: Context, uri: Uri) {
        _isProcessing.value = true
        viewModelScope.launch {
            val bitmap = loadSampledBitmap(context, uri, 1000)
            withContext(Dispatchers.Main) {
                selectedBgImageBitmap = bitmap
                val current = _currentTransformations.value
                _currentTransformations.value = current.copy(
                    bgOption = "other_photo",
                    bgImageUri = uri.toString()
                )
                applyCurrentTransformations()
                _isProcessing.value = false
            }
        }
    }

    fun cancelBackgroundRemoval() {
        rawAiMaskBitmap = null
        correctedMaskBitmap = null
        baseCombinedMask = null
        _isInteractiveSelecting.value = false
        maskUndoStack.clear()
        maskRedoStack.clear()
        updateMaskHistoryState()

        val current = _currentTransformations.value
        _currentTransformations.value = current.copy(
            bgRemovalActive = false
        )
        applyCurrentTransformations()
    }

    // --- HISTORIAL GENERAL DEL EDITOR ---
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

    fun savePhoto(context: Context) {
        val uri = originalImageUri ?: return
        _isProcessing.value = true

        viewModelScope.launch {
            try {
                val savedUri = withContext(Dispatchers.IO) {
                    val originalHighRes = loadSampledBitmap(context, uri, 3000)
                    if (originalHighRes == null) {
                        null
                    } else {
                        val transformations = _currentTransformations.value
                        val resultBitmap = processBitmap(originalHighRes, transformations)

                        val hasCrop = transformations.cropRect != null
                        val isPng = hasCrop || transformations.bgRemovalActive || context.contentResolver.getType(uri)?.contains("png") == true
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
