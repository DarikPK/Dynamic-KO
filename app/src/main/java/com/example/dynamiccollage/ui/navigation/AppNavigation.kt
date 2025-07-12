package com.example.dynamiccollage.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dynamiccollage.ui.screens.CoverSetupScreen
import com.example.dynamiccollage.ui.screens.ImageUploadScreen // Se creará en el siguiente paso
import com.example.dynamiccollage.ui.screens.InnerPagesScreen
import com.example.dynamiccollage.ui.screens.MainScreen
import com.example.dynamiccollage.ui.screens.PlaceholderScreen
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@Composable
fun AppNavigation(projectViewModel: ProjectViewModel) {
    val navController = rememberNavController()

    // Lógica para resetear grupos si no se guardaron los cambios
    val hasInnerPagesBeenSaved by projectViewModel.hasInnerPagesBeenSaved.collectAsStateWithLifecycle()

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, destination, _ ->
            val previousRoute = controller.previousBackStackEntry?.destination?.route

            if (previousRoute == Screen.InnerPages.route && destination.route != Screen.ImageUpload.route.replace("{groupId}", "[^/]+")) {
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
            route = Screen.ImageUpload.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                // ImageUploadScreen se creará en el siguiente paso. Por ahora, un placeholder si es necesario.
                ImageUploadScreen(
                    navController = navController,
                    projectViewModel = projectViewModel,
                    groupId = groupId
                )
            } else {
                // Manejar caso de error donde groupId es nulo
                navController.popBackStack()
            }
        }
        composable(Screen.PdfPreview.route) {
            PlaceholderScreen(screenName = "Vista Previa PDF")
        }
    }
}
