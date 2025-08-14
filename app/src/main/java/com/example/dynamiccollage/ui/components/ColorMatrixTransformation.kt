package com.example.dynamiccollage.ui.components

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation
import com.example.dynamiccollage.utils.ImageEffects

class ColorMatrixTransformation(
    private val brightness: Float,
    private val contrast: Float,
    private val saturation: Float
) : Transformation {

    override val cacheKey: String = "${javaClass.name}-$brightness-$contrast-$saturation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        // Only apply effects if they are not the default values
        if (brightness == 0f && contrast == 1f && saturation == 1f) {
            return input
        }
        return ImageEffects.applyEffects(input, brightness, contrast, saturation)
    }
}
