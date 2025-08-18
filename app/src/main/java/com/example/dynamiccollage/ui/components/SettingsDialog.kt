package com.example.dynamiccollage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@Composable
fun SettingsDialog(
    projectViewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    val projectConfig by projectViewModel.currentCoverConfig.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes de Páginas Interiores") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ingreso Inteligente")
                    Switch(
                        checked = projectConfig.smartLayoutEnabled,
                        onCheckedChange = { projectViewModel.onSmartLayoutToggled(it) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
