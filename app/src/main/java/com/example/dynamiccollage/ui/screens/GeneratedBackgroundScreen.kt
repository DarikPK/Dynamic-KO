package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.data.model.BackgroundPatternType
import com.example.dynamiccollage.data.model.GeneratedBackgroundConfig
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedBackgroundScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val projectConfig by projectViewModel.currentCoverConfig.collectAsState()
    val config = projectConfig.generatedBackgroundConfig ?: GeneratedBackgroundConfig()
    val margin = with(LocalDensity.current) { 16.toDp() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mosaico") },
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
                .padding(margin),
            verticalArrangement = Arrangement.spacedBy(margin)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Habilitar Mosaico", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { isChecked ->
                        projectViewModel.updateGeneratedBackgroundConfig(
                            context,
                            config.copy(enabled = isChecked)
                        )
                    }
                )
            }

            if (config.enabled) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = config.patternType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Patrón") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        BackgroundPatternType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    projectViewModel.updateGeneratedBackgroundConfig(
                                        context,
                                        config.copy(patternType = type)
                                    )
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text("Opacidad: ${config.opacity.format(2)}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = config.opacity,
                    onValueChange = { newOpacity ->
                        projectViewModel.updateGeneratedBackgroundConfig(
                            context,
                            config.copy(opacity = newOpacity)
                        )
                    },
                    valueRange = 0f..1f
                )

                Text("Tamaño: ${config.size.format(2)}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = config.size,
                    onValueChange = { newSize ->
                        projectViewModel.updateGeneratedBackgroundConfig(
                            context,
                            config.copy(size = newSize)
                        )
                    },
                    valueRange = 1f..100f
                )

                Text("Densidad: ${config.density.format(2)}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = config.density,
                    onValueChange = { newDensity ->
                        projectViewModel.updateGeneratedBackgroundConfig(
                            context,
                            config.copy(density = newDensity)
                        )
                    },
                    valueRange = 0.1f..1f
                )
            }
        }
    }
}

private fun Float.format(digits: Int) = "%.${digits}f".format(this)
