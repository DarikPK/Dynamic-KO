package com.example.dynamiccollage.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val imageUris = projectViewModel.getAllImageUris()
    var selectedImageUri by remember { mutableStateOf(imageUris.firstOrNull()) }
    val context = LocalContext.current

    // TODO: Recorte deshabilitado temporalmente. La siguiente variable y su uso deben ser descomentados
    // una vez que se decida el método de recorte (librería o implementación propia).
    /*
    val cropImage = rememberLauncherForActivityResult(
        contract = com.canhub.cropper.CropImageContract(),
        onResult = { result ->
            if (result.isSuccessful) {
                selectedImageUri = result.uriContent.toString()
                // This is not ideal, as we are not saving the bitmap, but the URI.
                // For now, we will just update the selected image URI to show the preview.
            }
        }
    )
    */

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
                    // TODO: El botón de recorte está deshabilitado hasta que se solucione la funcionalidad.
                    IconButton(onClick = { /* cropImage.launch(...) */ }, enabled = false) {
                        Icon(Icons.Default.Crop, contentDescription = "Recortar (deshabilitado)")
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
