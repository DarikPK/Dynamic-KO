package com.example.dynamiccollage.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dynamiccollage.ui.screens.CoverSetupScreen
import com.example.dynamiccollage.ui.screens.ImageUploadScreen
import com.example.dynamiccollage.ui.screens.InnerPagesScreen
import com.example.dynamiccollage.ui.screens.MainScreen
import com.example.dynamiccollage.ui.screens.PlaceholderScreen
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@Composable
fun AppNavigation(projectViewModel: ProjectViewModel) {
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
        composable(
            route = Screen.ImageUpload.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                ImageUploadScreen(
                    navController = navController,
                    projectViewModel = projectViewModel,
                    groupId = groupId
                )
            } else {
                navController.popBackStack()
            }
        }
        composable(Screen.PdfPreview.route) {
            PlaceholderScreen(screenName = "Vista Previa PDF")
        }
    }
}
