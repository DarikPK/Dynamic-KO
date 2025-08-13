package com.example.dynamiccollage.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.abs

private sealed class TouchRegion {
    object Center : TouchRegion()
    object TopLeft : TouchRegion()
    object TopRight : TouchRegion()
    object BottomLeft : TouchRegion()
    object BottomRight : TouchRegion()
    object Left : TouchRegion()
    object Right : TouchRegion()
    object Top : TouchRegion()
    object Bottom : TouchRegion()
    object None : TouchRegion()
}

@Composable
fun CropView(
    modifier: Modifier = Modifier,
    uri: Uri,
    onCrop: (cropRect: Rect, imageBounds: Rect) -> Unit
) {
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isInitialized by remember { mutableStateOf(false) }
    var touchRegion by remember { mutableStateOf<TouchRegion>(TouchRegion.None) }
    var imageBounds by remember { mutableStateOf(Rect.Zero) }
    var imageAspectRatio by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(uri).build(),
                contentDescription = "Image to crop",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { result ->
                    val drawable = result.result.drawable
                    imageAspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                    isInitialized = false // Force re-initialization when image changes
                }
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(uri) { // Re-trigger pointer input when URI changes
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                val touchSlop = with(density) { 24.dp.toPx() }
                                touchRegion = getTouchRegion(startOffset, cropRect, touchSlop)
                            },
                            onDragEnd = {
                                cropRect = getUpdatedRect(cropRect, dragOffset, touchRegion, imageBounds)
                                dragOffset = Offset.Zero
                                touchRegion = TouchRegion.None
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        }
                    }
            ) {
                if (imageAspectRatio <= 0f || size.width == 0f || size.height == 0f) return@Canvas

                imageBounds = getImageBounds(imageAspectRatio, size)
                if (imageBounds.isEmpty) return@Canvas

                if (!isInitialized) {
                    cropRect = imageBounds
                    isInitialized = true
                }

                val displayedRect = getUpdatedRect(cropRect, dragOffset, touchRegion, imageBounds)

                val outerPath = Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) }
                val innerPath = Path().apply { addRect(displayedRect) }
                val path = Path.combine(
                    operation = PathOperation.Difference,
                    path1 = outerPath,
                    path2 = innerPath
                )

                drawPath(path = path, color = Color.Black.copy(alpha = 0.5f))
                drawRect(
                    color = Color.White,
                    topLeft = displayedRect.topLeft,
                    size = displayedRect.size,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Corner handles
                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = displayedRect.topLeft)
                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = displayedRect.topRight)
                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = displayedRect.bottomLeft)
                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = displayedRect.bottomRight)
                // Side handles
                val handleSize = Size(20.dp.toPx(), 8.dp.toPx())
                drawRect(color = Color.White, topLeft = Offset(displayedRect.center.x - handleSize.width / 2, displayedRect.top - handleSize.height / 2), size = handleSize)
                drawRect(color = Color.White, topLeft = Offset(displayedRect.center.x - handleSize.width / 2, displayedRect.bottom - handleSize.height / 2), size = handleSize)
                drawRect(color = Color.White, topLeft = Offset(displayedRect.left - handleSize.height / 2, displayedRect.center.y - handleSize.width / 2), size = handleSize.copy(width = handleSize.height, height = handleSize.width))
                drawRect(color = Color.White, topLeft = Offset(displayedRect.right - handleSize.height / 2, displayedRect.center.y - handleSize.width / 2), size = handleSize.copy(width = handleSize.height, height = handleSize.width))
            }
        }
        Button(
            onClick = { onCrop(getUpdatedRect(cropRect, dragOffset, touchRegion, imageBounds), imageBounds) },
            enabled = getUpdatedRect(cropRect, dragOffset, touchRegion, imageBounds) != imageBounds,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Recortar")
        }
    }
}

private fun getImageBounds(imageAspectRatio: Float, canvasSize: androidx.compose.ui.geometry.Size): Rect {
    if (imageAspectRatio <= 0f || canvasSize.height == 0f) return Rect.Zero
    val canvasAspectRatio = canvasSize.width / canvasSize.height
    return if (imageAspectRatio > canvasAspectRatio) {
        val scaledHeight = canvasSize.width / imageAspectRatio
        val offsetY = (canvasSize.height - scaledHeight) / 2
        Rect(0f, offsetY, canvasSize.width, offsetY + scaledHeight)
    } else {
        val scaledWidth = canvasSize.height * imageAspectRatio
        val offsetX = (canvasSize.width - scaledWidth) / 2
        Rect(offsetX, 0f, offsetX + scaledWidth, canvasSize.height)
    }
}

private fun getTouchRegion(offset: Offset, rect: Rect, slop: Float): TouchRegion {
    return when {
        abs(offset.x - rect.left) < slop && abs(offset.y - rect.top) < slop -> TouchRegion.TopLeft
        abs(offset.x - rect.right) < slop && abs(offset.y - rect.top) < slop -> TouchRegion.TopRight
        abs(offset.x - rect.left) < slop && abs(offset.y - rect.bottom) < slop -> TouchRegion.BottomLeft
        abs(offset.x - rect.right) < slop && abs(offset.y - rect.bottom) < slop -> TouchRegion.BottomRight
        abs(offset.x - rect.left) < slop -> TouchRegion.Left
        abs(offset.x - rect.right) < slop -> TouchRegion.Right
        abs(offset.y - rect.top) < slop -> TouchRegion.Top
        abs(offset.y - rect.bottom) < slop -> TouchRegion.Bottom
        rect.contains(offset) -> TouchRegion.Center
        else -> TouchRegion.None
    }
}

private fun getUpdatedRect(
    currentRect: Rect,
    dragOffset: Offset,
    touchRegion: TouchRegion,
    bounds: Rect
): Rect {
    val newRect = when (touchRegion) {
        TouchRegion.Center -> currentRect.translate(dragOffset)
        TouchRegion.TopLeft -> currentRect.copy(left = currentRect.left + dragOffset.x, top = currentRect.top + dragOffset.y)
        TouchRegion.TopRight -> currentRect.copy(right = currentRect.right + dragOffset.x, top = currentRect.top + dragOffset.y)
        TouchRegion.BottomLeft -> currentRect.copy(left = currentRect.left + dragOffset.x, bottom = currentRect.bottom + dragOffset.y)
        TouchRegion.BottomRight -> currentRect.copy(right = currentRect.right + dragOffset.x, bottom = currentRect.bottom + dragOffset.y)
        TouchRegion.Left -> currentRect.copy(left = currentRect.left + dragOffset.x)
        TouchRegion.Right -> currentRect.copy(right = currentRect.right + dragOffset.x)
        TouchRegion.Top -> currentRect.copy(top = currentRect.top + dragOffset.y)
        TouchRegion.Bottom -> currentRect.copy(bottom = currentRect.bottom + dragOffset.y)
        TouchRegion.None -> currentRect
    }

    // Constrain the new rectangle to the image bounds
    val constrainedLeft = newRect.left.coerceIn(bounds.left, bounds.right - 20f)
    val constrainedTop = newRect.top.coerceIn(bounds.top, bounds.bottom - 20f)
    val constrainedRight = newRect.right.coerceIn(constrainedLeft + 20f, bounds.right)
    val constrainedBottom = newRect.bottom.coerceIn(constrainedTop + 20f, bounds.bottom)

    return Rect(constrainedLeft, constrainedTop, constrainedRight, constrainedBottom)
}
