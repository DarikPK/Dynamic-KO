
package pe.pixelcollage.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

enum class ScreenWidthClass { Small, Medium, Large }

// Clases para almacenar las dimensiones calculadas
data class ResponsiveDimensions(
    val screenWidthClass: ScreenWidthClass,
    val typographyScale: Float,
    val topBarTypographyScale: Float,
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

    val dimensions = remember(screenWidthDp, screenHeightDp, configuration.densityDpi) {
        val widthClass = when {
            screenWidthDp < 360.dp -> ScreenWidthClass.Small
            screenWidthDp <= 420.dp -> ScreenWidthClass.Medium
            else -> ScreenWidthClass.Large
        }

        // Phone Factor basado en Pixel 9 Pro
        var phoneFactor = min(screenWidthDp.value / 411f, screenHeightDp.value / 891f)
        phoneFactor = max(0.90f, min(1.05f, phoneFactor))

        // Corrección por densidad para Huawei y similares
        val isDensityCorrectionNeeded = screenWidthDp <= 360.dp && configuration.densityDpi <= 330
        if (isDensityCorrectionNeeded) {
            phoneFactor = max(0.88f, phoneFactor - 0.05f)
        }

        // Escalado de tipografía (contenido)
        val typographyScale = phoneFactor

        // Escalado de tipografía (TopBar) - más suave
        val topBarTypographyScale = max(0.97f, min(1.03f, phoneFactor))

        val buttonInternalPadding = if (isDensityCorrectionNeeded) 8.dp else 12.dp

        ResponsiveDimensions(
            screenWidthClass = widthClass,
            typographyScale = typographyScale,
            topBarTypographyScale = topBarTypographyScale,
            buttonMinHeight = 48.dp,
            buttonWidthPercent = 0.9f,
            buttonInternalPadding = buttonInternalPadding,
            buttonIconSize = (24.dp * phoneFactor).coerceAtLeast(20.dp),
            buttonIconSpacing = 8.dp,
            verticalSpacing = if (isDensityCorrectionNeeded) 8.dp else 12.dp,
            cardPadding = if (isDensityCorrectionNeeded) 10.dp else 16.dp,
            topBarHeight = (64.dp * phoneFactor).coerceAtLeast(56.dp),
            topBarIconSize = (24.dp * phoneFactor).coerceAtLeast(20.dp),
            externalMargin = 16.dp
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

// Extension para escalar tipografía con límites
@Composable
fun scaledSp(size: TextUnit, minSize: TextUnit = 12.sp, isTopBar: Boolean = false): TextUnit {
    val scale = if (isTopBar) Responsive.dimensions.topBarTypographyScale else Responsive.dimensions.typographyScale
    val scaledSize = size * scale
    return max(scaledSize, minSize)
}
