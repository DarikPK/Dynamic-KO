package com.example.dynamiccollage.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sign
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

enum class SlideState {
    NONE,
    UP,
    DOWN
}

private const val NO_ITEMS_MOVED = 0

private fun calculateNumberOfSlidItems(
    offsetY: Float,
    itemHeight: Int,
    offsetToSlide: Int,
    previousNumberOfItems: Int
): Int {
    val numberOfItemsInOffset = (offsetY / itemHeight).toInt()
    val numberOfItemsPlusOffset = ((offsetY + offsetToSlide) / itemHeight).toInt()
    val numberOfItemsMinusOffset = ((offsetY - offsetToSlide - 1) / itemHeight).toInt()

    return when {
        offsetY - offsetToSlide - 1 < 0 -> NO_ITEMS_MOVED
        numberOfItemsPlusOffset > numberOfItemsInOffset -> numberOfItemsPlusOffset
        numberOfItemsMinusOffset < numberOfItemsInOffset -> numberOfItemsInOffset
        else -> previousNumberOfItems
    }
}

fun <T> Modifier.dragToReorder(
    item: T,
    itemList: List<T>,
    itemHeight: Int,
    updateSlideState: (item: T, slideState: SlideState) -> Unit,
    onStartDrag: (currIndex: Int) -> Unit = {},
    onStopDrag: (currIndex: Int, destIndex: Int) -> Unit
): Modifier = composed {
    val offsetY = remember { Animatable(0f) }

    pointerInput(Unit) {
        coroutineScope {
            val itemIndex = itemList.indexOf(item)
            val offsetToSlide = itemHeight / 2
            var numberOfSlidItems = 0
            var previousNumberOfItems: Int
            var listOffset = 0

            val onDragStart = {
                launch {
                    offsetY.stop()
                }
                onStartDrag(itemIndex)
            }

            val onDragging = { change: androidx.compose.ui.input.pointer.PointerInputChange ->
                val verticalDragOffset = offsetY.value + change.positionChange().y
                launch {
                    offsetY.snapTo(verticalDragOffset)
                    val offsetSign = offsetY.value.sign.toInt()
                    previousNumberOfItems = numberOfSlidItems
                    numberOfSlidItems = calculateNumberOfSlidItems(
                        offsetY.value * offsetSign,
                        itemHeight,
                        offsetToSlide,
                        previousNumberOfItems
                    )

                    if (previousNumberOfItems > numberOfSlidItems) {
                        updateSlideState(
                            itemList[itemIndex + previousNumberOfItems * offsetSign],
                            SlideState.NONE
                        )
                    } else if (numberOfSlidItems != 0) {
                        try {
                            updateSlideState(
                                itemList[itemIndex + numberOfSlidItems * offsetSign],
                                if (offsetSign == 1) SlideState.UP else SlideState.DOWN
                            )
                        } catch (e: IndexOutOfBoundsException) {
                            numberOfSlidItems = previousNumberOfItems
                        }
                    }
                    listOffset = numberOfSlidItems * offsetSign
                }
                if (change.positionChange() != androidx.compose.ui.geometry.Offset.Zero) change.consume()
            }

            val onDragEnd = {
                launch {
                    offsetY.animateTo(itemHeight * numberOfSlidItems * offsetY.value.sign)
                    onStopDrag(itemIndex, itemIndex + listOffset)
                }
            }

            detectDragGesturesAfterLongPress(
                onDragStart = { onDragStart() },
                onDrag = { change, _ -> onDragging(change) },
                onDragEnd = { onDragEnd() }
            )
        }
    }.offset {
        IntOffset(0, offsetY.value.roundToInt())
    }
}
