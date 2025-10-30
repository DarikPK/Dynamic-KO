package pe.pixelcollage.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import pe.pixelcollage.app.viewmodel.ProjectViewModel
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
                title = { Text("Calidad de Imagen") },
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
                    Text("Baja")
                    Text("Alta")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Alta calidad")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Alta: Mayor calidad, archivos más grandes.")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Baja calidad")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Baja: Menor calidad, archivos más pequeños.")
                    }
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
