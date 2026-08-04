package pe.pixelcollage.app.ui.screens

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pe.pixelcollage.app.R
import pe.pixelcollage.app.ui.components.multicolorShimmer
import pe.pixelcollage.app.utils.SegmentationStatus
import pe.pixelcollage.app.viewmodel.PhotoEditorUiState
import pe.pixelcollage.app.viewmodel.PhotoEditorViewModel
import pe.pixelcollage.app.viewmodel.PhotoTransformations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    navController: NavController,
    imageUri: String,
    initialTool: String = "none",
    viewModel: PhotoEditorViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val transformations by viewModel.currentTransformations.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    // Control de herramienta seleccionada en el panel inferior
    var selectedTool by remember { mutableStateOf(initialTool) }

    // Control de diálogo AI no disponible
    var showAiUnavailableDialog by remember { mutableStateOf(false) }
    var aiUnavailableToolName by remember { mutableStateOf("") }

    // Control de diálogo confirmar salida
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Cargar la foto cuando entra la pantalla
    LaunchedEffect(imageUri) {
        val uri = Uri.parse(imageUri)
        viewModel.loadPhoto(context, uri)
    }

    val segmentationStatus by viewModel.segmentationStatus.collectAsState()
    LaunchedEffect(selectedTool) {
        if (selectedTool == "background_removal" && segmentationStatus == SegmentationStatus.NOT_INITIALIZED) {
            viewModel.prepareSubjectSegmentation(context)
        }
    }

    // Manejar el botón "atrás" del sistema de forma segura
    BackHandler {
        if (canUndo) {
            showExitConfirmDialog = true
        } else {
            navController.popBackStack()
        }
    }

    // Efecto de guardado exitoso
    LaunchedEffect(uiState) {
        if (uiState is PhotoEditorUiState.Success && (uiState as PhotoEditorUiState.Success).isSaved) {
            val successState = uiState as PhotoEditorUiState.Success
            Toast.makeText(context, "Imagen guardada con éxito en la galería", Toast.LENGTH_LONG).show()
            successState.savedUri?.let { uri ->
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/png" // PNG para asegurar transparencia
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Compartir Foto Editada"))
            }
            navController.popBackStack()
        }
    }

    // Diálogos y Modales elegantes
    if (showAiUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showAiUnavailableDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = Color(0xFFB39DDB),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Herramienta Inteligente", color = Color.White)
                }
            },
            text = {
                Text(
                    "La herramienta \"$aiUnavailableToolName\" estará disponible en una próxima actualización.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAiUnavailableDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text("Entendido", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E24),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Cambios sin guardar", color = Color.White)
                }
            },
            text = {
                Text(
                    "¿Deseas salir sin guardar los cambios aplicados en la fotografía?",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Salir sin guardar", color = Color(0xFFE57373))
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExitConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text("Seguir editando", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E24),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            PhotoEditorTopBar(
                title = "Editar foto",
                canUndo = canUndo,
                canRedo = canRedo,
                onBack = {
                    if (canUndo) {
                        showExitConfirmDialog = true
                    } else {
                        navController.popBackStack()
                    }
                },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onSave = { viewModel.savePhoto(context) }
            )
        },
        containerColor = Color.Black // Fondo negro continuo
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Canvas central de la fotografía
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF070709)),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = uiState) {
                        is PhotoEditorUiState.Loading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFFB39DDB))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Cargando fotografía...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                            }
                        }
                        is PhotoEditorUiState.Success -> {
                            PhotoPreviewCanvas(
                                originalBitmap = state.bitmap,
                                viewModel = viewModel,
                                activeTool = selectedTool
                            )
                        }
                        is PhotoEditorUiState.Error -> {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(state.message, color = Color.White, textAlign = TextAlign.Center)
                            }
                        }
                        else -> {}
                    }
                }

                // 2. Panel inferior contextual de herramienta o barra principal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111115))
                        .shadow(8.dp)
                ) {
                    AnimatedContent(
                        targetState = selectedTool,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                        },
                        label = "ToolPanelTransition"
                    ) { tool ->
                        when (tool) {
                            "crop" -> {
                                CropToolPanel(
                                    onConfirmCrop = { rect ->
                                        viewModel.applyCrop(rect)
                                        selectedTool = "none"
                                    },
                                    onCancelCrop = {
                                        selectedTool = "none"
                                    }
                                )
                            }
                            "rotate" -> {
                                RotateToolPanel(
                                    onRotateLeft = { viewModel.rotate90Degrees(false) },
                                    onRotateRight = { viewModel.rotate90Degrees(true) },
                                    onFlipHorizontal = { viewModel.flipHorizontal() },
                                    onFlipVertical = { viewModel.flipVertical() },
                                    onClose = { selectedTool = "none" }
                                )
                            }
                            "adjustments" -> {
                                AdjustmentsToolPanel(
                                    transformations = transformations,
                                    onBrightnessChange = { viewModel.updateBrightness(it) },
                                    onContrastChange = { viewModel.updateContrast(it) },
                                    onSaturationChange = { viewModel.updateSaturation(it) },
                                    onTemperatureChange = { viewModel.updateTemperature(it) },
                                    onExposureChange = { viewModel.updateExposure(it) },
                                    onReset = { viewModel.resetAdjustments() },
                                    onConfirm = {
                                        viewModel.saveTransformToHistory()
                                        selectedTool = "none"
                                    },
                                    onCancel = {
                                        viewModel.undo() // Deshacer los ajustes interactivos
                                        selectedTool = "none"
                                    }
                                )
                            }
                            "background_removal" -> {
                                BackgroundRemovalToolPanel(
                                    viewModel = viewModel,
                                    segmentationStatus = segmentationStatus,
                                    transformations = transformations,
                                    onClose = {
                                        viewModel.cancelBackgroundRemoval()
                                        selectedTool = "none"
                                    },
                                    onApply = {
                                        viewModel.saveTransformToHistory()
                                        selectedTool = "none"
                                    }
                                )
                            }
                            else -> {
                                // Barra de herramientas principal desplazable horizontalmente
                                EditorToolsBar(
                                    activeTool = selectedTool,
                                    onToolSelect = { toolName ->
                                        if (toolName == "ai_remove" || toolName == "ai_enhance") {
                                            aiUnavailableToolName = when (toolName) {
                                                "ai_remove" -> "Eliminar objeto"
                                                else -> "Mejorar calidad"
                                            }
                                            showAiUnavailableDialog = true
                                        } else {
                                            selectedTool = toolName
                                            if (toolName == "adjustments") {
                                                viewModel.saveTransformToHistory()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Overlay de procesamiento con shimmer multicolor sutil
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .pointerInput(Unit) {}, // Bloquear clics
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .width(280.dp)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .multicolorShimmer() // Reutilización de multicolorShimmer
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color(0xFFB39DDB),
                                    strokeWidth = 4.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "Procesando en alta resolución...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Esto puede tardar unos segundos",
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorTopBar(
    title: String,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Deshacer",
                    tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(
                    imageVector = Icons.Default.Redo,
                    contentDescription = "Rehacer",
                    tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
            TextButton(
                onClick = onSave,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB39DDB))
            ) {
                Text(
                    "GUARDAR",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF111115)
        )
    )
}

@Composable
fun PhotoPreviewCanvas(
    originalBitmap: Bitmap,
    viewModel: PhotoEditorViewModel,
    activeTool: String
) {
    val context = LocalContext.current
    val transformations by viewModel.currentTransformations.collectAsState()
    val isInteractiveSelecting by viewModel.isInteractiveSelecting.collectAsState()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    // Interacción para botón "Comparar"
    val compareInteractionSource = remember { MutableInteractionSource() }
    val isComparing by compareInteractionSource.collectIsPressedAsState()

    // Animación suave de entrada de la foto
    val entryAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 400),
        label = "ImageEntryAlpha"
    )
    val entryScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 450),
        label = "ImageEntryScale"
    )

    // Capturar trazos táctiles para la máscara manual o toques interactivos
    val points = remember { mutableStateListOf<android.graphics.PointF>() }

    // El patrón de tablero de ajedrez (checkerboard) clásico se dibuja detrás de la imagen si hay transparencia
    val checkerboardModifier = if (transformations.bgRemovalActive && transformations.bgOption == "transparent") {
        Modifier.drawBehind {
            val tileSize = 16.dp.toPx()
            val w = size.width
            val h = size.height
            var y = 0f
            var rowIdx = 0
            while (y < h) {
                var x = 0f
                var colIdx = 0
                while (x < w) {
                    val color = if ((rowIdx + colIdx) % 2 == 0) Color(0xFF252529) else Color(0xFF19191D)
                    drawRect(
                        color = color,
                        topLeft = androidx.compose.ui.geometry.Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(
                            if (x + tileSize > w) w - x else tileSize,
                            if (y + tileSize > h) h - y else tileSize
                        )
                    )
                    x += tileSize
                    colIdx++
                }
                y += tileSize
                rowIdx++
            }
        }
    } else {
        Modifier
    }

    var brushMode by remember { mutableStateOf("recover") } // recover, erase
    var brushSize by remember { mutableStateOf(40f) }

    // Almacenar el tamaño real renderizado de la imagen para calcular coordenadas táctiles exactas
    var containerWidth by remember { mutableStateOf(1) }
    var containerHeight by remember { mutableStateOf(1) }

    val drawingModifier = if (activeTool == "background_removal") {
        if (isInteractiveSelecting) {
            // Toque inteligente interactivo MediaPipe: detecta toques simples en lugar de arrastre
            Modifier.pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val normX = (tapOffset.x / size.width).coerceIn(0f, 1f)
                    val normY = (tapOffset.y / size.height).coerceIn(0f, 1f)
                    viewModel.handleInteractiveTouch(context, PointF(normX, normY))
                }
            }
        } else {
            // Corrección manual por pincel
            Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        points.clear()
                        val p = android.graphics.PointF(startOffset.x / size.width, startOffset.y / size.height)
                        points.add(p)
                        viewModel.saveMaskToHistory()
                    },
                    onDrag = { change, _ ->
                        val pos = change.position
                        val p = android.graphics.PointF(pos.x / size.width, pos.y / size.height)
                        points.add(p)
                        viewModel.applyManualStrokeToMask(points, brushMode, brushSize)
                    },
                    onDragEnd = {
                        points.clear()
                    }
                )
            }
        }
    } else {
        Modifier.pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = (scale * zoom).coerceIn(1f, 5f)
                if (scale > 1f) {
                    offset += pan
                } else {
                    offset = androidx.compose.ui.geometry.Offset.Zero
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lienzo principal de la imagen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(checkerboardModifier)
                    .then(drawingModifier)
                    .onGloballyPositioned { coordinates ->
                        containerWidth = coordinates.size.width
                        containerHeight = coordinates.size.height
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .graphicsLayer(
                        scaleX = scale * entryScale,
                        scaleY = scale * entryScale,
                        translationX = offset.x,
                        translationY = offset.y,
                        alpha = entryAlpha
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = originalBitmap.asImageBitmap(),
                    contentDescription = "Previsualización",
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.92f),
                    contentScale = ContentScale.Fit
                )

                if (isComparing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.65f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("ORIGINAL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Si es Quitar fondo manual (no interactivo), mostrar controles de dibujo manual sobre el lienzo
            if (activeTool == "background_removal" && !isInteractiveSelecting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = { brushMode = "recover" },
                            modifier = Modifier.background(if (brushMode == "recover") Color(0xFF7E57C2) else Color(0xFF212129), shape = CircleShape)
                        ) {
                            Icon(Icons.Default.Brush, contentDescription = "Recuperar", tint = Color.White)
                        }
                        IconButton(
                            onClick = { brushMode = "erase" },
                            modifier = Modifier.background(if (brushMode == "erase") Color(0xFF7E57C2) else Color(0xFF212129), shape = CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.White)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    ) {
                        Text("TAMAÑO", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = brushSize,
                            onValueChange = { brushSize = it },
                            valueRange = 10f..100f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFB39DDB), activeTrackColor = Color(0xFF7E57C2)),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Row {
                        val canUndoMask by viewModel.canUndoMask.collectAsState()
                        val canRedoMask by viewModel.canRedoMask.collectAsState()

                        IconButton(onClick = { viewModel.undoMaskStroke() }, enabled = canUndoMask) {
                            Icon(Icons.Default.Undo, contentDescription = "Deshacer trazo", tint = if (canUndoMask) Color.White else Color.White.copy(alpha = 0.3f))
                        }
                        IconButton(onClick = { viewModel.redoMaskStroke() }, enabled = canRedoMask) {
                            Icon(Icons.Default.Redo, contentDescription = "Rehacer trazo", tint = if (canRedoMask) Color.White else Color.White.copy(alpha = 0.3f))
                        }
                        IconButton(onClick = { viewModel.resetMaskToAi() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restablecer IA", tint = Color.White)
                        }
                    }
                }
            }

            // Control Comparar debajo del lienzo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .shadow(6.dp, shape = RoundedCornerShape(50))
                        .background(Color(0xFF212129), shape = RoundedCornerShape(50))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(50))
                        .clickable(
                            interactionSource = compareInteractionSource,
                            indication = null
                        ) {}
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = null,
                            tint = Color(0xFFB39DDB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "MANTENER PARA COMPARAR",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditorToolsBar(
    activeTool: String,
    onToolSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolItem(id = "crop", name = "Recortar", icon = Icons.Default.Crop, active = activeTool == "crop", onSelect = onToolSelect)
        ToolItem(id = "rotate", name = "Rotar", icon = Icons.Default.RotateRight, active = activeTool == "rotate", onSelect = onToolSelect)
        ToolItem(id = "adjustments", name = "Ajustes", icon = Icons.Default.Tune, active = activeTool == "adjustments", onSelect = onToolSelect)
        ToolItem(id = "background_removal", name = "Quitar fondo", icon = Icons.Default.ContentCut, active = activeTool == "background_removal", onSelect = onToolSelect)
        ToolItem(id = "ai_remove", name = "Eliminar objeto", icon = Icons.Default.LayersClear, active = false, onSelect = onToolSelect, isAi = true)
        ToolItem(id = "ai_enhance", name = "Mejorar calidad", icon = Icons.Default.AutoAwesome, active = false, onSelect = onToolSelect, isAi = true)
    }
}

@Composable
fun ToolItem(
    id: String,
    name: String,
    icon: ImageVector,
    active: Boolean,
    onSelect: (String) -> Unit,
    isAi: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (active) 1.08f else 1f,
        animationSpec = tween(150),
        label = "ToolScale"
    )

    Column(
        modifier = Modifier
            .width(84.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable { onSelect(id) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (active) Color(0xFF7E57C2).copy(alpha = 0.25f) else Color(0xFF212129),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (active) Color(0xFFB39DDB) else Color.White.copy(alpha = 0.12f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isAi) Color(0xFFB39DDB) else if (active) Color(0xFFB39DDB) else Color.White
            )

            if (isAi) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .background(Color(0xFF7E57C2), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("IA", color = Color.White, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            text = name,
            color = if (active) Color(0xFFB39DDB) else Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun RotateToolPanel(
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Rotar y Voltear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PanelActionButton(icon = Icons.Default.RotateLeft, label = "90° Izq", onClick = onRotateLeft)
            PanelActionButton(icon = Icons.Default.RotateRight, label = "90° Der", onClick = onRotateRight)
            PanelActionButton(icon = Icons.Default.Flip, label = "Espejo H", onClick = onFlipHorizontal)
            PanelActionButton(icon = Icons.Default.FlipToBack, label = "Espejo V", onClick = onFlipVertical)
        }
    }
}

@Composable
fun PanelActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = Color(0xFFB39DDB))
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
fun CropToolPanel(
    onConfirmCrop: (RectF) -> Unit,
    onCancelCrop: () -> Unit
) {
    var selectedRatio by remember { mutableStateOf("free") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recortar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row {
                IconButton(onClick = onCancelCrop) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color(0xFFE57373))
                }
                IconButton(onClick = {
                    val rect = when (selectedRatio) {
                        "1:1" -> RectF(0.1f, 0.1f, 0.9f, 0.9f)
                        "4:5" -> RectF(0.15f, 0.1f, 0.85f, 0.9f)
                        "9:16" -> RectF(0.2f, 0.05f, 0.8f, 0.95f)
                        "16:9" -> RectF(0.05f, 0.2f, 0.95f, 0.8f)
                        else -> RectF(0.1f, 0.1f, 0.9f, 0.9f)
                    }
                    onConfirmCrop(rect)
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar", tint = Color(0xFF81C784))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RatioButton(id = "free", label = "Libre", active = selectedRatio == "free", onSelect = { selectedRatio = it })
            RatioButton(id = "1:1", label = "1:1", active = selectedRatio == "1:1", onSelect = { selectedRatio = it })
            RatioButton(id = "4:5", label = "4:5", active = selectedRatio == "4:5", onSelect = { selectedRatio = it })
            RatioButton(id = "9:16", label = "9:16", active = selectedRatio == "9:16", onSelect = { selectedRatio = it })
            RatioButton(id = "16:9", label = "16:9", active = selectedRatio == "16:9", onSelect = { selectedRatio = it })
        }
    }
}

@Composable
fun RatioButton(
    id: String,
    label: String,
    active: Boolean,
    onSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (active) Color(0xFF7E57C2).copy(alpha = 0.25f) else Color(0xFF212129),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (active) Color(0xFFB39DDB) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect(id) }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (active) Color(0xFFB39DDB) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdjustmentsToolPanel(
    transformations: PhotoTransformations,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onExposureChange: (Float) -> Unit,
    onReset: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var activeAdjustmentTab by remember { mutableStateOf("brightness") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ajustes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onReset) {
                    Text("RESTABLECER", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color(0xFFE57373))
                }
                IconButton(onClick = onConfirm) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar", tint = Color(0xFF81C784))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val currentValue = when (activeAdjustmentTab) {
            "brightness" -> transformations.brightness
            "contrast" -> transformations.contrast
            "saturation" -> transformations.saturation
            "temperature" -> transformations.temperature
            else -> transformations.exposure
        }

        val onValueChange: (Float) -> Unit = when (activeAdjustmentTab) {
            "brightness" -> onBrightnessChange
            "contrast" -> onContrastChange
            "saturation" -> onSaturationChange
            "temperature" -> onTemperatureChange
            else -> onExposureChange
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (activeAdjustmentTab) {
                        "brightness" -> "Brillo"
                        "contrast" -> "Contraste"
                        "saturation" -> "Saturación"
                        "temperature" -> "Temperatura"
                        else -> "Exposición"
                    }.uppercase(),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${currentValue.toInt()}",
                    color = Color(0xFFB39DDB),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = currentValue,
                onValueChange = onValueChange,
                valueRange = -100f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFB39DDB),
                    activeTrackColor = Color(0xFF7E57C2),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdjustmentTabButton(id = "brightness", label = "Brillo", active = activeAdjustmentTab == "brightness", onClick = { activeAdjustmentTab = it })
            AdjustmentTabButton(id = "contrast", label = "Contraste", active = activeAdjustmentTab == "contrast", onClick = { activeAdjustmentTab = it })
            AdjustmentTabButton(id = "saturation", label = "Saturación", active = activeAdjustmentTab == "saturation", onClick = { activeAdjustmentTab = it })
            AdjustmentTabButton(id = "temperature", label = "Temperatura", active = activeAdjustmentTab == "temperature", onClick = { activeAdjustmentTab = it })
            AdjustmentTabButton(id = "exposure", label = "Exposición", active = activeAdjustmentTab == "exposure", onClick = { activeAdjustmentTab = it })
        }
    }
}

@Composable
fun AdjustmentTabButton(
    id: String,
    label: String,
    active: Boolean,
    onClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (active) Color(0xFF7E57C2).copy(alpha = 0.25f) else Color.Transparent,
                shape = RoundedCornerShape(50)
            )
            .border(
                1.dp,
                if (active) Color(0xFFB39DDB) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick(id) }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (active) Color(0xFFB39DDB) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BackgroundRemovalToolPanel(
    viewModel: PhotoEditorViewModel,
    segmentationStatus: SegmentationStatus,
    transformations: PhotoTransformations,
    onClose: () -> Unit,
    onApply: () -> Unit
) {
    val context = LocalContext.current
    val isInteractiveSelecting by viewModel.isInteractiveSelecting.collectAsState()

    val bgImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadBackgroundImage(context, uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Quitar fondo inteligente", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Procesado localmente en tu dispositivo de forma segura", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color(0xFFE57373))
                }
                if (!isInteractiveSelecting) {
                    IconButton(onClick = onApply, enabled = segmentationStatus == SegmentationStatus.SUCCESS) {
                        Icon(Icons.Default.Check, contentDescription = "Confirmar", tint = if (segmentationStatus == SegmentationStatus.SUCCESS) Color(0xFF81C784) else Color.White.copy(alpha = 0.3f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (segmentationStatus) {
            SegmentationStatus.NOT_INITIALIZED -> {
                Button(
                    onClick = { viewModel.prepareSubjectSegmentation(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text("Inicializar Inteligencia Artificial", color = Color.White)
                }
            }
            SegmentationStatus.DOWNLOADING_MODEL -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Preparando Quitar fondo...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Esta descarga se realiza una sola vez de forma segura.", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(color = Color(0xFFB39DDB), modifier = Modifier.fillMaxWidth(0.8f))
                    }
                }
            }
            SegmentationStatus.INITIALIZED -> {
                // El modelo ya está listo, iniciar el procesamiento automáticamente
                viewModel.handleInteractiveTouch(context, PointF(0.5f, 0.5f)) // Toque de inicialización por defecto
            }
            SegmentationStatus.PROCESSING -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFB39DDB), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Analizando fotografía táctil...", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
            SegmentationStatus.SUCCESS, SegmentationStatus.ERROR -> {
                if (isInteractiveSelecting) {
                    // Flujo Táctil Interactivo: Mostrar el mensaje "Toca lo que deseas conservar" y botón "Continuar"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB39DDB).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = Color(0xFFB39DDB),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Toca lo que deseas conservar",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Cada toque añade o quita objetos de la selección.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.confirmInteractiveSelection() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7E57C2),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Continuar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Flujo Clásico: Opciones de Fondos y Pincel manual
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("OPCIONES DE FONDO", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BgOptionButton(
                                label = "Transparente",
                                icon = Icons.Default.Texture,
                                active = transformations.bgOption == "transparent",
                                onClick = { viewModel.updateBackgroundOption("transparent") }
                            )
                            BgOptionButton(
                                label = "Blanco",
                                icon = Icons.Default.Palette,
                                active = transformations.bgOption == "color" && transformations.bgColor == android.graphics.Color.WHITE,
                                onClick = {
                                    viewModel.updateBackgroundColor(android.graphics.Color.WHITE)
                                    viewModel.updateBackgroundOption("color")
                                }
                            )
                            BgOptionButton(
                                label = "Negro",
                                icon = Icons.Default.Palette,
                                active = transformations.bgOption == "color" && transformations.bgColor == android.graphics.Color.BLACK,
                                onClick = {
                                    viewModel.updateBackgroundColor(android.graphics.Color.BLACK)
                                    viewModel.updateBackgroundOption("color")
                                }
                            )
                            repeat(8) { idx ->
                                BgOptionButton(
                                    label = "Degradado ${idx + 1}",
                                    icon = Icons.Default.Gradient,
                                    active = transformations.bgOption == "gradient" && transformations.bgGradientIndex == idx,
                                    onClick = {
                                        viewModel.updateBackgroundGradientIndex(idx)
                                        viewModel.updateBackgroundOption("gradient")
                                    }
                                )
                            }
                            BgOptionButton(
                                label = "Otra Foto",
                                icon = Icons.Default.AddPhotoAlternate,
                                active = transformations.bgOption == "other_photo",
                                onClick = {
                                    bgImagePickerLauncher.launch("image/*")
                                }
                            )
                            BgOptionButton(
                                label = "Desenfocar Original",
                                icon = Icons.Default.BlurOn,
                                active = transformations.bgOption == "blur_original",
                                onClick = {
                                    viewModel.updateBackgroundOption("blur_original")
                                }
                            )
                        }

                        if (transformations.bgOption == "blur_original") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("DESENFOQUE", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = transformations.blurRadius,
                                    onValueChange = { viewModel.updateBlurRadius(it) },
                                    valueRange = 1f..25f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFB39DDB), activeTrackColor = Color(0xFF7E57C2)),
                                    modifier = Modifier.padding(start = 12.dp).weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = transformations.outlineEnabled,
                                    onCheckedChange = { viewModel.updateOutlineSettings(it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7E57C2))
                                )
                                Text("Contorno", color = Color.White, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = transformations.shadowEnabled,
                                    onCheckedChange = { viewModel.updateShadowSettings(it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7E57C2))
                                )
                                Text("Sombra", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BgOptionButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (active) Color(0xFF7E57C2).copy(alpha = 0.25f) else Color(0xFF212129),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (active) Color(0xFFB39DDB) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = if (active) Color(0xFFB39DDB) else Color.White, modifier = Modifier.size(14.dp))
            Text(label, color = if (active) Color(0xFFB39DDB) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
