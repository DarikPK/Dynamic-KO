package com.example.dynamiccollage.ui.navigation

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
import com.example.dynamiccollage.ui.screens.ImageManagerScreen
import com.example.dynamiccollage.ui.screens.InnerPagesScreen
import com.example.dynamiccollage.ui.screens.MainScreen
import com.example.dynamiccollage.ui.screens.PdfPreviewScreen
import com.example.dynamiccollage.ui.screens.RowStyleScreen
import com.example.dynamiccollage.ui.screens.AdvancedCoverOptionsScreen
import com.example.dynamiccollage.ui.screens.MarginsScreen
import com.example.dynamiccollage.ui.screens.SunatDataScreen
import com.example.dynamiccollage.ui.screens.TextStyleScreen
import com.example.dynamiccollage.ui.screens.ColorPickerScreen
import com.example.dynamiccollage.ui.screens.WeightsScreen
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import com.example.dynamiccollage.viewmodel.RowStyleViewModel
import com.example.dynamiccollage.viewmodel.SunatDataViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun AppNavigation(
    projectViewModel: ProjectViewModel,
    coverSetupViewModel: CoverSetupViewModel = viewModel(),
    rowStyleViewModel: RowStyleViewModel = viewModel(),
    sunatDataViewModel: SunatDataViewModel = viewModel(),
    onThemeChange: (String) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                onThemeChange = onThemeChange
            )
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
        composable(Screen.SunatData.route) {
            SunatDataScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                sunatDataViewModel = sunatDataViewModel
            )
        }
        composable(Screen.ImageManager.route) {
            ImageManagerScreen(navController = navController, projectViewModel = projectViewModel)
        }
        composable(Screen.AdvancedCoverOptions.route) {
            AdvancedCoverOptionsScreen(
                navController = navController,
                coverSetupViewModel = coverSetupViewModel
            )
        }
        composable(Screen.TextStyle.route) {
            TextStyleScreen(
                navController = navController,
                coverSetupViewModel = coverSetupViewModel,
                projectViewModel = projectViewModel
            )
        }
        composable(Screen.Margins.route) {
            MarginsScreen(
                navController = navController,
                coverSetupViewModel = coverSetupViewModel,
                projectViewModel = projectViewModel
            )
        }
        composable(Screen.Weights.route) {
            WeightsScreen(
                navController = navController,
                coverSetupViewModel = coverSetupViewModel,
                projectViewModel = projectViewModel
            )
        }
        composable(
            route = Screen.ColorPicker.route + "/{fieldId}/{initialColor}",
            arguments = listOf(
                navArgument("fieldId") { type = NavType.StringType },
                navArgument("initialColor") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fieldId = backStackEntry.arguments?.getString("fieldId") ?: ""
            val initialColorHex = backStackEntry.arguments?.getString("initialColor") ?: "FFFFFF"
            ColorPickerScreen(
                navController = navController,
                fieldId = fieldId,
                initialColorHex = initialColorHex
            )
        }
    }
}
