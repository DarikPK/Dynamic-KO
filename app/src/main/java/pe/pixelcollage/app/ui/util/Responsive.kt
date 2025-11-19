
package pe.pixelcollage.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Clasificación de Pantallas
enum class ScreenWidthClass { Small, Medium, Large }
enum class ScreenHeightClass { Short, Normal, Tall }

// Clase para almacenar las dimensiones calculadas
data class ResponsiveDimensions(
    val screenWidthClass: ScreenWidthClass,
    val screenHeightClass: ScreenHeightClass,
    val typographyScale: Float,
    val buttonMinHeight: Dp,
    val buttonWidthPercent: Float,
    val buttonInternalPadding: Dp,
    val buttonIconSize: Dp,
    val buttonIconSpacing: Dp,
    val verticalSpacing: Dp,
    val cardPadding: Dp,
    val topBarHeight: Dp,
    val topBarIconSize: Dp,
    val externalMargin: Dp
)

// CompositionLocal para acceder a las dimensiones en cualquier Composable
val LocalResponsiveDimensions = compositionLocalOf<ResponsiveDimensions> { error("No responsive dimensions provided") }

@Composable
fun ProvideResponsiveDimensions(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    val dimensions = remember(screenWidthDp, screenHeightDp) {
        val widthClass = when {
            screenWidthDp < 360.dp -> ScreenWidthClass.Small
            screenWidthDp <= 420.dp -> ScreenWidthClass.Medium
            else -> ScreenWidthClass.Large
        }

        val heightClass = when {
            screenHeightDp < 640.dp -> ScreenHeightClass.Short
            screenHeightDp <= 880.dp -> ScreenHeightClass.Normal
            else -> ScreenHeightClass.Tall
        }

        var typoScale = 1.0f
        if (widthClass == ScreenWidthClass.Small) typoScale *= 0.85f
        if (heightClass == ScreenHeightClass.Short) typoScale *= 0.90f
        if (widthClass == ScreenWidthClass.Large || heightClass == ScreenHeightClass.Tall) typoScale *= 1.05f

        // Reglas para pantallas muy pequeñas
        if (screenWidthDp <= 320.dp) {
            typoScale *= 0.80f // 20% adicional
        }

        val buttonHeight = when {
            widthClass == ScreenWidthClass.Small && heightClass == ScreenHeightClass.Short -> 40.dp
            widthClass == ScreenWidthClass.Small && heightClass != ScreenHeightClass.Short -> 44.dp
            widthClass == ScreenWidthClass.Medium -> 48.dp
            else -> 52.dp // Large
        }.let { if (screenWidthDp <= 320.dp) 38.dp else it }

        ResponsiveDimensions(
            screenWidthClass = widthClass,
            screenHeightClass = heightClass,
            typographyScale = typoScale,
            buttonMinHeight = buttonHeight,
            buttonWidthPercent = when (widthClass) {
                ScreenWidthClass.Small -> 0.90f
                ScreenWidthClass.Medium -> 0.82f
                ScreenWidthClass.Large -> 0.75f
            },
            buttonInternalPadding = when (widthClass) {
                ScreenWidthClass.Small -> 8.dp
                ScreenWidthClass.Medium -> 10.dp
                ScreenWidthClass.Large -> 12.dp
            },
            buttonIconSize = when (widthClass) {
                ScreenWidthClass.Small -> 18.dp
                ScreenWidthClass.Medium -> 20.dp
                ScreenWidthClass.Large -> 22.dp
            },
            buttonIconSpacing = when (widthClass) {
                ScreenWidthClass.Small -> 6.dp
                ScreenWidthClass.Medium -> 8.dp
                ScreenWidthClass.Large -> 10.dp
            },
            verticalSpacing = when (heightClass) {
                ScreenHeightClass.Short -> 8.dp
                ScreenHeightClass.Normal -> 12.dp
                ScreenHeightClass.Tall -> 16.dp
            },
            cardPadding = when {
                widthClass == ScreenWidthClass.Small && heightClass == ScreenHeightClass.Short -> 8.dp
                widthClass == ScreenWidthClass.Small -> 10.dp
                widthClass == ScreenWidthClass.Medium -> 12.dp
                else -> 16.dp
            },
            topBarHeight = when (widthClass) {
                ScreenWidthClass.Small -> 48.dp
                ScreenWidthClass.Medium -> 56.dp
                ScreenWidthClass.Large -> 64.dp
            },
            topBarIconSize = when (widthClass) {
                ScreenWidthClass.Small -> 18.dp
                ScreenWidthClass.Medium -> 20.dp
                ScreenWidthClass.Large -> 24.dp
            },
            externalMargin = when (widthClass) {
                ScreenWidthClass.Small -> 10.dp
                ScreenWidthClass.Medium -> 14.dp
                ScreenWidthClass.Large -> 20.dp
            }.let { if (screenWidthDp <= 320.dp) it * 0.8f else it }
        )
    }

    CompositionLocalProvider(LocalResponsiveDimensions provides dimensions) {
        content()
    }
}

// Helpers para fácil acceso
object Responsive {
    val dimensions: ResponsiveDimensions
        @Composable
        get() = LocalResponsiveDimensions.current
}

// Extension para escalar tipografía
@Composable
fun scaledSp(size: Int) = (size * Responsive.dimensions.typographyScale).sp
