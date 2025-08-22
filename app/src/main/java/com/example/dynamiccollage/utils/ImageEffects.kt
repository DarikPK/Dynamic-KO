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

    /**
     * Applies a sharpen effect to a bitmap using a convolution matrix.
     * @param bitmap The original bitmap.
     * @param strength The strength of the sharpen effect. 0.0 means no change, 1.0 is full sharpen.
     * @return A new bitmap with the sharpen effect applied.
     */
    fun applySharpen(bitmap: Bitmap, strength: Float): Bitmap {
        if (strength <= 0f) return bitmap

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val newPixels = IntArray(width * height)

        // Sharpen kernel
        val kernel = floatArrayOf(
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        )

        for (i in 1 until height - 1) {
            for (j in 1 until width - 1) {
                var sumR = 0f
                var sumG = 0f
                var sumB = 0f

                // Apply kernel
                for (k in -1..1) {
                    for (l in -1..1) {
                        val pixel = pixels[(i + k) * width + (j + l)]
                        val kernelValue = kernel[(k + 1) * 3 + (l + 1)]

                        sumR += ((pixel shr 16) and 0xFF) * kernelValue
                        sumG += ((pixel shr 8) and 0xFF) * kernelValue
                        sumB += (pixel and 0xFF) * kernelValue
                    }
                }

                val originalPixel = pixels[i * width + j]
                val originalR = (originalPixel shr 16) and 0xFF
                val originalG = (originalPixel shr 8) and 0xFF
                val originalB = originalPixel and 0xFF

                // Clamp values
                val r = sumR.toInt().coerceIn(0, 255)
                val g = sumG.toInt().coerceIn(0, 255)
                val b = sumB.toInt().coerceIn(0, 255)

                // Blend with original pixel based on strength
                val finalR = (originalR * (1 - strength) + r * strength).toInt()
                val finalG = (originalG * (1 - strength) + g * strength).toInt()
                val finalB = (originalB * (1 - strength) + b * strength).toInt()

                newPixels[i * width + j] = (originalPixel and 0xFF000000.toInt()) or (finalR shl 16) or (finalG shl 8) or finalB
            }
        }

        // Handle edges by copying them from the original
        for (i in 0 until width) {
            newPixels[i] = pixels[i]
            newPixels[(height - 1) * width + i] = pixels[(height - 1) * width + i]
        }
        for (i in 0 until height) {
            newPixels[i * width] = pixels[i * width]
            newPixels[i * width + width - 1] = pixels[i * width + width - 1]
        }


        val newBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        newBitmap.setPixels(newPixels, 0, width, 0, 0, width, height)
        return newBitmap
    }
}
