package com.example.dynamiccollage.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.ImageBorderSettings
import com.example.dynamiccollage.data.model.ImageBorderStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBorderSettingsDialog(
    initialSettings: ImageBorderSettings,
    onDismiss: () -> Unit,
    onConfirm: (ImageBorderSettings) -> Unit
) {
    var selectedStyle by remember { mutableStateOf(initialSettings.style) }
    var sliderPosition by remember { mutableStateOf(initialSettings.size) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Estilo de Borde") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Style Selector
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

                // Size Slider (only visible if style is not NONE)
                if (selectedStyle != ImageBorderStyle.NONE) {
                    Text("Tamaño del Borde: ${sliderPosition.toInt()}pt", style = MaterialTheme.typography.bodyLarge)
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..50f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newSettings = ImageBorderSettings(style = selectedStyle, size = sliderPosition)
                onConfirm(newSettings)
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
