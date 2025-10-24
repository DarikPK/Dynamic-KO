package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HybridQualityScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel = viewModel()
) {
    val context = LocalContext.current
    val coverConfig by projectViewModel.currentCoverConfig.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calidad de Imagen (Híbrido)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Slider for Cover Image Quality
                Text(
                    text = "Calidad de Imagen de Portada: ${coverConfig.hybridCoverImageQuality}%",
                    style = MaterialTheme.typography.titleMedium
                )
                Slider(
                    value = coverConfig.hybridCoverImageQuality.toFloat(),
                    onValueChange = { newValue ->
                        projectViewModel.updateHybridCoverImageQuality(context, newValue.roundToInt())
                    },
                    valueRange = 10f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Baja")
                    Text("Alta")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Slider for Inner Images Quality
                Text(
                    text = "Calidad de Imágenes Interiores: ${coverConfig.hybridInnerImagesQuality}%",
                    style = MaterialTheme.typography.titleMedium
                )
                Slider(
                    value = coverConfig.hybridInnerImagesQuality.toFloat(),
                    onValueChange = { newValue ->
                        projectViewModel.updateHybridInnerImagesQuality(context, newValue.roundToInt())
                    },
                    valueRange = 10f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Baja (archivos más pequeños)")
                    Text("Alta (archivos más grandes)")
                }
            }
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Guardar y Volver")
            }
        }
    }
}
