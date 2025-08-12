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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
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
    object None : TouchRegion()
}

@Composable
fun CropView(
    modifier: Modifier = Modifier,
    uri: Uri,
    onCrop: (Rect, IntSize) -> Unit
) {
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isInitialized by remember { mutableStateOf(false) }
    var touchRegion by remember { mutableStateOf<TouchRegion>(TouchRegion.None) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
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
                modifier = Modifier.fillMaxSize()
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                val touchSlop = with(density) { 24.dp.toPx() }
                                touchRegion = getTouchRegion(startOffset, cropRect, touchSlop)
                            },
                            onDragEnd = {
                                cropRect = getUpdatedRect(cropRect, dragOffset, touchRegion)
                                dragOffset = Offset.Zero
                                touchRegion = TouchRegion.None
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        }
                    }
            ) {
                canvasSize = IntSize(size.width.toInt(), size.height.toInt())

                if (!isInitialized) {
                    val rectSize = size.width * 0.8f
                    cropRect = Rect(
                        left = (size.width - rectSize) / 2,
                        top = (size.height - rectSize) / 2,
                        right = (size.width + rectSize) / 2,
                        bottom = (size.height + rectSize) / 2
                    )
                    isInitialized = true
                }

                val displayedRect = getUpdatedRect(cropRect, dragOffset, touchRegion)

                val outerPath = Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) }
                val innerPath = Path().apply { addRect(displayedRect) }
                val path = Path.combine(
                    operation = PathOperation.Difference,
                    path1 = outerPath,
                    path2 = innerPath
                )

                drawPath(path = path, color = Color.Black.copy(alpha = 0.5f))
                drawRect(
                    rect = displayedRect,
                    color = Color.White,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Draw corner handles
                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = displayedRect.topLeft)
                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = displayedRect.topRight)
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = displayedRect.bottomLeft
                )
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = displayedRect.bottomRight
                )
            }
        }
        Button(
            onClick = { onCrop(cropRect, canvasSize) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Aceptar")
        }
    }
}

private fun getTouchRegion(offset: Offset, rect: Rect, slop: Float): TouchRegion {
    return when {
        abs(offset.x - rect.topLeft.x) < slop && abs(offset.y - rect.topLeft.y) < slop -> TouchRegion.TopLeft
        abs(offset.x - rect.topRight.x) < slop && abs(offset.y - rect.topRight.y) < slop -> TouchRegion.TopRight
        abs(offset.x - rect.bottomLeft.x) < slop && abs(offset.y - rect.bottomLeft.y) < slop -> TouchRegion.BottomLeft
        abs(offset.x - rect.bottomRight.x) < slop && abs(offset.y - rect.bottomRight.y) < slop -> TouchRegion.BottomRight
        rect.contains(offset) -> TouchRegion.Center
        else -> TouchRegion.None
    }
}

private fun getUpdatedRect(
    currentRect: Rect,
    dragOffset: Offset,
    touchRegion: TouchRegion
): Rect {
    return when (touchRegion) {
        TouchRegion.Center -> currentRect.translate(dragOffset)
        TouchRegion.TopLeft -> Rect(
            left = currentRect.left + dragOffset.x,
            top = currentRect.top + dragOffset.y,
            right = currentRect.right,
            bottom = currentRect.bottom
        )
        TouchRegion.TopRight -> Rect(
            left = currentRect.left,
            top = currentRect.top + dragOffset.y,
            right = currentRect.right + dragOffset.x,
            bottom = currentRect.bottom
        )
        TouchRegion.BottomLeft -> Rect(
            left = currentRect.left + dragOffset.x,
            top = currentRect.top,
            right = currentRect.right,
            bottom = currentRect.bottom + dragOffset.y
        )
        TouchRegion.BottomRight -> Rect(
            left = currentRect.left,
            top = currentRect.top,
            right = currentRect.right + dragOffset.x,
            bottom = currentRect.bottom + dragOffset.y
        )
        TouchRegion.None -> currentRect
    }
}
