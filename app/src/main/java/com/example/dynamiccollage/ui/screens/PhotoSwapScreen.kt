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
import kotlinx.coroutines.launch

data class PhotoItem(val uri: String, val groupIndex: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSwapScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val pageGroups by projectViewModel.currentPageGroups.collectAsState()
    val coverConfig by projectViewModel.currentCoverConfig.collectAsState()
    val pdfGenerationState by projectViewModel.pdfGenerationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var swapCounter by remember { mutableStateOf(0) }

    val allPhotos by remember(pageGroups, coverConfig, swapCounter) {
        derivedStateOf {
            val photoItems = mutableListOf<PhotoItem>()
            coverConfig.mainImageUri?.let { photoItems.add(PhotoItem(it, -1)) } // -1 for cover
            pageGroups.forEachIndexed { index, group ->
                group.imageUris.forEach { uri ->
                    photoItems.add(PhotoItem(uri, index))
                }
            }
            photoItems
        }
    }

    var firstSelection by remember { mutableStateOf<PhotoItem?>(null) }
    var secondSelection by remember { mutableStateOf<PhotoItem?>(null) }
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
            text = { Text("Estás a punto de intercambiar fotos entre diferentes grupos. ¿Quieres continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        projectViewModel.swapPhotos(context, firstSelection!!.uri, secondSelection!!.uri)
                        hasUnsavedChanges = true
                        swapCounter++
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
            if (!isDeleteMode && firstSelection != null && secondSelection != null) {
                FloatingActionButton(onClick = {
                    if (firstSelection!!.groupIndex != secondSelection!!.groupIndex) {
                        showIntergroupSwapConfirmDialog = true
                    } else {
                        projectViewModel.swapPhotos(context, firstSelection!!.uri, secondSelection!!.uri)
                        hasUnsavedChanges = true
                        swapCounter++
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
                itemsIndexed(allPhotos, key = { _, item -> item.uri }) { index, item ->
                    val isSelected = item.uri == firstSelection?.uri || item.uri == secondSelection?.uri
                    val isCover = item.uri == coverConfig.mainImageUri
                    val isFirstSelectionCover = firstSelection?.uri == coverConfig.mainImageUri

                    val isCompatible = firstSelection == null ||
                            isFirstSelectionCover ||
                            isCover ||
                            (ImageUtils.getImageOrientation(context, item.uri) == firstPhotoOrientation)

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
                                        projectViewModel.movePhoto(context, firstSelection!!.uri, item.uri)
                                        hasUnsavedChanges = true
                                        swapCounter++
                                        firstSelection = null
                                    }
                                } else {
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
                                text = "Grupo ${item.groupIndex + 1}",
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (isDeleteMode) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                    .padding(8.dp)
                                    .size(40.dp)
                            )
                        }
                    }
                }
            }
            if (pdfGenerationState == PdfGenerationState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
