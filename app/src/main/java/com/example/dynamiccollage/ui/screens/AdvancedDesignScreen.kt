package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDesignScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel = viewModel()
) {
    val context = LocalContext.current
    val coverConfig by projectViewModel.currentCoverConfig.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diseño Avanzado") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { navController.navigate(Screen.SheetBackground.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fondo de Página")
            }
            Button(
                onClick = { navController.navigate(Screen.ImageBorders.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bordes de Imágenes")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Calidad de Imagen (Híbrido): ${coverConfig.hybridImageQuality}%",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = coverConfig.hybridImageQuality.toFloat(),
                onValueChange = { newValue ->
                    projectViewModel.updateHybridImageQuality(context, newValue.roundToInt())
                },
                valueRange = 10f..100f,
                steps = 8, // (100-10)/10 = 9 steps, but steps param is 0-indexed so it's 8
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Baja")
                Text("Alta")
            }
        }
    }
}
