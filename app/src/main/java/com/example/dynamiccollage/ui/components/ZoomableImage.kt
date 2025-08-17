package com.example.dynamiccollage.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

@Composable
fun ZoomableImage(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    onGesture: (Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    // Notify the parent about the zoom state
    LaunchedEffect(scale) {
        onGesture(scale > 1f)
    }

    fun applyOffsetBounds() {
        val imageWidth = viewSize.width * scale
        val imageHeight = viewSize.height * scale
        val maxOffsetX = (imageWidth - viewSize.width).coerceAtLeast(0f) / 2f
        val maxOffsetY = (imageHeight - viewSize.height).coerceAtLeast(0f) / 2f

        offset = Offset(
            x = offset.x.coerceIn(-maxOffsetX, maxOffsetX),
            y = offset.y.coerceIn(-maxOffsetY, maxOffsetY)
        )
        if (scale == 1f) {
            offset = Offset.Zero
        }
    }

    Image(
        bitmap = bitmap,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .onSizeChanged { viewSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    offset = (offset + centroid - centroid * (scale / oldScale)) + pan
                    applyOffsetBounds()
                }
            }
            .pointerInput(Unit) {
                 detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        scope.launch {
                            val newScale = if (scale > 1f) 1f else 2.5f
                            val oldScale = scale
                            scale = newScale
                            offset = (offset - tapOffset) * (newScale / oldScale) + tapOffset
                            applyOffsetBounds()
                        }
                    }
                )
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
    )
}
