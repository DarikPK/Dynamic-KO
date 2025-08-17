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
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

@Composable
fun ZoomableImage(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // We get the size of the bitmap to constrain the panning
    val size = IntSize(bitmap.width, bitmap.height)

    val gestureModifier = modifier
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = {
                    scope.launch {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                }
            )
        }
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = (scale * zoom).coerceIn(1f, 5f)

                val maxOffsetX = (size.width * (scale - 1)) / 2f
                val maxOffsetY = (size.height * (scale - 1)) / 2f

                val newOffsetX = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                val newOffsetY = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)

                if (scale == 1f) {
                    offset = Offset.Zero
                } else {
                    offset = Offset(newOffsetX, newOffsetY)
                }
            }
        }

    Image(
        bitmap = bitmap,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = gestureModifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
    )
}
