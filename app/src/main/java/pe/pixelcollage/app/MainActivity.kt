package pe.pixelcollage.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pe.pixelcollage.app.data.repository.AuthRepository
import pe.pixelcollage.app.data.repository.UserRepository
import pe.pixelcollage.app.ui.navigation.AppNavigation
import pe.pixelcollage.app.ui.theme.DynamicCollageTheme
import pe.pixelcollage.app.viewmodel.*
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {
    internal val projectViewModel: ProjectViewModel by viewModels()

    private val authRepository by lazy { AuthRepository() }
    private val userRepository by lazy { UserRepository() }

    private val mainViewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(authRepository, userRepository) as T
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
        PDFBoxResourceLoader.init(applicationContext)
        setContent {
            val themeName by projectViewModel.themeName.collectAsState()
            DynamicCollageTheme(themeName = themeName) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        projectViewModel = projectViewModel,
                        mainViewModel = viewModels<MainViewModel> { mainViewModelFactory }.value,
                        loginViewModel = viewModels<LoginViewModel> { loginViewModelFactory }.value,
                        controlPanelViewModel = viewModels<ControlPanelViewModel> { controlPanelViewModelFactory }.value
                    )
                }
            }
        }
    }
}
