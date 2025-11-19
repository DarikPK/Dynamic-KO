package pe.pixelcollage.app.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import pe.pixelcollage.app.R
import pe.pixelcollage.app.ui.components.ResponsiveTopAppBar
import pe.pixelcollage.app.ui.components.ZoomableImage
import pe.pixelcollage.app.ui.util.Responsive
import pe.pixelcollage.app.ui.util.scaledSp
import pe.pixelcollage.app.viewmodel.ProjectViewModel
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

    val dimensions = Responsive.dimensions

    Scaffold(
        topBar = {
            ResponsiveTopAppBar(
                title = { Text(stringResource(id = R.string.main_btn_preview_pdf), fontSize = scaledSp(22)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(dimensions.topBarIconSize)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(pe.pixelcollage.app.ui.navigation.Screen.PhotoSwap.route)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Ordenar Fotos",
                            modifier = Modifier.size(dimensions.topBarIconSize)
                        )
                    }
                    if (file != null) {
                        IconButton(onClick = {
                            projectViewModel.createShareableUriForFile(context, file)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(dimensions.topBarIconSize)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            val pdfSize by projectViewModel.pdfSize.collectAsState()
            if (pdfSize > 0) {
                Text(
                    text = "Tamaño del PDF: ${projectViewModel.getFormattedPdfSize()}",
                    modifier = Modifier.padding(dimensions.externalMargin),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = scaledSp(18)
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
                    Text("Error: PDF not found", fontSize = scaledSp(16))
                }
            }
        }
    }
}


@Composable
fun PdfView(modifier: Modifier = Modifier, uri: Uri) {
    val context = LocalContext.current
    val dimensions = Responsive.dimensions

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
            Text("Error opening PDF", fontSize = scaledSp(16))
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
                pageIndex = index
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
    pageIndex: Int
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
            ZoomableImage(
                bitmap = it.asImageBitmap(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            )
        }
    }
}
