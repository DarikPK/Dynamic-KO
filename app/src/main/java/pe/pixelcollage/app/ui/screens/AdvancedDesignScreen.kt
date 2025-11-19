package pe.pixelcollage.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowSizeClass
import androidx.compose.material3.adaptive.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pe.pixelcollage.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDesignScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diseño Avanzado") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        val widthSizeClass = windowSizeClass.widthSizeClass
        val isCompact = widthSizeClass == WindowWidthSizeClass.Compact
        val contentPadding = if (isCompact) 12.dp else 16.dp
        val verticalSpacing = if (isCompact) 10.dp else 12.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                ResponsiveButton(
                    onClick = { navController.navigate(Screen.GeneratedBackground.route) },
                    text = "Editor de Fondo de Hoja",
                    isCompact = isCompact
                )
                ResponsiveButton(
                    onClick = { navController.navigate(Screen.ImageBorders.route) },
                    text = "Bordes de Imágenes",
                    isCompact = isCompact
                )
                ResponsiveButton(
                    onClick = { navController.navigate(Screen.ColorThemeSelection.route) },
                    text = "Color Texto/Tablas",
                    isCompact = isCompact
                )
                ResponsiveButton(
                    onClick = { navController.navigate(Screen.HybridQuality.route) },
                    text = "Calidad de Imagen",
                    isCompact = isCompact
                )
            }
        }
    }
}

@Composable
private fun ResponsiveButton(
    onClick: () -> Unit,
    text: String,
    isCompact: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(if (isCompact) 0.95f else 0.85f)
            .heightIn(min = 40.dp, max = 52.dp)
    ) {
        Text(text)
    }
}
