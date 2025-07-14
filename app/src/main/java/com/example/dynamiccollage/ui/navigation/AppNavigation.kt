package com.example.dynamiccollage.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
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
    val navController: NavHostController = rememberNavController()

    val hasInnerPagesBeenSaved by projectViewModel.hasInnerPagesBeenSaved.collectAsStateWithLifecycle()

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, destination, _ ->
            val previousRouteTemplate = controller.previousBackStackEntry?.destination?.route

            // Comparamos las plantillas de ruta, no las rutas construidas.
            if (previousRouteTemplate == Screen.InnerPages.route && destination.route != Screen.ImageUpload.route) {
                if (!hasInnerPagesBeenSaved) {
                    projectViewModel.resetPageGroups()
                }
            }

            if (destination.route == Screen.InnerPages.route) {
                projectViewModel.confirmInnerPagesSaved(false)
            }
        }
        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

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
            route = Screen.ImageUpload.route, // "image_upload_screen/{groupId}"
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                ImageUploadScreen(
                    navController = navController,
                    projectViewModel = projectViewModel,
                    groupId = groupId,
                )
            } else {
                // Si groupId es nulo, es un error, volvemos atrás.
                navController.popBackStack()
            }
        }
        composable(Screen.PdfPreview.route) {
            PlaceholderScreen(screenName = "Vista Previa PDF")
        }
    }
}
