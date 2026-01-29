package pe.pixelcollage.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val pageGroups by projectViewModel.currentPageGroups.collectAsState()

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
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
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // Quality Mode Selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = coverConfig.qualityMode == "general",
                    onCheckedChange = { projectViewModel.updateQualityMode(context, "general") }
                )
                Text(
                    text = "Calidad de Imágenes Interiores: ${coverConfig.hybridInnerImagesQuality}%",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (coverConfig.qualityMode == "general") {
                Slider(
                    value = coverConfig.hybridInnerImagesQuality.toFloat(),
                    onValueChange = { newValue ->
                        projectViewModel.updateHybridInnerImagesQuality(context, newValue.roundToInt())
                    },
                    valueRange = 10f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = coverConfig.qualityMode == "group",
                    onCheckedChange = { projectViewModel.updateQualityMode(context, "group") }
                )
                Text("Calidad de Imágenes por Grupo", style = MaterialTheme.typography.titleMedium)
            }

            if (coverConfig.qualityMode == "group") {
                Spacer(modifier = Modifier.height(16.dp))
                pageGroups.forEach { group ->
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = "${group.groupName}: ${group.hybridImageQuality}%",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Slider(
                            value = group.hybridImageQuality.toFloat(),
                            onValueChange = { newValue ->
                                projectViewModel.updateGroupQuality(context, group.id, newValue.roundToInt())
                            },
                            valueRange = 10f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
