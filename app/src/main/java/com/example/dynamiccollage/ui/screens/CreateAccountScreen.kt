package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamiccollage.data.repository.AuthRepository
import com.example.dynamiccollage.data.repository.UserRepository
import com.example.dynamiccollage.viewmodel.AccountManagementViewModel
import com.example.dynamiccollage.viewmodel.AccountManagementViewModelFactory
import com.example.dynamiccollage.viewmodel.CreationState
import java.text.Normalizer

// Función de ayuda para normalizar el texto
private fun String.normalizeForInput(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    navController: NavController,
    viewModel: AccountManagementViewModel = viewModel(
        factory = AccountManagementViewModelFactory(
            AuthRepository(),
            UserRepository()
        )
    )
) {
    var nick by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var allowAutoLogin by remember { mutableStateOf(true) }
    val creationState by viewModel.creationState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(creationState) {
        when (val state = creationState) {
            is CreationState.Success -> {
                Toast.makeText(context, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                viewModel.resetCreationState()
                navController.popBackStack()
            }
            is CreationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetCreationState()
            }
            else -> { /* No-op */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta Hijo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nick,
                onValueChange = { nick = it },
                label = { Text("Nick del Usuario") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.normalizeForInput() },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.normalizeForInput() },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Permitir inicio de sesión automático")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = allowAutoLogin,
                    onCheckedChange = { allowAutoLogin = it }
                )
            }
            Button(
                onClick = {
                    viewModel.createChildAccount(nick, email, password, allowAutoLogin)
                },
                enabled = creationState !is CreationState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (creationState is CreationState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Crear Cuenta")
                }
            }
        }
    }
}
