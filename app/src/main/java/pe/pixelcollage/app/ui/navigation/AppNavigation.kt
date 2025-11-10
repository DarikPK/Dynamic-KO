package pe.pixelcollage.app.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pe.pixelcollage.app.ui.screens.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.navigation
import pe.pixelcollage.app.data.repository.AuthRepository
import pe.pixelcollage.app.data.repository.UserRepository
import pe.pixelcollage.app.ui.screens.AccountManagementScreen
import pe.pixelcollage.app.ui.screens.CreateAccountScreen
import pe.pixelcollage.app.ui.screens.ManageAccountsScreen
import pe.pixelcollage.app.ui.screens.auth.BlockedScreen
import pe.pixelcollage.app.ui.screens.auth.ControlPanelScreen
import pe.pixelcollage.app.ui.screens.auth.LoginScreen
import pe.pixelcollage.app.viewmodel.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    projectViewModel: ProjectViewModel,
    mainViewModel: MainViewModel,
    loginViewModel: LoginViewModel,
    controlPanelViewModel: ControlPanelViewModel,
    startDestination: String
) {
    val coverSetupViewModel: CoverSetupViewModel = viewModel()
    val rowStyleViewModel: RowStyleViewModel = viewModel()
    val sunatDataViewModel: SunatDataViewModel = viewModel()
    val innerPagesViewModel: InnerPagesViewModel = viewModel(factory = InnerPagesViewModelFactory(projectViewModel))
    val navController = rememberNavController()
    val userState by mainViewModel.userState.collectAsState()

    // This effect will react to changes in userState and navigate accordingly.
    LaunchedEffect(userState) {
        if (userState is UserState.Unauthenticated) {
            // Ensure we are not already in the auth flow to prevent navigation loops
            if (navController.currentDestination?.route?.startsWith("auth_flow") == false) {
                navController.navigate("auth_flow") {
                    // Pop everything up to the start destination of the graph to clear the back stack
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    // Avoid multiple copies of the same destination when re-logging in
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
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
                        navController.navigate("main_app_flow") {
                            popUpTo("auth_flow") { inclusive = true }
                        }
                    }
                )
            }
            composable(AuthScreen.Blocked.route) {
                BlockedScreen()
            }
        }

        navigation(startDestination = Screen.Main.route, route = "main_app_flow") {
            composable(Screen.Main.route) {
                MainScreen(
                    navController = navController,
                    projectViewModel = projectViewModel,
                    mainViewModel = mainViewModel
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
        composable(Screen.PhotoSwap.route) {
            PhotoSwapScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                mainViewModel = mainViewModel
            )
        }
        composable(Screen.RecycleBin.route) {
            RecycleBinScreen(
                navController = navController,
                projectViewModel = projectViewModel
            )
        }
        composable(Screen.HybridQuality.route) {
            HybridQualityScreen(
                navController = navController,
                projectViewModel = projectViewModel
            )
        }
        composable(Screen.ColorThemeSelection.route) {
            ColorThemeSelectionScreen(
                navController = navController,
                projectViewModel = projectViewModel
            )
        }
        composable(Screen.GeneratedBackground.route) {
            GeneratedBackgroundScreen(
                navController = navController,
                projectViewModel = projectViewModel
            )
        }
        composable("account_management") {
            AccountManagementScreen(navController = navController)
        }
        composable("create_account") {
            val authRepository = AuthRepository()
            val userRepository = UserRepository()
            val factory = CreateAccountViewModelFactory(authRepository, userRepository)
            CreateAccountScreen(
                navController = navController,
                createAccountViewModel = viewModel(factory = factory)
            )
        }
        composable("manage_accounts") {
            val userRepository = UserRepository()
            val factory = ManageAccountsViewModelFactory(userRepository)
            ManageAccountsScreen(
                navController = navController,
                manageAccountsViewModel = viewModel(factory = factory)
            )
        }
        composable(AuthScreen.ControlPanel.route) {
            val user = (userState as? UserState.Authenticated)?.user
            if (user != null) {
                ControlPanelScreen(
                    controlPanelViewModel = controlPanelViewModel,
                    parentId = user.uid
                )
            }
        }
    }
}
}
