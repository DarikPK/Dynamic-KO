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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
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
import com.example.dynamiccollage.utils.ImageUtils
import com.example.dynamiccollage.viewmodel.PdfGenerationState
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import kotlinx.coroutines.launch

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
            val innerImages = pageGroups.flatMap { it.imageUris }
            val all = mutableListOf<String>()
            coverConfig.mainImageUri?.let { all.add(it) }
            all.addAll(innerImages)
            all
        }
    }

    var firstSelection by remember { mutableStateOf<String?>(null) }
    var secondSelection by remember { mutableStateOf<String?>(null) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val firstPhotoOrientation by remember(firstSelection) {
        derivedStateOf {
            firstSelection?.let { ImageUtils.getImageOrientation(context, it) }
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
                scope.launch {
                    snackbarHostState.showSnackbar("PDF guardado correctamente.")
                }
                projectViewModel.resetPdfGenerationState()
                navController.popBackStack()
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Intercambiar Fotos") },
                navigationIcon = {
                    IconButton(onClick = { handleBackNavigation() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            projectViewModel.generatePdf(context, "updated_project")
                        },
                        enabled = hasUnsavedChanges && pdfGenerationState != PdfGenerationState.Loading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (firstSelection != null && secondSelection != null) {
                FloatingActionButton(onClick = {
                    projectViewModel.swapPhotos(context, firstSelection!!, secondSelection!!)
                    hasUnsavedChanges = true
                    swapCounter++
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
                itemsIndexed(allPhotos, key = { _, uri -> uri }) { index, uri ->
                    val isSelected = uri == firstSelection || uri == secondSelection
                    val isCover = uri == coverConfig.mainImageUri
                    val isFirstSelectionCover = firstSelection == coverConfig.mainImageUri

                    val isCompatible = firstSelection == null ||
                            isFirstSelectionCover ||
                            isCover ||
                            (ImageUtils.getImageOrientation(context, uri) == firstPhotoOrientation)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(
                                width = if (isSelected) 4.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .alpha(if (isCompatible) 1f else 0.4f)
                            .clickable(enabled = isCompatible) {
                                if (firstSelection == null) {
                                    firstSelection = uri
                                } else if (secondSelection == null) {
                                    if (uri != firstSelection) {
                                        secondSelection = uri
                                    } else {
                                        firstSelection = null
                                    }
                                } else {
                                    if (uri == firstSelection) {
                                        firstSelection = secondSelection
                                        secondSelection = null
                                    } else if (uri == secondSelection) {
                                        secondSelection = null
                                    } else {
                                        firstSelection = uri
                                        secondSelection = null
                                    }
                                }
                            }
                    ) {
                        AsyncImage(
                            model = Uri.parse(uri),
                            contentDescription = "Foto para intercambiar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = (index + 1).toString(),
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                .padding(4.dp)
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
