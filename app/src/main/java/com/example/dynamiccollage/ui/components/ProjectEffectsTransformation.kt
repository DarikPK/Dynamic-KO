package com.example.dynamiccollage.ui.components

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation
import com.example.dynamiccollage.data.model.ImageEffectSettings
import com.example.dynamiccollage.utils.ImageEffects

class ProjectEffectsTransformation(
    private val settings: ImageEffectSettings
) : Transformation {

    // A unique key for caching purposes. Coil uses this to identify the transformation.
    override val cacheKey: String = "ProjectEffectsTransformation(settings=$settings)"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        // Apply the ColorMatrix effects first
        val contrast = 1.0f + settings.contrast / 100.0f
        val saturation = 1.0f + settings.saturation / 100.0f
        var processedBitmap = ImageEffects.applyEffects(input, settings.brightness, contrast, saturation)

        // Apply sharpness or blur
        val sharpness = settings.sharpness / 100.0f
        if (sharpness > 0) {
            processedBitmap = ImageEffects.applySharpen(processedBitmap, sharpness)
        } else if (sharpness < 0) {
            processedBitmap = ImageEffects.applyBlur(processedBitmap, -sharpness)
        }

        return processedBitmap
    }
}
