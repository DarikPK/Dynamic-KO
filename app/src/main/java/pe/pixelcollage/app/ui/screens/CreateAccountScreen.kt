package pe.pixelcollage.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pe.pixelcollage.app.viewmodel.CreateAccountViewModel
import pe.pixelcollage.app.viewmodel.CreateAccountState

@Composable
fun CreateAccountScreen(
    navController: NavController,
    createAccountViewModel: CreateAccountViewModel = viewModel()
) {
    var nick by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val createAccountState by createAccountViewModel.createAccountState.collectAsState()
    val context = LocalContext.current

import androidx.compose.ui.res.stringResource
import pe.pixelcollage.app.R

    LaunchedEffect(createAccountState) {
        when (createAccountState) {
            is CreateAccountState.Success -> {
                Toast.makeText(context, R.string.account_created_successfully, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is CreateAccountState.Error -> {
                val message = (createAccountState as CreateAccountState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
            else -> {
                // No-op for Idle and Loading
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(id = R.string.create_account_title), style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = nick,
            onValueChange = { nick = it },
            label = { Text(stringResource(id = R.string.nickname_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password_label)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        if (createAccountState is CreateAccountState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { createAccountViewModel.createAccount(context, nick, email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = nick.isNotBlank() && email.isNotBlank() && password.isNotBlank()
            ) {
                Text(stringResource(id = R.string.create_account_title))
            }
        }
    }
}
