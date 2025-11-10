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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pe.pixelcollage.app.data.model.BackgroundPatternType
import pe.pixelcollage.app.data.model.GeneratedBackgroundConfig
import pe.pixelcollage.app.data.model.SheetBackgroundType
import pe.pixelcollage.app.ui.navigation.Screen
import pe.pixelcollage.app.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetBackgroundOptionsScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val projectConfig by projectViewModel.currentCoverConfig.collectAsState()
    val currentColor = projectConfig.pageBackgroundColor?.let { Color(it) } ?: Color.White
    val sheetBackgroundType = projectConfig.sheetBackgroundType

    // Listen for result from ColorPickerScreen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        // For solid background color
        savedStateHandle?.getLiveData<String>("selected_color_background")?.observeForever { colorHex ->
            if (colorHex != null) {
                val color = Color(android.graphics.Color.parseColor("#$colorHex"))
                projectViewModel.updatePageBackgroundColor(context, color)
                savedStateHandle.remove<String>("selected_color_background")
            }
        }

        // For pattern color in custom background
        savedStateHandle?.getLiveData<String>("selected_color_pattern_color")?.observeForever { colorHex ->
            if (colorHex != null) {
                val color = Color(android.graphics.Color.parseColor("#$colorHex"))
                val currentConfig = projectViewModel.currentCoverConfig.value.generatedBackgroundConfig ?: GeneratedBackgroundConfig()
                projectViewModel.updateGeneratedBackgroundConfig(
                    context,
                    currentConfig.copy(color = color.toArgb())
                )
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Solid Background Option ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = sheetBackgroundType == SheetBackgroundType.SOLID,
                        onClick = {
                            projectViewModel.updateSheetBackgroundType(
                                context,
                                SheetBackgroundType.SOLID
                            )
                        }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = sheetBackgroundType == SheetBackgroundType.SOLID,
                    onClick = {
                        projectViewModel.updateSheetBackgroundType(
                            context,
                            SheetBackgroundType.SOLID
                        )
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Sólido", style = MaterialTheme.typography.bodyLarge)
            }

            if (sheetBackgroundType == SheetBackgroundType.SOLID) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp), // Indent to align with RadioButton text
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Color Actual:", style = MaterialTheme.typography.titleMedium)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(currentColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                        )
                    }
                    Button(
                        onClick = {
                            val colorHex =
                                String.format("%06X", (0xFFFFFF and currentColor.toArgb()))
                            navController.navigate(
                                Screen.ColorPicker.withArgs("background", "placeholder", colorHex)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Elegir Color")
                    }
                }
            }

            // --- Custom Background Option ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = sheetBackgroundType == SheetBackgroundType.CUSTOM,
                        onClick = {
                            projectViewModel.updateSheetBackgroundType(
                                context,
                                SheetBackgroundType.CUSTOM
                            )
                        }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = sheetBackgroundType == SheetBackgroundType.CUSTOM,
                    onClick = {
                        projectViewModel.updateSheetBackgroundType(
                            context,
                            SheetBackgroundType.CUSTOM
                        )
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Personalizado", style = MaterialTheme.typography.bodyLarge)
            }

            if (sheetBackgroundType == SheetBackgroundType.CUSTOM) {
                val config = projectConfig.generatedBackgroundConfig ?: GeneratedBackgroundConfig()

                Column(modifier = Modifier.padding(start = 48.dp)) {
                    // Enable/Disable Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Habilitar fondo personalizado", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = config.enabled,
                            onCheckedChange = { isChecked ->
                                projectViewModel.updateGeneratedBackgroundConfig(
                                    context,
                                    config.copy(enabled = isChecked)
                                )
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
                                BackgroundPatternType.entries.forEach { pattern ->
                                    DropdownMenuItem(
                                        text = { Text(pattern.displayName) },
                                        onClick = {
                                            projectViewModel.updateGeneratedBackgroundConfig(
                                                context,
                                                config.copy(patternType = pattern)
                                            )
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
                                projectViewModel.updateGeneratedBackgroundConfig(
                                    context,
                                    config.copy(opacity = newOpacity)
                                )
                            },
                            valueRange = 0f..1f
                        )

                        // Size Slider
                        Text("Tamaño: ${String.format("%.0f", config.size)}")
                        Slider(
                            value = config.size,
                            onValueChange = { newSize ->
                                projectViewModel.updateGeneratedBackgroundConfig(
                                    context,
                                    config.copy(size = newSize)
                                )
                            },
                            valueRange = 5f..50f
                        )

                        // Density Slider
                        Text("Densidad: ${String.format("%.2f", config.density)}")
                        Slider(
                            value = config.density,
                            onValueChange = { newDensity ->
                                projectViewModel.updateGeneratedBackgroundConfig(
                                    context,
                                    config.copy(density = newDensity)
                                )
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
}
