package com.example.dynamiccollage.data.model

import androidx.compose.ui.graphics.Color

data class BorderProperties(
    val color: Color = Color.Black,
    val thickness: Float = 1f, // in points
    val top: Boolean = false,
    val bottom: Boolean = false,
    val left: Boolean = false,
    val right: Boolean = false
)

data class RowStyle(
    val backgroundColor: Color = Color.Transparent,
    val padding: Float = 10f, // in points
    val border: BorderProperties = BorderProperties()
)
