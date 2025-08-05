package com.example.dynamiccollage.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dynamiccollage.ui.screens.CoverSetupScreen
import com.example.dynamiccollage.ui.screens.InnerPagesScreen
import com.example.dynamiccollage.ui.screens.MainScreen
import com.example.dynamiccollage.ui.screens.PdfPreviewScreen
import com.example.dynamiccollage.ui.screens.RowStyleScreen
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import com.example.dynamiccollage.viewmodel.RowStyleViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun AppNavigation(
    projectViewModel: ProjectViewModel,
    coverSetupViewModel: CoverSetupViewModel = viewModel(),
    rowStyleViewModel: RowStyleViewModel = viewModel()
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController, projectViewModel = projectViewModel)
        }
        composable(Screen.CoverSetup.route) {
            CoverSetupScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                coverSetupViewModel = coverSetupViewModel
            )
        }
        composable(Screen.InnerPages.route) {
            InnerPagesScreen(navController = navController, projectViewModel = projectViewModel)
        }
        composable(
            route = Screen.PdfPreview.route + "/{pdfPath}",
            arguments = listOf(navArgument("pdfPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val pdfPath = backStackEntry.arguments?.getString("pdfPath")
            val decodedPdfPath = URLDecoder.decode(pdfPath, StandardCharsets.UTF_8.toString())
            PdfPreviewScreen(navController = navController, pdfPath = decodedPdfPath, projectViewModel = projectViewModel)
        }
        composable(Screen.RowStyleEditor.route) {
            RowStyleScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                coverSetupViewModel = coverSetupViewModel,
                rowStyleViewModel = rowStyleViewModel
            )
        }
    }
}
