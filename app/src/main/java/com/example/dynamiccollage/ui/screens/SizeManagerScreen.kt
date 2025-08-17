package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import com.example.dynamiccollage.viewmodel.SizeManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeManagerScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    sizeManagerViewModel: SizeManagerViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val projectConfig by projectViewModel.currentCoverConfig.collectAsState()
    val imageQuality by sizeManagerViewModel.imageQuality.collectAsState()
    val autoAdjustSize by sizeManagerViewModel.autoAdjustSize.collectAsState()

    LaunchedEffect(projectConfig) {
        sizeManagerViewModel.loadInitialState(projectConfig)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Tamaño de PDF") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val mappedQuality = remember(imageQuality) {
                ((imageQuality - 50) * (100f / 40f)).toInt()
            }

            Text(
                text = "Calidad de Imagen: $mappedQuality%",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = imageQuality.toFloat(),
                onValueChange = { sizeManagerViewModel.onQualityChange(it) },
                valueRange = 50f..90f,
                steps = 39
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = autoAdjustSize,
                    onCheckedChange = { sizeManagerViewModel.onAutoAdjustChange(it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajustar tamaño automáticamente si supera 3MB")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val newConfig = projectConfig.copy(
                        imageQuality = imageQuality,
                        autoAdjustSize = autoAdjustSize
                    )
                    projectViewModel.updateCoverConfig(newConfig)
                    projectViewModel.saveProject(context)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}
