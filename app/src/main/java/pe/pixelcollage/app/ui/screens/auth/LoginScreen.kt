package pe.pixelcollage.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.pixelcollage.app.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by loginViewModel.loginState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { loginViewModel.login(email, password) },
            enabled = loginState !is pe.pixelcollage.app.viewmodel.LoginState.Loading
        ) {
            Text("Login")
        }
        if (loginState is pe.pixelcollage.app.viewmodel.LoginState.Error) {
            Text(
                text = (loginState as pe.pixelcollage.app.viewmodel.LoginState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is pe.pixelcollage.app.viewmodel.LoginState.Success) {
            onLoginSuccess()
        }
    }
}
