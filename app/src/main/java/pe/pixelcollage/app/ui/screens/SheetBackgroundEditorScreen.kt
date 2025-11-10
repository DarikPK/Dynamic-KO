package pe.pixelcollage.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pe.pixelcollage.app.data.model.BackgroundPatternType
import pe.pixelcollage.app.data.model.ColorThemes
import pe.pixelcollage.app.data.model.GeneratedBackgroundConfig
import pe.pixelcollage.app.ui.navigation.Screen
import pe.pixelcollage.app.utils.BackgroundGenerator
import pe.pixelcollage.app.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetBackgroundEditorScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val draftConfig by projectViewModel.draftGeneratedBackgroundConfig.collectAsState()

    // Start editing session when the screen is first composed
    LaunchedEffect(Unit) {
        projectViewModel.startBackgroundEditingSession()
    }

    // Discard changes when the user navigates back
    DisposableEffect(navController) {
        onDispose {
            projectViewModel.discardBackgroundConfig()
        }
    }

    // Listen for result from ColorPickerScreen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getLiveData<String>("selected_color_pattern_color")?.observeForever { colorHex ->
            if (colorHex != null) {
                val color = Color(android.graphics.Color.parseColor("#$colorHex"))
                draftConfig?.let {
                    projectViewModel.updateGeneratedBackgroundConfig(it.copy(color = color.toArgb()))
                }
                savedStateHandle.remove<String>("selected_color_pattern_color")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fondo de Hoja(s)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    projectViewModel.saveBackgroundConfig(context)
                    navController.popBackStack()
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Guardar")
            }
        }
    ) { paddingValues ->
        draftConfig?.let { config ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- Preview ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(Color.White) // Always a white background
                        // Use a dummy color theme for preview as it's not available here
                        val dummyTheme = ColorThemes.themes.first()
                        BackgroundGenerator.drawGeneratedBackground(
                            drawContext.canvas.nativeCanvas,
                            config,
                            size.width,
                            size.height,
                            dummyTheme
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Controls ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Habilitar fondo personalizado", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = { isChecked ->
                            projectViewModel.updateGeneratedBackgroundConfig(config.copy(enabled = isChecked))
                        }
                    )
                }

                // Further options are only shown if the background is enabled
                if (config.enabled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Pattern Type Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = config.patternType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Patrón") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            BackgroundPatternType.entries.forEach { pattern ->
                                DropdownMenuItem(
                                    text = { Text(pattern.displayName) },
                                    onClick = {
                                        projectViewModel.updateGeneratedBackgroundConfig(config.copy(patternType = pattern))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Opacity Slider
                    Text("Opacidad: ${String.format("%.2f", config.opacity)}")
                    Slider(
                        value = config.opacity,
                        onValueChange = { newOpacity ->
                            projectViewModel.updateGeneratedBackgroundConfig(config.copy(opacity = newOpacity))
                        },
                        valueRange = 0f..1f
                    )

                    // Size Slider
                    Text("Tamaño: ${String.format("%.0f", config.size)}")
                    Slider(
                        value = config.size,
                        onValueChange = { newSize ->
                            projectViewModel.updateGeneratedBackgroundConfig(config.copy(size = newSize))
                        },
                        valueRange = 5f..50f
                    )

                    // Density Slider
                    Text("Densidad: ${String.format("%.2f", config.density)}")
                    Slider(
                        value = config.density,
                        onValueChange = { newDensity ->
                            projectViewModel.updateGeneratedBackgroundConfig(config.copy(density = newDensity))
                        },
                        valueRange = 0.1f..1f
                    )

                    // Color Picker Button
                    val patternColor = config.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Color del Patrón:", style = MaterialTheme.typography.titleMedium)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(patternColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                        )
                    }
                    Button(
                        onClick = {
                            val colorHex = String.format("%06X", (0xFFFFFF and patternColor.toArgb()))
                            navController.navigate(
                                Screen.ColorPicker.withArgs("pattern_color", "placeholder", colorHex)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Elegir Color para el Patrón")
                    }
                }
            }
        }
    }
}
