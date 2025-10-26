package com.example.dynamiccollage.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dynamiccollage.data.model.User

@Composable
fun MainScreen(
    user: User,
    onLogout: () -> Unit,
    onGoToControlPanel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome, ${user.uid}!")
        Spacer(modifier = Modifier.height(16.dp))
        if (user.role == "master") {
            Button(onClick = onGoToControlPanel) {
                Text("Go to Control Panel")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}
