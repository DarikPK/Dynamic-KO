package com.example.dynamiccollage.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.utils.ImageUtils
import com.example.dynamiccollage.viewmodel.PdfGenerationState
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import com.example.dynamiccollage.data.model.PhotoArrangementItem
import com.example.dynamiccollage.data.model.SheetType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSwapScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val pdfGenerationState by projectViewModel.pdfGenerationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        projectViewModel.initializePhotoArrangement()
    }

    val photoArrangement by projectViewModel.photoArrangement.collectAsState()

    var firstSelection by remember { mutableStateOf<PhotoArrangementItem?>(null) }
    var secondSelection by remember { mutableStateOf<PhotoArrangementItem?>(null) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    var isDragMode by remember { mutableStateOf(false) }
    var showDeleteConfirmDialogSingle by remember { mutableStateOf<String?>(null) }
    var showIntergroupSwapConfirmDialog by remember { mutableStateOf(false) }


    val firstPhotoOrientation by remember(firstSelection) {
        derivedStateOf {
            firstSelection?.let { ImageUtils.getImageOrientation(context, it.uri) }
        }
    }

    fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showExitConfirmDialog = true
        } else {
            navController.popBackStack()
        }
    }

    BackHandler {
        handleBackNavigation()
    }

    LaunchedEffect(pdfGenerationState) {
        when (pdfGenerationState) {
            is PdfGenerationState.Success -> {
                val file = (pdfGenerationState as PdfGenerationState.Success).file
                val encodedPath = java.net.URLEncoder.encode(file.absolutePath, "UTF-8")
                navController.navigate(Screen.PdfPreview.withArgs(encodedPath)) {
                    // Pop up to MainScreen to clear the back stack
                    popUpTo(Screen.Main.route)
                }
                projectViewModel.resetPdfGenerationState()
            }
            is PdfGenerationState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Error al guardar: ${(pdfGenerationState as PdfGenerationState.Error).message}")
                }
                projectViewModel.resetPdfGenerationState()
            }
            else -> {}
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Descartar Cambios") },
            text = { Text("¿Estás seguro que quieres salir sin guardar los cambios?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        navController.popBackStack()
                    }
                ) { Text("Salir") }
            },
            dismissButton = {
                Button(onClick = { showExitConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    LaunchedEffect(isDeleteMode, isDragMode) {
        if (isDeleteMode || isDragMode) {
            firstSelection = null
            secondSelection = null
        }
    }

    if (showIntergroupSwapConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showIntergroupSwapConfirmDialog = false },
            title = { Text("Confirmar Intercambio") },
            text = { Text("Estás a punto de intercambiar fotos entre diferentes tipos de hoja. ¿Quieres continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        projectViewModel.swapPhotoOrder(firstSelection!!, secondSelection!!)
                        hasUnsavedChanges = true
                        firstSelection = null
                        secondSelection = null
                        showIntergroupSwapConfirmDialog = false
                    }
                ) { Text("Continuar") }
            },
            dismissButton = {
                Button(onClick = { showIntergroupSwapConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteConfirmDialogSingle != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialogSingle = null },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro que quieres mover esta foto a la papelera?") },
            confirmButton = {
                Button(
                    onClick = {
                        projectViewModel.deletePhoto(context, showDeleteConfirmDialogSingle!!)
                        hasUnsavedChanges = true
                        showDeleteConfirmDialogSingle = null
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmDialogSingle = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ordenar Fotos") },
                navigationIcon = {
                    IconButton(onClick = { handleBackNavigation() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { isDeleteMode = false }) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Modo Intercambio",
                            tint = if (!isDeleteMode) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    IconButton(onClick = {
                        isDeleteMode = true
                        isDragMode = false
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Modo Eliminar",
                            tint = if (isDeleteMode) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    IconButton(onClick = {
                        isDragMode = true
                        isDeleteMode = false
                    }) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Modo Arrastrar",
                            tint = if (isDragMode) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    IconButton(
                        onClick = {
                            projectViewModel.saveArrangement(context)
                            projectViewModel.generatePdf(context, "updated_project")
                        },
                        enabled = hasUnsavedChanges && pdfGenerationState != PdfGenerationState.Loading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isDeleteMode && !isDragMode && firstSelection != null && secondSelection != null) {
                FloatingActionButton(onClick = {
                    if (firstSelection!!.sheetType != secondSelection!!.sheetType) {
                        showIntergroupSwapConfirmDialog = true
                    } else {
                        projectViewModel.swapPhotoOrder(firstSelection!!, secondSelection!!)
                        hasUnsavedChanges = true
                        firstSelection = null
                        secondSelection = null
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar Intercambio")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(photoArrangement.sortedBy { it.order }, key = { _, item -> item.uri }) { index, item ->
                    val isSelected = item.uri == firstSelection?.uri || item.uri == secondSelection?.uri
                    val isCover = item.order == 1

                    val isCompatible = firstSelection == null ||
                            isCover ||
                            (item.sheetType == SheetType.SINGLE || firstSelection?.sheetType == SheetType.SINGLE) ||
                            (ImageUtils.getImageOrientation(context, item.uri) == firstSelection?.let { ImageUtils.getImageOrientation(context, it.uri) })

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(
                                width = if (isSelected) 4.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .alpha(if (isDeleteMode || isDragMode || isCompatible) 1f else 0.4f)
                            .clickable(enabled = isDeleteMode || isDragMode || isCompatible) {
                                if (isDeleteMode) {
                                    showDeleteConfirmDialogSingle = item.uri
                                } else if (isDragMode) {
                                    if (firstSelection == null) {
                                        firstSelection = item
                                    } else {
                                        projectViewModel.movePhotoOrder(firstSelection!!, item)
                                        hasUnsavedChanges = true
                                        firstSelection = null
                                    }
                                } else { // Swap Mode
                                    if (firstSelection == null) {
                                        firstSelection = item
                                    } else if (secondSelection == null) {
                                        if (item.uri != firstSelection?.uri) {
                                            secondSelection = item
                                        } else {
                                            firstSelection = null
                                        }
                                    } else {
                                        if (item.uri == firstSelection?.uri) {
                                            firstSelection = secondSelection
                                            secondSelection = null
                                        } else if (item.uri == secondSelection?.uri) {
                                            secondSelection = null
                                        } else {
                                            firstSelection = item
                                            secondSelection = null
                                        }
                                    }
                                }
                            }
                    ) {
                        AsyncImage(
                            model = Uri.parse(item.uri),
                            contentDescription = "Foto para intercambiar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "Orden ${item.order}",
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        if (isCover) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Foto de portada",
                                tint = Color.Yellow,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                    .padding(4.dp)
                            )
                        } else {
                             Text(
                                text = if (item.sheetType == SheetType.SINGLE) "Hoja Única" else "Hoja Doble",
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        // Icons for modes will be handled in the TopAppBar
                    }
                }
            }
            if (pdfGenerationState == PdfGenerationState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
