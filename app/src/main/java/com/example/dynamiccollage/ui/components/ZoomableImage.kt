package com.example.dynamiccollage.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun ZoomableImage(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

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
                forEachGesture {
                    awaitPointerEventScope {
                        var isTransforming = false
                        var tapHandled = false

                        // Wait for a first pointer down
                        val down = awaitFirstDown()

                        // Await a second pointer down for multi-touch gestures, or a timeout for single-touch
                        val secondDown = withTimeoutOrNull(200) { awaitPointerEvent() }

                        if (secondDown == null) {
                            // Single pointer gesture (tap or drag)
                            // Check for double tap
                            val secondTap = withTimeoutOrNull(250) { awaitFirstDown(requireUnconsumed = false) }
                            if (secondTap != null) {
                                scope.launch {
                                    val newScale = if (scale > 1f) 1f else 2.5f
                                    val oldScale = scale
                                    scale = newScale
                                    offset = (offset - down.position) * (newScale / oldScale) + down.position
                                    applyOffsetBounds()
                                }
                                tapHandled = true
                                secondTap.consume()
                            }
                        }

                        if (!tapHandled) {
                            // Main gesture loop
                            do {
                                val event = awaitPointerEvent()
                                val pointers = event.changes

                                val pan = pointers.calculatePan()

                                if (pointers.size > 1) {
                                    isTransforming = true
                                } else if (scale > 1f && pan.getDistance() > 0.1f) {
                                    isTransforming = true
                                }

                                if (isTransforming) {
                                    val zoom = pointers.calculateZoom()
                                    val centroid = pointers.calculateCentroid()
                                    val oldScale = scale
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    offset = (offset + centroid - centroid * (scale / oldScale)) + pan
                                    applyOffsetBounds()

                                    pointers.forEach {
                                        if (it.positionChanged()) {
                                            it.consume()
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
    )
}

private fun List<PointerInputChange>.calculateZoom(): Float {
    if (size < 2) return 1f
    val p1 = this[0]
    val p2 = this[1]
    val oldDist = (p1.previousPosition - p2.previousPosition).getDistance()
    val newDist = (p1.position - p2.position).getDistance()
    return if (oldDist == 0f) 1f else newDist / oldDist
}

private fun List<PointerInputChange>.calculatePan(): Offset {
    if (isEmpty()) return Offset.Zero
    val sum = fold(Offset.Zero) { acc, pointerInputChange -> acc + (pointerInputChange.position - pointerInputChange.previousPosition) }
    return sum / size.toFloat()
}

private fun List<PointerInputChange>.calculateCentroid(): Offset {
    if (isEmpty()) return Offset.Zero
    val sum = fold(Offset.Zero) { acc, pointerInputChange -> acc + pointerInputChange.position }
    return sum / size.toFloat()
}

private fun Offset.getDistance(): Float = sqrt(x * x + y * y)
