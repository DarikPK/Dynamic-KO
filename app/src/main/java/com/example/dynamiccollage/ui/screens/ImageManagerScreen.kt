package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val imageUris = projectViewModel.getAllImageUris()
    var selectedImageUri by remember { mutableStateOf(imageUris.firstOrNull()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Imagen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { /* TODO: Implement crop */ }) {
                        Icon(Icons.Default.Crop, contentDescription = "Recortar")
                    }
                    IconButton(onClick = { /* TODO: Implement rotate */ }) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Girar")
                    }
                    IconButton(onClick = { /* TODO: Implement adjustments */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Ajustes")
                    }
                    IconButton(onClick = { /* TODO: Implement filters */ }) {
                        Icon(Icons.Default.Filter, contentDescription = "Filtros")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("No hay imágenes para editar.")
            }
        }
    }
}
