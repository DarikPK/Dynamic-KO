package com.example.dynamiccollage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState // Importar collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color // Para el texto de error
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.PageOrientation
import com.example.dynamiccollage.viewmodel.InnerPagesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditGroupDialog(
    editingGroup: PageGroup?, // Null si es creación, no null si es edición
    viewModel: InnerPagesViewModel,
    onDismiss: () -> Unit
) {
    if (editingGroup == null) return

    val isConfigValid by viewModel.isEditingGroupConfigValid.collectAsState()
    val pageGroupsFromVM by viewModel.pageGroups.collectAsState() // Para obtener imageUris.size del grupo original

    val originalGroup = remember(editingGroup.id, pageGroupsFromVM) {
        pageGroupsFromVM.find { it.id == editingGroup.id }
    }

    var sheetCountString by remember {
        mutableStateOf(editingGroup.sheetCount.takeIf { it > 0 }?.toString() ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (originalGroup == null) // Es un grupo nuevo si no está en la lista del VM
                    stringResource(R.string.dialog_create_group_title)
                else stringResource(R.string.dialog_edit_group_title)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editingGroup.groupName,
                    onValueChange = { viewModel.onEditingGroupNameChange(it) },
                    label = { Text(stringResource(R.string.group_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(stringResource(R.string.group_orientation_label), style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = editingGroup.orientation == PageOrientation.Vertical,
                        onClick = { viewModel.onEditingGroupOrientationChange(PageOrientation.Vertical) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text(stringResource(R.string.orientation_vertical)) }
                    SegmentedButton(
                        selected = editingGroup.orientation == PageOrientation.Horizontal,
                        onClick = { viewModel.onEditingGroupOrientationChange(PageOrientation.Horizontal) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text(stringResource(R.string.orientation_horizontal)) }
                }

                Text(stringResource(R.string.photos_per_sheet_label), style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = editingGroup.photosPerSheet == 1,
                        onClick = { viewModel.onEditingGroupPhotosPerSheetChange(1) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text(stringResource(R.string.one_photo)) }
                    SegmentedButton(
                        selected = editingGroup.photosPerSheet == 2,
                        onClick = { viewModel.onEditingGroupPhotosPerSheetChange(2) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text(stringResource(R.string.two_photos)) }
                }

                OutlinedTextField(
                    value = sheetCountString,
                    onValueChange = {
                        sheetCountString = it
                        viewModel.onEditingGroupSheetCountChange(it)
                    },
                    label = { Text(stringResource(R.string.sheet_count_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = editingGroup.sheetCount <= 0 // Validación simple para Nro Hojas
                )
                if (editingGroup.sheetCount <= 0) {
                    Text(
                        text = stringResource(R.string.error_sheet_count_invalid),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }


                OutlinedTextField(
                    value = editingGroup.optionalTextStyle.content,
                    onValueChange = { viewModel.onEditingGroupOptionalTextChange(it) },
                    label = { Text(stringResource(R.string.optional_text_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.all_caps_label))
                    androidx.compose.material3.Switch(
                        checked = editingGroup.optionalTextStyle.allCaps,
                        onCheckedChange = { viewModel.onEditingGroupOptionalTextAllCapsChange(it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Separación entre fotos", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = editingGroup.imageSpacing,
                    onValueChange = { viewModel.onEditingGroupImageSpacingChange(it) },
                    valueRange = 0f..50f,
                    steps = 49,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                Text(
                    text = "Valor: ${editingGroup.imageSpacing.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )

                // Mostrar advertencia si la configuración no coincide con las fotos cargadas (solo en edición)
                if (originalGroup != null && originalGroup.imageUris.isNotEmpty() && !isConfigValid && editingGroup.sheetCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.warning_photo_quota_mismatch,
                            editingGroup.totalPhotosRequired,
                            originalGroup.imageUris.size
                        ),
                        color = MaterialTheme.colorScheme.error, // O un color de advertencia
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.saveEditingGroup() },
                enabled = isConfigValid && editingGroup.sheetCount > 0 // Deshabilitar si no es válido
            ) { Text(stringResource(R.string.save_button)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp)
    )
}
