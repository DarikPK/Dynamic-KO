package com.example.dynamiccollage.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dynamiccollage.ui.screens.MainScreen
import com.example.dynamiccollage.ui.screens.PlaceholderScreen
import com.example.dynamiccollage.ui.screens.CoverSetupScreen
import com.example.dynamiccollage.ui.screens.InnerPagesScreen
import com.example.dynamiccollage.viewmodel.ProjectViewModel // Importar ProjectViewModel

@Composable
fun AppNavigation(projectViewModel: ProjectViewModel) { // Aceptar ProjectViewModel
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController, projectViewModel = projectViewModel)
        }
        composable(Screen.CoverSetup.route) {
            CoverSetupScreen(navController = navController, projectViewModel = projectViewModel)
        }
        composable(Screen.InnerPages.route) {
            InnerPagesScreen(navController = navController, projectViewModel = projectViewModel)
        }
        composable(Screen.PdfPreview.route) {
            // PdfPreviewScreen también podría necesitar projectViewModel si muestra datos del proyecto
            PlaceholderScreen(screenName = "Vista Previa PDF")
        }
        // composable(Screen.Settings.route) {
        //     PlaceholderScreen(screenName = "Configuración")
        // }
    }
}
