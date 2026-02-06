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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // Estado local para permitir el patrón de "Borrador"
    var localCoverQuality by remember(coverConfig.hybridCoverImageQuality) {
        mutableStateOf(coverConfig.hybridCoverImageQuality.toFloat())
    }
    var localQualityMode by remember(coverConfig.qualityMode) {
        mutableStateOf(coverConfig.qualityMode)
    }
    var localInnerQuality by remember(coverConfig.hybridInnerImagesQuality) {
        mutableStateOf(coverConfig.hybridInnerImagesQuality.toFloat())
    }
    var localGroupQualities by remember(pageGroups) {
        mutableStateOf(pageGroups.associate { it.id to it.hybridImageQuality.toFloat() })
    }

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
                text = "Calidad de Imagen de Portada: ${localCoverQuality.toInt()}%",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = localCoverQuality,
                onValueChange = { localCoverQuality = it },
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
                    checked = localQualityMode == "general",
                    onCheckedChange = { localQualityMode = "general" }
                )
                Text(
                    text = "Calidad de Imágenes Interiores: ${localInnerQuality.toInt()}%",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (localQualityMode == "general") {
                Slider(
                    value = localInnerQuality,
                    onValueChange = { localInnerQuality = it },
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
                    checked = localQualityMode == "group",
                    onCheckedChange = { localQualityMode = "group" }
                )
                Text("Calidad de Imágenes por Grupo", style = MaterialTheme.typography.titleMedium)
            }

            if (localQualityMode == "group") {
                Spacer(modifier = Modifier.height(16.dp))
                pageGroups.forEach { group ->
                    val currentGroupQuality = localGroupQualities[group.id] ?: group.hybridImageQuality.toFloat()
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = "${group.groupName}: ${currentGroupQuality.toInt()}%",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Slider(
                            value = currentGroupQuality,
                            onValueChange = { newValue ->
                                localGroupQualities = localGroupQualities.toMutableMap().apply {
                                    this[group.id] = newValue
                                }
                            },
                            valueRange = 10f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val updatedConfig = coverConfig.copy(
                        hybridCoverImageQuality = localCoverQuality.roundToInt(),
                        qualityMode = localQualityMode,
                        hybridInnerImagesQuality = localInnerQuality.roundToInt()
                    )
                    projectViewModel.updateCoverConfig(updatedConfig)

                    val updatedGroups = pageGroups.map { group ->
                        group.copy(
                            hybridImageQuality = localGroupQualities[group.id]?.roundToInt()
                                ?: group.hybridImageQuality
                        )
                    }
                    projectViewModel.updatePageGroups(context, updatedGroups)
                    // updatePageGroups ya llama a saveProject(context)

                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Guardar y Volver")
            }
        }
    }
}
