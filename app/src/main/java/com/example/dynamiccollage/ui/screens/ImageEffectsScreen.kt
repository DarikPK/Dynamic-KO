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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dynamiccollage.ui.components.ColorMatrixTransformation
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

    // State for sliders
    var brightnessSlider by remember { mutableStateOf(0f) }
    var contrastSlider by remember { mutableStateOf(0f) }
    var saturationSlider by remember { mutableStateOf(0f) }

    // Debounced state for Coil transformation
    var brightnessEffect by remember { mutableStateOf(0f) }
    var contrastEffect by remember { mutableStateOf(1f) }
    var saturationEffect by remember { mutableStateOf(1f) }

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
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri)
                        .transformations(
                            ColorMatrixTransformation(
                                brightness = brightnessEffect,
                                contrast = contrastEffect,
                                saturation = saturationEffect
                            )
                        )
                        .build(),
                    contentDescription = "Image Preview"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sliders for image effects
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Brillo: ${"%.0f".format(brightnessSlider)}")
                Slider(
                    value = brightnessSlider,
                    onValueChange = { brightnessSlider = it },
                    valueRange = -100f..100f,
                    onValueChangeFinished = {
                        brightnessEffect = brightnessSlider
                    }
                )
                Text("Contraste: ${"%.0f".format(contrastSlider)}")
                Slider(
                    value = contrastSlider,
                    onValueChange = { contrastSlider = it },
                    valueRange = -100f..100f,
                    onValueChangeFinished = {
                        contrastEffect = 1.0f + contrastSlider / 100.0f
                    }
                )
                Text("Saturación: ${"%.0f".format(saturationSlider)}")
                Slider(
                    value = saturationSlider,
                    onValueChange = { saturationSlider = it },
                    valueRange = -100f..100f,
                    onValueChangeFinished = {
                        saturationEffect = 1.0f + saturationSlider / 100.0f
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            val originalBitmap = context.contentResolver.openInputStream(uri)?.use {
                                BitmapFactory.decodeStream(it)
                            }
                            if (originalBitmap != null) {
                                val bitmapWithEffects = ImageEffects.applyEffects(
                                    bitmap = originalBitmap,
                                    brightness = brightnessEffect,
                                    contrast = contrastEffect,
                                    saturation = saturationEffect
                                )
                                projectViewModel.saveImageWithEffects(
                                    context = context,
                                    oldUri = imageUri,
                                    bitmapWithEffects = bitmapWithEffects
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
                        brightnessEffect = 0f
                        contrastEffect = 1f
                        saturationEffect = 1f
                    }) {
                        Text("Restablecer")
                    }
                }
            }
        }
    }
}
