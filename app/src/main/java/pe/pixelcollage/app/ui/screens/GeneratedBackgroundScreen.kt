package pe.pixelcollage.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pe.pixelcollage.app.data.model.*
import pe.pixelcollage.app.ui.navigation.Screen
import pe.pixelcollage.app.utils.BackgroundGenerator
import pe.pixelcollage.app.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedBackgroundScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val draftConfig by projectViewModel.draftGeneratedBackgroundConfig.collectAsState()
    val projectCoverConfig by projectViewModel.currentCoverConfig.collectAsState()
    var hasChanges by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // Listener para el resultado del ColorPickerScreen
    LaunchedEffect(key1 = navController.currentBackStackEntry) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("selected_color_background")
            ?.observe(navController.currentBackStackEntry!!) { colorHex ->
                val newColor = Color(android.graphics.Color.parseColor("#$colorHex"))
                projectViewModel.updateGeneratedBackgroundConfig(
                    draftConfig?.copy(solidColor = newColor)
                )
                // Limpiar el estado para no volver a procesarlo
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_color_background")
            }
    }

    LaunchedEffect(draftConfig, projectCoverConfig) {
        hasChanges = draftConfig != projectCoverConfig.generatedBackgroundConfig
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
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Salir sin guardar") },
            text = { Text("Hay cambios sin guardar. ¿Quieres descartarlos y salir?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    projectViewModel.discardBackgroundConfig()
                    navController.popBackStack()
                }) { Text("Sí, salir") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("No, quedarse") }
            }
        )
    }

    LaunchedEffect(Unit) {
        projectViewModel.startBackgroundEditingSession()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor de Fondo de Hoja") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showDialog = true
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
                                projectViewModel.saveBackgroundConfig(context)
                                Toast.makeText(context, "Fondo guardado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            enabled = hasChanges,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = "Guardar cambios en el fondo",
                                tint = if (hasChanges) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val currentConfig = draftConfig
        if (currentConfig == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Previsualización", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(1f / 1.414f)
                        .border(1.dp, Color.Gray)
                        .padding(1.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                        drawIntoCanvas { canvas ->
                            val dummyTheme = ColorTheme("Dummy", Color.Black, Color.LightGray, Color.DarkGray)
                            BackgroundGenerator.drawGeneratedBackground(
                                canvas.nativeCanvas,
                                currentConfig,
                                size.width,
                                size.height
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Estilos", style = MaterialTheme.typography.titleLarge)
                    Text("Configuración", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Columna Izquierda: Lista de Estilos
                    LazyColumn(
                        modifier = Modifier.weight(0.4f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(BackgroundPatternType.values()) { patternType ->
                            BackgroundTextItem(
                                patternType = patternType,
                                isSelected = currentConfig.patternType == patternType,
                                onClick = {
                                    val newConfig = currentConfig.copy(patternType = patternType)
                                    projectViewModel.updateGeneratedBackgroundConfig(newConfig)
                                }
                            )
                        }
                    }

                    // Columna Derecha: Opciones de Configuración
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .verticalScroll(rememberScrollState())
                            .padding(start = 8.dp, end = 8.dp)
                    ) {
                        when (currentConfig.patternType) {
                            BackgroundPatternType.SÓLIDO -> {
                                SolidColorSettings(
                                    navController = navController,
                                    currentConfig = currentConfig,
                                    onConfigChange = { newConfig ->
                                        projectViewModel.updateGeneratedBackgroundConfig(newConfig)
                                    }
                                )
                            }
                            else -> {
                                PatternSettings(
                                    currentConfig = currentConfig,
                                    onConfigChange = { newConfig ->
                                        projectViewModel.updateGeneratedBackgroundConfig(newConfig)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatternSettings(
    currentConfig: GeneratedBackgroundConfig,
    onConfigChange: (GeneratedBackgroundConfig) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown para seleccionar el tema de color
        var expanded by remember { mutableStateOf(false) }
        val themes = ColorThemes.themes
        val selectedTheme = themes.find { it.name == currentConfig.colorThemeName } ?: themes.first()

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedTheme.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Combinación de Colores") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                themes.forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.name) },
                        onClick = {
                            onConfigChange(currentConfig.copy(colorThemeName = theme.name))
                            expanded = false
                        }
                    )
                }
            }
        }

        SettingsSlider(
            label = "Transparencia",
            value = currentConfig.transparency,
            onValueChange = { onConfigChange(currentConfig.copy(transparency = it)) }
        )

        SettingsSlider(
            label = "Densidad",
            value = currentConfig.density,
            onValueChange = { onConfigChange(currentConfig.copy(density = it)) }
        )

        SettingsSlider(
            label = "Tamaño",
            value = currentConfig.size,
            onValueChange = { onConfigChange(currentConfig.copy(size = it)) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onConfigChange(currentConfig.copy(isRandom = !currentConfig.isRandom)) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Aleatoriedad")
            Switch(
                checked = currentConfig.isRandom,
                onCheckedChange = { onConfigChange(currentConfig.copy(isRandom = it)) }
            )
        }
    }
}

@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(text = "$label: ${"%.1f".format(value)}")
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 100
        )
    }
}

@Composable
private fun SolidColorSettings(
    navController: NavController,
    currentConfig: GeneratedBackgroundConfig,
    onConfigChange: (GeneratedBackgroundConfig) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(
            onClick = {
                val colorHex = String.format("%06X", (0xFFFFFF and currentConfig.solidColor.toArgb()))
                navController.navigate(Screen.ColorPicker.withArgs("background", "background", colorHex))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Color Actual")
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(currentConfig.solidColor, shape = CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onConfigChange(currentConfig.copy(combineWithSolidColor = !currentConfig.combineWithSolidColor))
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Combinar con otros fondos")
            Switch(
                checked = currentConfig.combineWithSolidColor,
                onCheckedChange = { isChecked ->
                    onConfigChange(currentConfig.copy(combineWithSolidColor = isChecked))
                }
            )
        }
    }
}

@Composable
private fun BackgroundTextItem(
    patternType: BackgroundPatternType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = patternType.displayName,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )
}
