package pe.pixelcollage.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pe.pixelcollage.app.R
import pe.pixelcollage.app.data.model.BackgroundPatternType
import pe.pixelcollage.app.data.model.ColorThemes
import pe.pixelcollage.app.data.model.GeneratedBackgroundConfig
import pe.pixelcollage.app.utils.BackgroundGenerator
import pe.pixelcollage.app.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedBackgroundScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val projectConfig by projectViewModel.currentCoverConfig.collectAsState()
    val initialConfig = projectConfig.generatedBackgroundConfig ?: GeneratedBackgroundConfig()

    var draftConfig by remember { mutableStateOf(initialConfig) }
    var originalConfig by remember { mutableStateOf(initialConfig) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Sincroniza el estado si la configuración inicial del ViewModel cambia
    LaunchedEffect(initialConfig) {
        draftConfig = initialConfig
        originalConfig = initialConfig
    }

    val hasChanges by remember {
        derivedStateOf { draftConfig != originalConfig }
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
                    navController.popBackStack()
                }) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("No, quedarse")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mosaico") },
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
                                projectViewModel.updateGeneratedBackgroundConfig(draftConfig)
                                originalConfig = draftConfig // Actualiza el estado original al guardar
                                Toast.makeText(context, "Mosaico guardado", Toast.LENGTH_SHORT).show()
                            },
                            enabled = hasChanges,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = "Guardar Mosaico",
                                tint = if (hasChanges) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MosaicPreview(config = draftConfig)

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = draftConfig.patternType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Patrón") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    BackgroundPatternType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                val newConfig = draftConfig.copy(
                                    patternType = type,
                                    enabled = type != BackgroundPatternType.NONE
                                )
                                draftConfig = newConfig
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (draftConfig.patternType != BackgroundPatternType.NONE) {
                Text("Color", style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(ColorThemes.themes) { theme ->
                        val color = theme.rucBackgroundColor
                        val isSelected = draftConfig.color == color.toArgb()

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    draftConfig = draftConfig.copy(color = color.toArgb())
                                }
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Opacidad: ${draftConfig.opacity.format(2)}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = draftConfig.opacity,
                    onValueChange = { newOpacity ->
                        draftConfig = draftConfig.copy(opacity = newOpacity)
                    },
                    valueRange = 0f..1f
                )

                Text("Tamaño: ${draftConfig.size.format(2)}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = draftConfig.size,
                    onValueChange = { newSize ->
                        draftConfig = draftConfig.copy(size = newSize)
                    },
                    valueRange = 1f..100f
                )

                Text("Densidad: ${draftConfig.density.format(2)}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = draftConfig.density,
                    onValueChange = { newDensity ->
                        draftConfig = draftConfig.copy(density = newDensity)
                    },
                    valueRange = 0.1f..1f
                )
            }
        }
    }
}

private fun Float.format(digits: Int) = "%.${digits}f".format(this)

@Composable
private fun MosaicPreview(config: GeneratedBackgroundConfig) {
    val context = LocalContext.current
    val projectViewModel: ProjectViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val themeName by projectViewModel.themeName.collectAsState()
    val currentTheme = ColorThemes.themes.find { it.name == themeName } ?: ColorThemes.themes.first()

    val bitmap = remember {
        Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888)
    }

    LaunchedEffect(config, currentTheme) {
        val canvas = Canvas(bitmap)
        // Limpiar el canvas
        canvas.drawColor(android.graphics.Color.WHITE)
        // Dibujar el fondo
        if (config.patternType != BackgroundPatternType.NONE) {
            BackgroundGenerator.drawGeneratedBackground(
                canvas,
                config,
                200f,
                300f,
                currentTheme
            )
        }
    }

    Box(
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Previsualización del Mosaico",
            modifier = Modifier.fillMaxSize()
        )
    }
}
