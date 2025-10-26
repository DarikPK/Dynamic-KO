package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedCoverOptionsScreen(
    navController: NavController,
    coverSetupViewModel: CoverSetupViewModel // ViewModel is passed but might not be used directly here
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Opciones Avanzadas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuButton(
                text = "Estilo de Texto",
                onClick = { navController.navigate(Screen.TextStyle.route) }
            )
            MenuButton(
                text = "Estilo de Tabla",
                onClick = { navController.navigate(Screen.RowStyleEditor.route) }
            )
            MenuButton(
                text = "Márgenes de Hoja",
                onClick = { navController.navigate(Screen.Margins.route) }
            )
            MenuButton(
                text = "Peso de Elementos",
                onClick = { navController.navigate(Screen.Weights.route) }
            )
        }
    }
}

@Composable
private fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(48.dp)
    ) {
        Text(text.uppercase())
    }
}
