package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(screenName: String) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dynamic Collage") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all =_16dp), // Suponiendo que _16dp es una constante o se reemplaza por 16.dp
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pantalla: $screenName",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Contenido en desarrollo.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// Definir _16dp o usar directamente Dp
private val _16dp = androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun PlaceholderScreenPreview() {
    DynamicCollageTheme {
        PlaceholderScreen("Ejemplo")
    }
}
