package com.example.dynamiccollage.data.model

/**
 * A serializable representation of a normalized rectangle.
 * All values are floats from 0.0 to 1.0, representing percentages.
 */
data class SerializableNormalizedRectF(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

data class ImageEffectSettings(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val sharpness: Float = 0f,
    val rotationDegrees: Float = 0f,
    val cropRect: SerializableNormalizedRectF? = null
) {
    fun hasTransforms(): Boolean {
        return rotationDegrees != 0f || cropRect != null
    }

    fun isUnchanged(): Boolean {
        return brightness == 0f &&
                contrast == 0f &&
                saturation == 0f &&
                sharpness == 0f &&
                !hasTransforms()
    }
}
