package pe.pixelcollage.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import pe.pixelcollage.app.data.repository.AuthRepository
import pe.pixelcollage.app.data.repository.UserRepository
import pe.pixelcollage.app.ui.navigation.AppNavigation
import pe.pixelcollage.app.ui.theme.DynamicCollageTheme
import pe.pixelcollage.app.viewmodel.*

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    internal val projectViewModel: ProjectViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private val authRepository by lazy { AuthRepository() }
    private val userRepository by lazy { UserRepository() }

    private val mainViewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(application, authRepository, userRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val loginViewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(authRepository, userRepository) as T
            }
        }
    }

    private val controlPanelViewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ControlPanelViewModel(userRepository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        PDFBoxResourceLoader.init(applicationContext)
        setContent {
            val themeName by projectViewModel.themeName.collectAsState()
            val isPlayStoreMode by authViewModel.isPlayStoreMode.collectAsState()
            val windowSizeClass = calculateWindowSizeClass(this)

            DynamicCollageTheme(themeName = themeName) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isPlayStoreMode != null) {
                        AppNavigation(
                            windowSizeClass = windowSizeClass,
                            projectViewModel = projectViewModel,
                            mainViewModel = viewModels<MainViewModel> { mainViewModelFactory }.value,
                            loginViewModel = viewModels<LoginViewModel> { loginViewModelFactory }.value,
                            controlPanelViewModel = viewModels<ControlPanelViewModel> { controlPanelViewModelFactory }.value,
                            startDestination = if (isPlayStoreMode == true) "main_app_flow" else "auth_flow"
                        )
                    } else {
                        pe.pixelcollage.app.ui.screens.SplashScreen()
                    }
                }
            }
        }
    }
}
