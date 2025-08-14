package com.example.dynamiccollage.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object ImageEffects {

    /**
     * Applies brightness, contrast, and saturation effects to a bitmap.
     * @param bitmap The original bitmap.
     * @param brightness The brightness value. A good range is [-100, 100].
     * @param contrast The contrast value. 1.0 means no change. A good range is [0.5, 2.0].
     * @param saturation The saturation value. 1.0 means no change. 0.0 is grayscale. A good range is [0.0, 2.0].
     * @return A new bitmap with the effects applied.
     */
    fun applyEffects(
        bitmap: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): Bitmap {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(newBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix()

        // Note: The order of concatenation matters.
        // Saturation -> Contrast -> Brightness seems to be a standard order.

        // 1. Saturation
        val saturationMatrix = ColorMatrix().apply {
            setSaturation(saturation)
        }

        // 2. Contrast
        val contrastMatrix = ColorMatrix().apply {
            // Formula for contrast:
            // c = contrast value (e.g., 1.0 for no change)
            // t = (1.0 - c) * 127.5
            val t = (1.0f - contrast) * 127.5f
            set(floatArrayOf(
                contrast, 0f, 0f, 0f, t,
                0f, contrast, 0f, 0f, t,
                0f, 0f, contrast, 0f, t,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        // 3. Brightness
        val brightnessMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f, 0f, 0f, 0f, brightness,
                0f, 1f, 0f, 0f, brightness,
                0f, 0f, 1f, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        // Concatenate the matrices
        colorMatrix.postConcat(saturationMatrix)
        colorMatrix.postConcat(contrastMatrix)
        colorMatrix.postConcat(brightnessMatrix)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return newBitmap
    }
}
