package com.example.dynamiccollage.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dynamiccollage.ui.components.CropView
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import java.io.InputStream
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val imageUris by remember { mutableStateOf(projectViewModel.getAllImageUris()) }
    var selectedImageUri by remember { mutableStateOf(imageUris.firstOrNull()?.let { Uri.parse(it) }) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Imagen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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
                if (selectedImageUri != null) {
                    CropView(
                        uri = selectedImageUri!!,
                        onCrop = { cropRect, canvasSize ->
                            val croppedBitmap = cropBitmap(
                                context = context,
                                uri = selectedImageUri!!,
                                cropRect = cropRect,
                                canvasSize = canvasSize
                            )
                            if (croppedBitmap != null) {
                                // TODO: PHASE 4
                                projectViewModel.saveCroppedImage(context, selectedImageUri.toString(), croppedBitmap)
                                navController.popBackStack()
                            }
                        }
                    )
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
                                color = if (uri == selectedImageUri) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable { selectedImageUri = uri }
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
    canvasSize: IntSize
): Bitmap? {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream == null) return null

        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val (bitmapWidth, bitmapHeight) = originalBitmap.width to originalBitmap.height
        val (canvasWidth, canvasHeight) = canvasSize.width to canvasSize.height

        if (bitmapWidth == 0 || bitmapHeight == 0 || canvasWidth == 0 || canvasHeight == 0) return null

        val scaleX = canvasWidth.toFloat() / bitmapWidth
        val scaleY = canvasHeight.toFloat() / bitmapHeight
        val scale = min(scaleX, scaleY)

        if (scale <= 0f) return null

        val displayedWidth = bitmapWidth * scale
        val displayedHeight = bitmapHeight * scale

        val offsetX = (canvasWidth - displayedWidth) / 2
        val offsetY = (canvasHeight - displayedHeight) / 2

        val translatedRect = cropRect.translate(-offsetX, -offsetY)

        val finalCropRect = Rect(
            left = translatedRect.left / scale,
            top = translatedRect.top / scale,
            right = translatedRect.right / scale,
            bottom = translatedRect.bottom / scale
        )

        val boundedRect = finalCropRect.intersect(Rect(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat()))

        if (boundedRect.isEmpty || boundedRect.width <= 0 || boundedRect.height <= 0) return null

        val x = boundedRect.left.toInt()
        val y = boundedRect.top.toInt()
        val width = boundedRect.width.toInt()
        val height = boundedRect.height.toInt()

        if (x < 0 || y < 0 || x + width > originalBitmap.width || y + height > originalBitmap.height) {
            return null // Final check to prevent crash
        }

        return Bitmap.createBitmap(originalBitmap, x, y, width, height)
    } catch (e: Exception) {
        // Log the exception in a real app
        e.printStackTrace()
        return null
    }
}
