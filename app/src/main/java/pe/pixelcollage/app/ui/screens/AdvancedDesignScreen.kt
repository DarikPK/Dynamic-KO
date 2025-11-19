package pe.pixelcollage.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pe.pixelcollage.app.ui.components.ResponsiveMainButton
import pe.pixelcollage.app.ui.components.ResponsiveTopAppBar
import pe.pixelcollage.app.ui.navigation.Screen
import pe.pixelcollage.app.ui.util.Responsive
import pe.pixelcollage.app.ui.util.scaledSp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDesignScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController
) {
    val dimensions = Responsive.dimensions

    Scaffold(
        topBar = {
            ResponsiveTopAppBar(
                title = "Diseño Avanzado",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", modifier = Modifier.size(dimensions.topBarIconSize))
                    }
                }
            )
        }
    ) { paddingValues ->
        val columnModifier = if (dimensions.screenWidthClass == pe.pixelcollage.app.ui.util.ScreenWidthClass.Large) {
            Modifier.widthIn(max = 600.dp)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = columnModifier
                    .fillMaxSize()
                    .padding(dimensions.externalMargin),
                verticalArrangement = Arrangement.spacedBy(dimensions.verticalSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ResponsiveMainButton(
                    onClick = { navController.navigate(Screen.GeneratedBackground.route) },
                    text = "Editor de Fondo de Hoja"
                )
                ResponsiveMainButton(
                    onClick = { navController.navigate(Screen.ImageBorders.route) },
                    text = "Bordes de Imágenes"
                )
                ResponsiveMainButton(
                    onClick = { navController.navigate(Screen.ColorThemeSelection.route) },
                    text = "Color Texto/Tablas"
                )
                ResponsiveMainButton(
                    onClick = { navController.navigate(Screen.HybridQuality.route) },
                    text = "Calidad de Imagen"
                )
            }
        }
    }
}
