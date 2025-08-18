package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.ImageBorderSettings
import com.example.dynamiccollage.data.model.ImageBorderStyle
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageBordersScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val projectConfig by projectViewModel.currentCoverConfig.collectAsState()
    val pageGroups by projectViewModel.currentPageGroups.collectAsState()

    var selectedStyle by remember { mutableStateOf(ImageBorderStyle.NONE) }
    var sliderPosition by remember { mutableStateOf(10f) }

    val selectionMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            this["cover"] = false
            pageGroups.forEach { group ->
                this[group.id] = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bordes de Imágenes") },
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
        ) {
            // Controls Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Estilo de Borde", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ImageBorderStyle.values().forEachIndexed { index, style ->
                        SegmentedButton(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ImageBorderStyle.values().size)
                        ) {
                            val textRes = when (style) {
                                ImageBorderStyle.NONE -> R.string.image_border_style_none
                                ImageBorderStyle.CURVED -> R.string.image_border_style_curved
                                ImageBorderStyle.CHAMFERED -> R.string.image_border_style_chamfered
                            }
                            Text(stringResource(id = textRes))
                        }
                    }
                }

                if (selectedStyle != ImageBorderStyle.NONE) {
                    Text("Tamaño del Borde: ${sliderPosition.toInt()}pt", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..50f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Divider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Text(
                        "Aplicar a:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                item {
                    SelectableRow(
                        text = "Portada",
                        checked = selectionMap["cover"] ?: false,
                        onCheckedChange = { selectionMap["cover"] = it }
                    )
                }
                items(pageGroups, key = { it.id }) { group ->
                    val groupName = group.groupName.ifBlank { "Grupo sin nombre" }
                    SelectableRow(
                        text = groupName,
                        checked = selectionMap[group.id] ?: false,
                        onCheckedChange = { selectionMap[group.id] = it }
                    )
                }
            }

            Button(
                onClick = {
                    val currentSettingsMap = projectConfig.imageBorderSettingsMap.toMutableMap()
                    selectionMap.forEach { (id, isSelected) ->
                        if (isSelected) {
                            if (selectedStyle == ImageBorderStyle.NONE) {
                                currentSettingsMap.remove(id)
                            } else {
                                currentSettingsMap[id] = ImageBorderSettings(style = selectedStyle, size = sliderPosition)
                            }
                        }
                    }
                    projectViewModel.updateImageBorderSettings(currentSettingsMap)
                    projectViewModel.saveProject(context)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Aplicar y Guardar")
            }
        }
    }
}

@Composable
private fun SelectableRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}
