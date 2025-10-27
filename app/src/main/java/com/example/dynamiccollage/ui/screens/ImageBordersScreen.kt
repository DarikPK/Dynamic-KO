package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.example.dynamiccollage.ui.components.EditBorderSettingsDialog
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

    // A temporary map to hold changes before saving
    var tempBorderSettingsMap by remember {
        mutableStateOf(projectConfig.imageBorderSettingsMap)
    }

    var editingItemId by remember { mutableStateOf<String?>(null) }

    // Show the dialog if an item is being edited
    editingItemId?.let { id ->
        val initialSettings = tempBorderSettingsMap[id] ?: ImageBorderSettings()
        EditBorderSettingsDialog(
            initialSettings = initialSettings,
            onDismiss = { editingItemId = null },
            onConfirm = { newSettings ->
                val newMap = tempBorderSettingsMap.toMutableMap()
                if (newSettings.style == ImageBorderStyle.NONE) {
                    newMap.remove(id)
                } else {
                    newMap[id] = newSettings
                }
                tempBorderSettingsMap = newMap
                editingItemId = null
            }
        )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Text(
                        "Configuración de Bordes por Grupo:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                item {
                    val settings = tempBorderSettingsMap["cover"] ?: ImageBorderSettings()
                    EditableItemRow(
                        text = "Portada",
                        settings = settings,
                        onEditClicked = { editingItemId = "cover" }
                    )
                }
                items(pageGroups, key = { it.id }) { group ->
                    val settings = tempBorderSettingsMap[group.id] ?: ImageBorderSettings()
                    val groupName = group.groupName.ifBlank { "Grupo sin nombre (${group.id.take(6)}...)" }
                    EditableItemRow(
                        text = groupName,
                        settings = settings,
                        onEditClicked = { editingItemId = group.id }
                    )
                }
            }

            Button(
                onClick = {
                    projectViewModel.updateImageBorderSettings(context, tempBorderSettingsMap)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Guardar y Volver")
            }
        }
    }
}

@Composable
private fun EditableItemRow(
    text: String,
    settings: ImageBorderSettings,
    onEditClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onEditClicked)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge
        )

        val statusText = when (settings.style) {
            ImageBorderStyle.NONE -> stringResource(id = R.string.image_border_style_none)
            ImageBorderStyle.CURVED -> "${stringResource(id = R.string.image_border_style_curved)} (${settings.size.toInt()}pt)"
            ImageBorderStyle.CHAMFERED -> "${stringResource(id = R.string.image_border_style_chamfered)} (${settings.size.toInt()}pt)"
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Editar",
            tint = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider()
}
