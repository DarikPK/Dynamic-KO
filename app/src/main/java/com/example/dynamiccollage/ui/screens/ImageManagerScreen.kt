package com.example.dynamiccollage.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dynamiccollage.ui.components.CropView
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.math.min
import android.graphics.BitmapFactory



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val imageUris by remember { mutableStateOf(projectViewModel.getAllImageUris()) }
    var currentSelectedUri by remember { mutableStateOf(imageUris.firstOrNull()?.let { Uri.parse(it) }) }
    var uriBeforeCrop by remember { mutableStateOf(currentSelectedUri) }
    var originalUriOfSession by remember { mutableStateOf(currentSelectedUri) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Imagen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val uriToRevertTo = uriBeforeCrop
                            if (uriToRevertTo != null && currentSelectedUri != null) {
                                projectViewModel.replaceImageUri(context, currentSelectedUri.toString(), uriToRevertTo.toString())
                                currentSelectedUri = uriToRevertTo
                            }
                        },
                        enabled = currentSelectedUri != uriBeforeCrop
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Deshacer Recorte")
                    }
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val oldUri = currentSelectedUri
                                if (oldUri != null) {
                                    val newUriString = projectViewModel.rotateImage(context, oldUri.toString())
                                    if (newUriString != null) {
                                        uriBeforeCrop = oldUri
                                        currentSelectedUri = Uri.parse(newUriString)
                                    }
                                }
                            }
                        },
                        enabled = currentSelectedUri != null
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Girar")
                    }
                    IconButton(
                        onClick = {
                            currentSelectedUri?.let {
                                navController.navigate("image_effects_screen/${Uri.encode(it.toString())}")
                            }
                        },
                        enabled = currentSelectedUri != null
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Efectos")
                    }
                    IconButton(
                        onClick = {
                             if (originalUriOfSession != null && currentSelectedUri != null) {
                                projectViewModel.replaceImageUri(context, currentSelectedUri.toString(), originalUriOfSession.toString())
                                currentSelectedUri = originalUriOfSession
                                uriBeforeCrop = originalUriOfSession
                            }
                        },
                        enabled = currentSelectedUri != originalUriOfSession
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = "Restablecer Original")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (currentSelectedUri != null) {
                    key(currentSelectedUri) {
                        CropView(
                            uri = currentSelectedUri!!,
                            onCrop = { cropRect, imageBounds ->
                                coroutineScope.launch {
                                val croppedBitmap = cropBitmap(
                                    context = context,
                                    uri = currentSelectedUri!!,
                                    cropRect = cropRect,
                                    imageBounds = imageBounds
                                )
                                if (croppedBitmap != null) {
                                    val oldUri = currentSelectedUri
                                    val newUriString = projectViewModel.saveCroppedImage(context, oldUri.toString(), croppedBitmap)
                                    if (newUriString != null) {
                                        uriBeforeCrop = oldUri
                                        currentSelectedUri = Uri.parse(newUriString)
                                    }
                                }
                            }
                        })
                    }
                } else {
                    Text("No hay imágenes para editar.")
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageUris) { uriString ->
                    val uri = Uri.parse(uriString)
                    AsyncImage(
                        model = uri,
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(84.dp)
                            .border(
                                width = 2.dp,
                                color = if (uri == currentSelectedUri) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable {
                                currentSelectedUri = uri
                                uriBeforeCrop = uri
                                originalUriOfSession = uri
                            }
                    )
                }
            }
        }
    }
}

private fun cropBitmap(
    context: Context,
    uri: Uri,
    cropRect: Rect,
    imageBounds: Rect
): Bitmap? {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream == null) return null

        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val scale = originalBitmap.width.toFloat() / imageBounds.width

        val finalLeft = (cropRect.left - imageBounds.left) * scale
        val finalTop = (cropRect.top - imageBounds.top) * scale
        val finalWidth = cropRect.width * scale
        val finalHeight = cropRect.height * scale

        if (finalWidth <= 0 || finalHeight <= 0) return null

        return Bitmap.createBitmap(
            originalBitmap,
            finalLeft.toInt(),
            finalTop.toInt(),
            finalWidth.toInt(),
            finalHeight.toInt()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
