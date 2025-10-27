package com.example.dynamiccollage.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynamiccollage.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateof("") }
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
            enabled = loginState !is com.example.dynamiccollage.viewmodel.LoginState.Loading
        ) {
            Text("Login")
        }
        if (loginState is com.example.dynamiccollage.viewmodel.LoginState.Error) {
            Text(
                text = (loginState as com.example.dynamiccollage.viewmodel.LoginState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is com.example.dynamiccollage.viewmodel.LoginState.Success) {
            onLoginSuccess()
        }
    }
}
