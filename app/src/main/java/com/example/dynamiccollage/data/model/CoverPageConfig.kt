package com.example.dynamiccollage.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Valores por defecto para la configuración de la portada

data class CoverConfig(
    // ...otras propiedades...
    val pageOrientation: PageOrientation = PageOrientation.Vertical,
)