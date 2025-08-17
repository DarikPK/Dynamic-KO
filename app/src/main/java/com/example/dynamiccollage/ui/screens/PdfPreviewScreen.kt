package com.example.dynamiccollage.ui.screens


import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import com.example.dynamiccollage.ui.components.ZoomableImage
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
    navController: NavController,
    pdfPath: String?,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val file = pdfPath?.let { File(it) }
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
                    if (file != null) {
                        IconButton(onClick = {
                            projectViewModel.createShareableUriForFile(context, file)
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
            if (file != null && file.exists()) {
                PdfView(uri = Uri.fromFile(file))
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
fun PdfView(modifier: Modifier = Modifier, uri: Uri) {
    val context = LocalContext.current

    val rendererState by remember(uri) {
        mutableStateOf(
            try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                val renderer = pfd?.let { PdfRenderer(it) }
                object {
                    val renderer = renderer
                    val pageCount = renderer?.pageCount ?: 0
                    val pfd = pfd
                }
            } catch (e: Exception) {
                object {
                    val renderer = null
                    val pageCount = 0
                    val pfd = null
                }
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

    val bitmaps = remember { mutableStateListOf<Bitmap>() }
    val density = LocalDensity.current.density
    val zoomedStates = remember { mutableStateMapOf<Int, Boolean>() }
    val isAnyImageZoomed = zoomedStates.values.any { it }

    LaunchedEffect(rendererState) {
        bitmaps.clear()
        zoomedStates.clear()
        val renderer = rendererState.renderer ?: return@LaunchedEffect
        for (i in 0 until rendererState.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(
                (page.width * density).toInt(),
                (page.height * density).toInt(),
                Bitmap.Config.ARGB_8888
            )
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bitmap)
            zoomedStates[i] = false
            page.close()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = !isAnyImageZoomed
    ) {
        itemsIndexed(bitmaps) { index, bitmap ->
            ZoomableImage(
                bitmap = bitmap.asImageBitmap(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                onGesture = { isZoomed ->
                    zoomedStates[index] = isZoomed
                }
            )
            if (index < bitmaps.size - 1) {
                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
