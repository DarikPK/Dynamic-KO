package com.example.dynamiccollage.ui.components

import android.graphics.Bitmap
import android.graphics.Matrix
import coil.size.Size
import coil.transform.Transformation
import com.example.dynamiccollage.data.model.ImageEffectSettings
import com.example.dynamiccollage.utils.ImageEffects

class ProjectEffectsTransformation(
    private val settings: ImageEffectSettings
) : Transformation {

    override val cacheKey: String = "ProjectEffectsTransformation(settings=$settings)"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        var processedBitmap = input

        // 1. Apply Crop if specified
        settings.cropRect?.let { normalizedRect ->
            // Ensure rect values are valid percentages
            if (normalizedRect.width > 0 && normalizedRect.height > 0) {
                val left = (normalizedRect.left * processedBitmap.width).toInt()
                val top = (normalizedRect.top * processedBitmap.height).toInt()
                val width = (normalizedRect.width * processedBitmap.width).toInt()
                val height = (normalizedRect.height * processedBitmap.height).toInt()

                // Check if calculated dimensions are valid and within bounds
                if (width > 0 && height > 0 && (left + width) <= processedBitmap.width && (top + height) <= processedBitmap.height) {
                    processedBitmap = Bitmap.createBitmap(
                        processedBitmap,
                        left,
                        top,
                        width,
                        height
                    )
                }
            }
        }

        // 2. Apply Rotation if specified
        if (settings.rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(settings.rotationDegrees) }
            processedBitmap = Bitmap.createBitmap(
                processedBitmap, 0, 0, processedBitmap.width, processedBitmap.height, matrix, true
            )
        }

        // 3. Apply ColorMatrix effects
        val contrast = 1.0f + settings.contrast / 100.0f
        val saturation = 1.0f + settings.saturation / 100.0f
        processedBitmap = ImageEffects.applyEffects(processedBitmap, settings.brightness, contrast, saturation)

        // 4. Apply sharpness or blur
        val sharpness = settings.sharpness / 100.0f
        if (sharpness > 0) {
            processedBitmap = ImageEffects.applySharpen(processedBitmap, sharpness)
        } else if (sharpness < 0) {
            processedBitmap = ImageEffects.applyBlur(processedBitmap, -sharpness)
        }

        return processedBitmap
    }
}
