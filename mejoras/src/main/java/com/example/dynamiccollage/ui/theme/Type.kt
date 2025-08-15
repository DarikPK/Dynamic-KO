package com.example.dynamiccollage.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.dynamiccollage.R

// Definición de la familia de fuentes Calibri
val calibriFontFamily = FontFamily(
    Font(R.font.calibri_regular, FontWeight.Normal),
    Font(R.font.calibri_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.calibri_bold, FontWeight.Bold),
    Font(R.font.calibri_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

// Reemplazar con las familias de fuentes deseadas si se tienen fuentes personalizadas.
// Por ahora, usaremos las fuentes predeterminadas de Material Design.
val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    /* Otros estilos de texto predeterminados que puedes sobrescribir:
    bodyLarge, bodyMedium, bodySmall,
    labelLarge, labelMedium, labelSmall,
    titleLarge, titleMedium, titleSmall,
    headlineLarge, headlineMedium, headlineSmall,
    displayLarge, displayMedium, displaySmall
    */
)
