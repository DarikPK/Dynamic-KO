
package pe.pixelcollage.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.pixelcollage.app.ui.util.Responsive
import pe.pixelcollage.app.ui.util.scaledSp

@Composable
fun ResponsiveMainButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    buttonColor: Color? = null,
    textColor: Color? = null
) {
    val dimensions = Responsive.dimensions
    val colors = if (buttonColor != null) {
        ButtonDefaults.buttonColors(containerColor = buttonColor)
    } else {
        ButtonDefaults.buttonColors()
    }
    val textFinalColor = textColor ?: LocalContentColor.current

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(dimensions.buttonWidthPercent)
            .heightIn(min = dimensions.buttonMinHeight),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = dimensions.buttonInternalPadding),
        colors = colors
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimensions.buttonIconSize),
                tint = textFinalColor
            )
            Spacer(modifier = Modifier.width(dimensions.buttonIconSpacing))
        }
        Text(
            text = text,
            fontSize = scaledSp(16.sp),
            textAlign = TextAlign.Center,
            color = textFinalColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val dimensions = Responsive.dimensions

    CenterAlignedTopAppBar(
        title = {
            ProvideTextStyle(value = MaterialTheme.typography.titleLarge.copy(fontSize = scaledSp(20.sp, minSize = 14.sp, isTopBar = true))) {
                title()
            }
        },
        modifier = Modifier.height(dimensions.topBarHeight),
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
fun TrialModeBanner(
    remainingPdfs: Int?,
    isVisible: Boolean
) {
    val dimensions = Responsive.dimensions
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(180)) + androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(180)),
        exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) + androidx.compose.animation.shrinkVertically(animationSpec = androidx.compose.animation.core.tween(180))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(dimensions.buttonWidthPercent)
                .padding(bottom = dimensions.verticalSpacing)
                .heightIn(max = screenHeight * 0.10f),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(dimensions.cardPadding),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    "Modo de Prueba",
                    fontSize = scaledSp(18.sp, minSize = 14.sp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val remainingText = if (remainingPdfs == -1) "(No disponible)" else remainingPdfs.toString()
                Text(
                    "PDFs restantes: $remainingText",
                    fontSize = scaledSp(16.sp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
