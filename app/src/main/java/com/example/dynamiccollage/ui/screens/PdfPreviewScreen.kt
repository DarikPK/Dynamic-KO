package com.example.dynamiccollage.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.PhotoRect
import com.example.dynamiccollage.ui.components.SwapPhotoDialog
import com.example.dynamiccollage.ui.components.ZoomableImage
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import com.example.dynamiccollage.viewmodel.PdfGenerationState
import java.io.File

// Data class to hold the state for the PDF renderer.
data class RendererState(
    val renderer: PdfRenderer?,
    val pageCount: Int,
    val pfd: ParcelFileDescriptor?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
    navController: NavController,
    pdfPath: String?,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val initialFile = pdfPath?.let { File(it) }
    val pdfGenerationState by projectViewModel.pdfGenerationState.collectAsState()

    var currentFile by remember { mutableStateOf(initialFile) }
    var photoLayouts by remember { mutableStateOf<List<PhotoRect>>(emptyList()) }
    var showSwapDialog by remember { mutableStateOf(false) }
    var firstPhotoToSwap by remember { mutableStateOf<PhotoRect?>(null) }
    val allPhotos by remember { derivedStateOf { projectViewModel.getAllImageUris() } }

    LaunchedEffect(pdfGenerationState) {
        when (val state = pdfGenerationState) {
            is PdfGenerationState.Success -> {
                currentFile = state.file
                photoLayouts = state.photoLayouts
            }
            else -> { /* Do nothing for other states */ }
        }
    }

    if (showSwapDialog && firstPhotoToSwap != null) {
        SwapPhotoDialog(
            onDismissRequest = { showSwapDialog = false },
            allPhotos = allPhotos,
            firstPhotoUri = firstPhotoToSwap!!.uri,
            onPhotoSelected = { secondPhotoUri ->
                projectViewModel.swapPhotos(context, firstPhotoToSwap!!.uri, secondPhotoUri)
                projectViewModel.generatePdf(context, "preview_regenerated")
                showSwapDialog = false
            }
        )
    }

    val shareablePdfUri by projectViewModel.shareablePdfUri.collectAsState()

    LaunchedEffect(shareablePdfUri) {
        shareablePdfUri?.let { uri ->
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Share PDF"))
            projectViewModel.resetShareableUri()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.main_btn_preview_pdf)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    currentFile?.let {
                        IconButton(onClick = {
                            projectViewModel.createShareableUriForFile(context, it)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            val pdfSize by projectViewModel.pdfSize.collectAsState()
            if (pdfSize > 0) {
                Text(
                    text = "Tamaño del PDF: ${projectViewModel.getFormattedPdfSize()}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (currentFile != null && currentFile!!.exists()) {
                PdfView(
                    uri = Uri.fromFile(currentFile!!),
                    photoLayouts = photoLayouts,
                    onPhotoClick = { photoRect ->
                        firstPhotoToSwap = photoRect
                        showSwapDialog = true
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: PDF not found")
                }
            }
        }
    }
}


@Composable
fun PdfView(
    modifier: Modifier = Modifier,
    uri: Uri,
    photoLayouts: List<PhotoRect>,
    onPhotoClick: (PhotoRect) -> Unit
) {
    val context = LocalContext.current

    val rendererState by remember(uri) {
        mutableStateOf(
            try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                val renderer = pfd?.let { PdfRenderer(it) }
                RendererState(
                    renderer = renderer,
                    pageCount = renderer?.pageCount ?: 0,
                    pfd = pfd
                )
            } catch (e: Exception) {
                RendererState(null, 0, null)
            }
        )
    }

    DisposableEffect(rendererState) {
        onDispose {
            rendererState.renderer?.close()
            rendererState.pfd?.close()
        }
    }

    if (rendererState.renderer == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error opening PDF")
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = rendererState.pageCount) { index ->
            PdfPage(
                renderer = rendererState.renderer!!,
                pageIndex = index,
                photoRects = photoLayouts.filter { it.pageIndex == index },
                onPhotoClick = onPhotoClick
            )
            if (index < rendererState.pageCount - 1) {
                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun PdfPage(
    renderer: PdfRenderer,
    pageIndex: Int,
    photoRects: List<PhotoRect>,
    onPhotoClick: (PhotoRect) -> Unit
) {
    val density = LocalDensity.current.density
    var bitmap by remember(renderer, pageIndex) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = renderer, key2 = pageIndex) {
        isLoading = true
        val page = renderer.openPage(pageIndex)
        val newBitmap = Bitmap.createBitmap(
            (page.width * density).toInt(),
            (page.height * density).toInt(),
            Bitmap.Config.ARGB_8888
        )
        page.render(newBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmap = newBitmap
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(Color.LightGray)) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        bitmap?.let {
            Box(modifier = Modifier.fillMaxWidth()) {
                ZoomableImage(
                    bitmap = it.asImageBitmap(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                )

                // Overlays for clickable photos
                val imageWidth = with(LocalDensity.current) { it.width.toDp() }
                val scaleFactor = imageWidth / (renderer.openPage(pageIndex).use { p -> p.width.dp })

                photoRects.forEach { photoRect ->
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (photoRect.rect.left.dp * scaleFactor),
                                y = (photoRect.rect.top.dp * scaleFactor)
                            )
                            .size(
                                width = (photoRect.rect.width().dp * scaleFactor),
                                height = (photoRect.rect.height().dp * scaleFactor)
                            )
                    ) {
                        IconButton(
                            onClick = { onPhotoClick(photoRect) },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Intercambiar foto",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
