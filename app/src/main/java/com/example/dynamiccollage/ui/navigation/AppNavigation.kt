package com.example.dynamiccollage.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.dynamiccollage.ui.screens.*
import com.example.dynamiccollage.ui.screens.auth.*
import com.example.dynamiccollage.viewmodel.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    projectViewModel: ProjectViewModel,
    onThemeChange: (String) -> Unit,
    mainViewModel: MainViewModel,
    loginViewModel: LoginViewModel,
    controlPanelViewModel: ControlPanelViewModel
) {
    val context = LocalContext.current
    val coverSetupViewModel: CoverSetupViewModel = viewModel()
    val rowStyleViewModel: RowStyleViewModel = viewModel()
    val sunatDataViewModel: SunatDataViewModel = viewModel()
    val innerPagesViewModel: InnerPagesViewModel = viewModel(factory = InnerPagesViewModelFactory(projectViewModel))
    val sizeManagerViewModel: SizeManagerViewModel = viewModel()
    val navController = rememberNavController()
    val userState by mainViewModel.userState.collectAsState()

    NavHost(navController = navController, startDestination = "auth_flow") {
        navigation(startDestination = "splash", route = "auth_flow") {
            composable("splash") {
                // A simple splash screen to decide where to go
                when (userState) {
                    is UserState.Authenticated -> {
                        navController.navigate("main_app_flow") {
                            popUpTo("auth_flow") { inclusive = true }
                        }
                    }
                    is UserState.Unauthenticated -> {
                        navController.navigate(AuthScreen.Login.route) {
                            popUpTo("auth_flow") { inclusive = true }
                        }
                    }
                    is UserState.Blocked -> {
                        navController.navigate(AuthScreen.Blocked.route) {
                            popUpTo("auth_flow") { inclusive = true }
                        }
                    }
                    is UserState.Loading -> {
                        // Show a loading indicator
                    }
                }
            }
            composable(AuthScreen.Login.route) {
                LoginScreen(
                    loginViewModel = loginViewModel,
                    onLoginSuccess = {
                        mainViewModel.checkUser()
                    }
                )
            }
            composable(AuthScreen.Blocked.route) {
                BlockedScreen()
            }
        }

        navigation(startDestination = Screen.Main.route, route = "main_app_flow") {
            composable(Screen.Main.route) {
                val user = (userState as? UserState.Authenticated)?.user
                if (user != null) {
                    MainScreen(
                        navController = navController,
                        projectViewModel = projectViewModel,
                        onThemeChange = onThemeChange
                    )
                }
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
                projectViewModel = projectViewModel,
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
        composable(Screen.SizeManager.route) {
            SizeManagerScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                sizeManagerViewModel = sizeManagerViewModel
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
    }
}
