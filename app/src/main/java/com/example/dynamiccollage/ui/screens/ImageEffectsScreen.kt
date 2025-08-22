package com.example.dynamiccollage.ui.screens

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
import androidx.compose.material3.CircularProgressIndicator
import com.example.dynamiccollage.data.model.ImageEffectSettings
import com.example.dynamiccollage.utils.ImageEffects
import com.example.dynamiccollage.viewmodel.ProjectViewModel
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

    val effectSettingsMap by projectViewModel.imageEffectSettings.collectAsState()

    // State for sliders, initialized to default
    var brightnessSlider by remember { mutableStateOf(0f) }
    var contrastSlider by remember { mutableStateOf(0f) }
    var saturationSlider by remember { mutableStateOf(0f) }
    var sharpnessSlider by remember { mutableStateOf(0f) }

    // State for the preview bitmap
    var previewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    // Store the original bitmap in memory
    var originalBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // This one effect handles loading the bitmap, initializing the sliders, and updating the preview
    LaunchedEffect(uri, effectSettingsMap) {
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }
            originalBitmap = bitmap

            val settings = effectSettingsMap[imageUri] ?: ImageEffectSettings()
            brightnessSlider = settings.brightness
            contrastSlider = settings.contrast
            saturationSlider = settings.saturation
            sharpnessSlider = settings.sharpness

            // Now that the bitmap is loaded and sliders are set, generate the initial preview
            updatePreview()
        }
    }

    // Function to update the preview bitmap
    fun updatePreview() {
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            originalBitmap?.let { ob ->
                // Create a smaller bitmap for faster preview processing
                val scaleFactor = 400.0 / ob.width.coerceAtLeast(ob.height)
                val thumbnail = if (scaleFactor < 1.0) {
                    android.graphics.Bitmap.createScaledBitmap(ob, (ob.width * scaleFactor).toInt(), (ob.height * scaleFactor).toInt(), true)
                } else {
                    ob
                }

                val brightness = brightnessSlider
                val contrast = 1.0f + contrastSlider / 100.0f
                val saturation = 1.0f + saturationSlider / 100.0f
                val sharpness = sharpnessSlider / 100.0f

                var processedBitmap = ImageEffects.applyEffects(thumbnail, brightness, contrast, saturation)
                if (sharpness > 0) {
                    processedBitmap = ImageEffects.applySharpen(processedBitmap, sharpness)
                } else if (sharpness < 0) {
                    processedBitmap = ImageEffects.applyBlur(processedBitmap, -sharpness)
                }

                previewBitmap = processedBitmap
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustar Efectos de Imagen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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

            // Sliders for image effects
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
                        coroutineScope.launch {
                            originalBitmap?.let { ob ->
                                val brightness = brightnessSlider
                                val contrast = 1.0f + contrastSlider / 100.0f
                                val saturation = 1.0f + saturationSlider / 100.0f
                                val sharpness = sharpnessSlider / 100.0f

                                var processedBitmap = ImageEffects.applyEffects(ob, brightness, contrast, saturation)
                                if (sharpness > 0) {
                                    processedBitmap = ImageEffects.applySharpen(processedBitmap, sharpness)
                                } else if (sharpness < 0) {
                                    processedBitmap = ImageEffects.applyBlur(processedBitmap, -sharpness)
                                }

                                val newSettings = ImageEffectSettings(
                                    brightness = brightnessSlider,
                                    contrast = contrastSlider,
                                    saturation = saturationSlider,
                                    sharpness = sharpnessSlider
                                )
                                projectViewModel.updateImageEffectSettings(imageUri, newSettings)

                                projectViewModel.saveImageWithEffects(
                                    context = context,
                                    oldUri = imageUri,
                                    bitmapWithEffects = processedBitmap
                                )
                                navController.popBackStack()
                            }
                        }
                    }) {
                        Text("Aplicar y Guardar")
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
