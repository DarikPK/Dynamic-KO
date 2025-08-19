package com.example.dynamiccollage.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dynamiccollage.ui.screens.*
import com.example.dynamiccollage.viewmodel.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    projectViewModel: ProjectViewModel
) {
    val coverSetupViewModel: CoverSetupViewModel = viewModel()
    val rowStyleViewModel: RowStyleViewModel = viewModel()
    val sunatDataViewModel: SunatDataViewModel = viewModel()
    val innerPagesViewModel: InnerPagesViewModel = viewModel(factory = InnerPagesViewModelFactory(projectViewModel))
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                projectViewModel = projectViewModel
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
            InnerPagesScreen(
                navController = navController,
                innerPagesViewModel = innerPagesViewModel
            )
        }
        composable(Screen.GroupHeaderStyle.route) {
            GroupHeaderStyleScreen(
                navController = navController,
                viewModel = innerPagesViewModel
            )
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
        composable(Screen.AdvancedDesign.route) {
            AdvancedDesignScreen(navController = navController)
        }
        composable(Screen.SheetBackground.route) {
            SheetBackgroundScreen(navController = navController, projectViewModel = projectViewModel)
        }
        composable(Screen.ImageBorders.route) {
            ImageBordersScreen(navController = navController, projectViewModel = projectViewModel)
        }
        composable(Screen.SizeManager.route) {
            SizeManagerScreen(
                navController = navController,
                projectViewModel = projectViewModel
            )
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
            route = Screen.ColorPicker.route + "/{colorType}/{fieldId}/{initialColor}",
            arguments = listOf(
                navArgument("colorType") { type = NavType.StringType },
                navArgument("fieldId") { type = NavType.StringType; nullable = true },
                navArgument("initialColor") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val colorType = backStackEntry.arguments?.getString("colorType") ?: ""
            val fieldId = backStackEntry.arguments?.getString("fieldId")
            val initialColorHex = backStackEntry.arguments?.getString("initialColor") ?: "FFFFFF"
            ColorPickerScreen(
                navController = navController,
                colorType = colorType,
                fieldId = fieldId,
                initialColorHex = initialColorHex
            )
        }
        composable(
            route = Screen.ImageEffects.route + "/{imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val decodedImageUri = URLDecoder.decode(imageUri, StandardCharsets.UTF_8.toString())
            ImageEffectsScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                imageUri = decodedImageUri
            )
        }
        composable(Screen.ThemeSelection.route) {
            ThemeSelectionScreen(
                navController = navController,
                projectViewModel = projectViewModel
            )
        }
    }
}
