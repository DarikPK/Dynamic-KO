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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pe.pixelcollage.app.data.model.BackgroundPatternType
import pe.pixelcollage.app.data.model.ColorTheme
import pe.pixelcollage.app.data.model.GeneratedBackgroundConfig
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Previsualización", style = MaterialTheme.typography.titleLarge)

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
                                size.height,
                                dummyTheme
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Seleccionar Estilo", style = MaterialTheme.typography.titleLarge)

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(BackgroundPatternType.values()) { patternType ->
                        BackgroundThumbnail(
                            patternType = patternType,
                            isSelected = currentConfig.patternType == patternType,
                            onClick = {
                                val newConfig = currentConfig.copy(patternType = patternType)
                                projectViewModel.updateGeneratedBackgroundConfig(newConfig)
                            }
                        )
                    }
                }

                if (currentConfig.patternType == BackgroundPatternType.SÓLIDO) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Color Sólido")
                    OutlinedButton(
                        onClick = {
                            val colorHex = String.format("%06X", (0xFFFFFF and currentConfig.solidColor.toArgb()))
                            navController.navigate(Screen.ColorPicker.withArgs("background", "background", colorHex))
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
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
                }
            }
        }
    }
}

@Composable
private fun BackgroundThumbnail(
    patternType: BackgroundPatternType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                )
                .padding(2.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawIntoCanvas { canvas ->
                     val dummyTheme = ColorTheme("Dummy", Color.Black, Color.LightGray, Color.DarkGray)
                     val config = GeneratedBackgroundConfig(patternType = patternType)
                     BackgroundGenerator.drawGeneratedBackground(
                         canvas.nativeCanvas,
                         config,
                         size.width,
                         size.height,
                         dummyTheme
                     )
                }
            }
        }
        Text(
            text = patternType.displayName,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
