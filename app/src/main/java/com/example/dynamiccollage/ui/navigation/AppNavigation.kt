package com.example.dynamiccollage.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dynamiccollage.ui.screens.MainScreen
import com.example.dynamiccollage.ui.screens.PlaceholderScreen
import com.example.dynamiccollage.ui.screens.CoverSetupScreen // Importar la nueva pantalla

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        composable(Screen.CoverSetup.route) {
            CoverSetupScreen(navController = navController) // Usar la nueva pantalla
        }
        composable(Screen.InnerPages.route) {
            PlaceholderScreen(screenName = "Páginas Interiores")
        }
        composable(Screen.PdfPreview.route) {
            PlaceholderScreen(screenName = "Vista Previa PDF")
        }
        // composable(Screen.Settings.route) {
        //     PlaceholderScreen(screenName = "Configuración")
        // }
    }
}
