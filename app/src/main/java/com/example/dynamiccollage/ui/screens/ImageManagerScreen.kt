package com.example.dynamiccollage.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dynamiccollage.data.model.ImageEffectSettings
import androidx.compose.ui.geometry.Rect
import com.example.dynamiccollage.data.model.SerializableNormalizedRectF
import com.example.dynamiccollage.ui.components.CropView
import com.example.dynamiccollage.ui.components.ProjectEffectsTransformation
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Collect state from the ViewModel
    val coverConfig by projectViewModel.currentCoverConfig.collectAsState()
    val pageGroups by projectViewModel.currentPageGroups.collectAsState()
    val effectSettingsMap by projectViewModel.imageEffectSettings.collectAsState()
    val currentSelectedUriString by projectViewModel.managerSelectedUri.collectAsState()
    val currentSelectedUri = currentSelectedUriString?.let { Uri.parse(it) }

    // Derive the list of all image URIs from the state
    val imageUris = remember(coverConfig, pageGroups) {
        projectViewModel.getAllImageUris()
    }

    // State to force recomposition of CropView
    var cropViewResetKey by remember { mutableStateOf(0) }

    // Get the settings for the currently selected image
    val currentSettings = currentSelectedUriString?.let {
        effectSettingsMap[it]
    } ?: ImageEffectSettings()

    // Initialize the selected URI in the ViewModel once
    LaunchedEffect(imageUris) {
        if (currentSelectedUriString == null && imageUris.isNotEmpty()) {
            projectViewModel.setManagerSelectedUri(imageUris.first())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Imagen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (currentSelectedUriString != null) {
                                val currentRotation = currentSettings.rotationDegrees
                                val newRotation = (currentRotation + 90f) % 360f
                                projectViewModel.updateImageRotation(context, currentSelectedUriString!!, newRotation)
                            }
                        },
                        enabled = currentSelectedUriString != null
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Girar")
                    }
                    IconButton(
                        onClick = {
                            currentSelectedUriString?.let {
                                navController.navigate("image_effects_screen/${Uri.encode(it)}")
                            }
                        },
                        enabled = currentSelectedUriString != null
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Efectos")
                    }
                    IconButton(
                        onClick = {
                            if (currentSelectedUriString != null) {
                                projectViewModel.resetImageTransforms(context, currentSelectedUriString!!)
                                // Force CropView to reset its internal state
                                cropViewResetKey++
                            }
                        },
                        enabled = currentSelectedUriString != null && currentSettings.hasTransforms()
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = "Restablecer")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (currentSelectedUri != null) {
                    key(currentSelectedUriString, cropViewResetKey) {
                        CropView(
                            uri = currentSelectedUri,
                            transformations = listOf(ProjectEffectsTransformation(currentSettings)),
                            onCrop = { cropRect, imageBounds ->
                                coroutineScope.launch {
                                    if (imageBounds.width > 0 && imageBounds.height > 0) {
                                        val normalizedRect = SerializableNormalizedRectF(
                                            left = (cropRect.left - imageBounds.left) / imageBounds.width,
                                            top = (cropRect.top - imageBounds.top) / imageBounds.height,
                                            width = cropRect.width / imageBounds.width,
                                            height = cropRect.height / imageBounds.height
                                        )
                                        projectViewModel.updateImageCrop(context, currentSelectedUriString!!, normalizedRect)
                                        // Increment key to signal CropView should reset its overlay
                                        cropViewResetKey++
                                    }
                                }
                            }
                        )
                    }
                } else {
                    Text("No hay imágenes para editar.")
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageUris) { uriString ->
                    val uri = Uri.parse(uriString)
                    val settings = effectSettingsMap[uriString]
                    val transformations = if (settings != null) {
                        listOf(ProjectEffectsTransformation(settings))
                    } else {
                        emptyList()
                    }

                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(context)
                            .data(uri)
                            .transformations(transformations)
                            .build(),
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(84.dp)
                            .border(
                                width = 2.dp,
                                color = if (uriString == currentSelectedUriString) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable {
                                projectViewModel.setManagerSelectedUri(uriString)
                            }
                    )
                }
            }
        }
    }
}
