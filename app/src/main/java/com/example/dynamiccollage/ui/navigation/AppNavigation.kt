package com.example.dynamiccollage.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dynamiccollage.ui.screens.MainScreen // Se creará en el siguiente paso del plan
import com.example.dynamiccollage.ui.screens.PlaceholderScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            // MainScreen se creará en el siguiente paso del plan.
            // Por ahora, podemos usar un Placeholder o directamente llamar a MainScreen si ya lo tuviéramos.
            // Asumiendo que MainScreen tomará el navController para navegar a otras pantallas.
             MainScreen(navController = navController)
            // PlaceholderScreen(screenName = "Principal (MainScreen)")
        }
        composable(Screen.CoverSetup.route) {
            PlaceholderScreen(screenName = "Configuración de Portada")
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
