package pe.pixelcollage.app.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import pe.pixelcollage.app.data.model.ImageEffectSettings
import pe.pixelcollage.app.utils.ImageEffects
import pe.pixelcollage.app.viewmodel.ProjectViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEffectsScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    imageUri: String
) {
    val uri = remember { Uri.parse(imageUri) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val draftEffectSettings by projectViewModel.draftImageEffectSettings.collectAsState()
    val currentSettings = draftEffectSettings[imageUri] ?: ImageEffectSettings()

    var brightnessSlider by remember { mutableStateOf(0f) }
    var contrastSlider by remember { mutableStateOf(0f) }
    var saturationSlider by remember { mutableStateOf(0f) }
    var sharpnessSlider by remember { mutableStateOf(0f) }

    LaunchedEffect(currentSettings) {
        brightnessSlider = currentSettings.brightness
        contrastSlider = currentSettings.contrast
        saturationSlider = currentSettings.saturation
        sharpnessSlider = currentSettings.sharpness
    }

    var previewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var originalBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    fun updatePreview() {
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            originalBitmap?.let { ob ->
                val scaleFactor = 400.0 / ob.width.coerceAtLeast(ob.height)
                val thumbnail = if (scaleFactor < 1.0) {
                    android.graphics.Bitmap.createScaledBitmap(ob, (ob.width * scaleFactor).toInt(), (ob.height * scaleFactor).toInt(), true)
                } else ob
                val processedBitmap = ImageEffects.applyEffects(
                    thumbnail,
                    brightnessSlider,
                    1.0f + contrastSlider / 100.0f,
                    1.0f + saturationSlider / 100.0f
                ).run {
                    when {
                        sharpnessSlider > 0 -> ImageEffects.applySharpen(this, sharpnessSlider / 100.0f)
                        sharpnessSlider < 0 -> ImageEffects.applyBlur(this, -sharpnessSlider / 100.0f)
                        else -> this
                    }
                }
                previewBitmap = processedBitmap
            }
        }
    }

    LaunchedEffect(uri) {
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            originalBitmap = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            updatePreview()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustar Efectos de Imagen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás (Cancelar)")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = "Image Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Brillo: ${"%.0f".format(brightnessSlider)}")
                Slider(
                    value = brightnessSlider,
                    onValueChange = { brightnessSlider = it },
                    valueRange = -100f..100f,
                    onValueChangeFinished = { updatePreview() }
                )
                Text("Contraste: ${"%.0f".format(contrastSlider)}")
                Slider(
                    value = contrastSlider,
                    onValueChange = { contrastSlider = it },
                    valueRange = -100f..100f,
                    onValueChangeFinished = { updatePreview() }
                )
                Text("Saturación: ${"%.0f".format(saturationSlider)}")
                Slider(
                    value = saturationSlider,
                    onValueChange = { saturationSlider = it },
                    valueRange = -100f..100f,
                    onValueChangeFinished = { updatePreview() }
                )
                Text("Nitidez: ${"%.0f".format(sharpnessSlider)}")
                Slider(
                    value = sharpnessSlider,
                    onValueChange = { sharpnessSlider = it },
                    valueRange = -100f..100f,
                    onValueChangeFinished = { updatePreview() }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = {
                        val updatedSettings = currentSettings.copy(
                            brightness = brightnessSlider,
                            contrast = contrastSlider,
                            saturation = saturationSlider,
                            sharpness = sharpnessSlider
                        )
                        projectViewModel.updateImageEffectSettings(imageUri, updatedSettings)
                        navController.popBackStack()
                    }) {
                        Text("Aplicar")
                    }
                    Button(onClick = {
                        brightnessSlider = 0f
                        contrastSlider = 0f
                        saturationSlider = 0f
                        sharpnessSlider = 0f
                        updatePreview()
                    }) {
                        Text("Restablecer")
                    }
                }
            }
        }
    }
}
