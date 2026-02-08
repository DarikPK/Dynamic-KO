package pe.pixelcollage.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import pe.pixelcollage.app.data.model.ImageEffectSettings
import pe.pixelcollage.app.data.model.SerializableNormalizedRectF
import pe.pixelcollage.app.ui.components.CropView
import pe.pixelcollage.app.ui.components.ProjectEffectsTransformation
import pe.pixelcollage.app.viewmodel.ProjectViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val originalEffectSettings by projectViewModel.imageEffectSettings.collectAsState()
    val draftEffectSettings by projectViewModel.draftImageEffectSettings.collectAsState()
    val currentSelectedUriString by projectViewModel.managerSelectedUri.collectAsState()
    val currentSelectedUri = currentSelectedUriString?.let { Uri.parse(it) }

    val imageUris = projectViewModel.getAllImageUris()

    var cropViewResetKey by remember { mutableStateOf(0) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Iniciar la sesión de edición al entrar en la pantalla
    LaunchedEffect(Unit) {
        projectViewModel.startImageEditingSession()
    }

    val hasChanges by remember {
        derivedStateOf { draftEffectSettings != originalEffectSettings }
    }

    val currentSettings = currentSelectedUriString?.let {
        draftEffectSettings[it]
    } ?: ImageEffectSettings()

    LaunchedEffect(imageUris) {
        if (currentSelectedUriString == null && imageUris.isNotEmpty()) {
            projectViewModel.setManagerSelectedUri(imageUris.first())
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (hasChanges) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    BackHandler(enabled = hasChanges) {
        showExitConfirmDialog = true
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Salir sin guardar") },
            text = { Text("Has realizado cambios pero no los has guardado. ¿Estás seguro de que quieres salir?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmDialog = false
                    projectViewModel.discardImageEffects()
                    navController.popBackStack()
                }) { Text("Sí, salir") }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) { Text("No, quedarse") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Imagen") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showExitConfirmDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (currentSelectedUriString != null) {
                                val currentRotation = currentSettings.rotationDegrees
                                val newRotation = (currentRotation + 90f) % 360f
                                projectViewModel.updateImageRotation(currentSelectedUriString!!, newRotation)
                            }
                        },
                        enabled = currentSelectedUriString != null
                    ) { Icon(Icons.Default.RotateRight, contentDescription = "Girar") }

                    IconButton(
                        onClick = {
                            currentSelectedUriString?.let {
                                navController.navigate("image_effects_screen/${Uri.encode(it)}")
                            }
                        },
                        enabled = currentSelectedUriString != null
                    ) { Icon(Icons.Default.Tune, contentDescription = "Efectos") }

                    IconButton(
                        onClick = {
                            if (currentSelectedUriString != null) {
                                projectViewModel.resetImageTransforms(currentSelectedUriString!!)
                                cropViewResetKey++
                            }
                        },
                        enabled = currentSelectedUriString != null && currentSettings.hasTransforms()
                    ) { Icon(Icons.Default.Restore, contentDescription = "Restablecer") }

                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(CircleShape)
                            .background(if (hasChanges) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .border(
                                width = if (hasChanges) 1.5.dp else 0.dp,
                                color = if (hasChanges) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        IconButton(
                            onClick = {
                                projectViewModel.saveImageEffects(context)
                                Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                            },
                            enabled = hasChanges
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = "Guardar Cambios")
                        }
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
                var bitmapForCropper by remember { mutableStateOf<Bitmap?>(null) }

                LaunchedEffect(currentSelectedUri, draftEffectSettings) {
                    if (currentSelectedUri != null) {
                        val settings = draftEffectSettings[currentSelectedUriString] ?: ImageEffectSettings()
                        val inputStream = context.contentResolver.openInputStream(currentSelectedUri)

                        val processed = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            val originalBitmap = inputStream?.use {
                                android.graphics.BitmapFactory.decodeStream(it)
                            } ?: return@withContext null

                            var current: Bitmap = originalBitmap

                            // 1. Aplicar Rotación
                            if (settings.rotationDegrees != 0f) {
                                val b = current
                                val matrix = Matrix().apply { postRotate(settings.rotationDegrees) }
                                current = Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
                            }

                            // 2. Aplicar Efectos de Color
                            current = pe.pixelcollage.app.utils.ImageEffects.applyEffects(
                                current,
                                settings.brightness,
                                1.0f + settings.contrast / 100.0f,
                                1.0f + settings.saturation / 100.0f
                            )

                            // 3. Aplicar Nitidez/Desenfoque
                            val sharpness = settings.sharpness / 100.0f
                            current = when {
                                sharpness > 0 -> pe.pixelcollage.app.utils.ImageEffects.applySharpen(current, sharpness)
                                sharpness < 0 -> pe.pixelcollage.app.utils.ImageEffects.applyBlur(current, -sharpness)
                                else -> current
                            }

                            current
                        }
                        bitmapForCropper = processed
                    } else {
                        bitmapForCropper = null
                    }
                }

                if (bitmapForCropper != null) {
                    key(currentSelectedUriString, cropViewResetKey) {
                        CropView(
                            bitmap = bitmapForCropper!!,
                            onCrop = { cropRect, imageBounds ->
                                if (imageBounds.width > 0 && imageBounds.height > 0) {
                                    val normalizedRect = SerializableNormalizedRectF(
                                        left = (cropRect.left - imageBounds.left) / imageBounds.width,
                                        top = (cropRect.top - imageBounds.top) / imageBounds.height,
                                        width = cropRect.width / imageBounds.width,
                                        height = cropRect.height / imageBounds.height
                                    )
                                    projectViewModel.updateImageCrop(currentSelectedUriString!!, normalizedRect)
                                    cropViewResetKey++
                                }
                            }
                        )
                    }
                } else if (currentSelectedUri != null) {
                    CircularProgressIndicator()
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
                    val settings = draftEffectSettings[uriString]
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
                            .clickable { projectViewModel.setManagerSelectedUri(uriString) }
                    )
                }
            }
        }
    }
}
