package pe.pixelcollage.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import pe.pixelcollage.app.R
import pe.pixelcollage.app.data.model.PageGroup
import pe.pixelcollage.app.data.model.PageOrientation
import pe.pixelcollage.app.ui.navigation.Screen
import pe.pixelcollage.app.ui.util.scaledSp
import pe.pixelcollage.app.viewmodel.InnerPagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditGroupDialog(
    context: android.content.Context,
    navController: NavController,
    editingGroup: PageGroup?,
    viewModel: InnerPagesViewModel,
    onDismiss: () -> Unit
) {
    if (editingGroup == null) return

    val isConfigValid by viewModel.isEditingGroupConfigValid.collectAsState()
    val pageGroupsFromVM by viewModel.pageGroups.collectAsState()

    val originalGroup = remember(editingGroup.id, pageGroupsFromVM) {
        pageGroupsFromVM.find { it.id == editingGroup.id }
    }

    var sheetCountString by remember(editingGroup.sheetCount) {
        mutableStateOf(editingGroup.sheetCount.takeIf { it > 0 }?.toString() ?: "")
    }
    var imageSpacingString by remember(editingGroup.imageSpacing) {
        mutableStateOf(editingGroup.imageSpacing.toInt().toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (originalGroup == null) stringResource(R.string.dialog_create_group_title)
                else stringResource(R.string.dialog_edit_group_title),
                fontSize = scaledSp(20.sp)
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
                    label = { Text(stringResource(R.string.group_name_label), fontSize = scaledSp(14.sp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = scaledSp(16.sp))
                )

                if (!editingGroup.smartLayoutEnabled) {
                    Text(stringResource(R.string.group_orientation_label), style = MaterialTheme.typography.labelMedium, fontSize = scaledSp(14.sp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = editingGroup.orientation == PageOrientation.Vertical,
                            onClick = { viewModel.onEditingGroupOrientationChange(PageOrientation.Vertical) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) { Text(stringResource(R.string.orientation_vertical), fontSize = scaledSp(14.sp)) }
                        SegmentedButton(
                            selected = editingGroup.orientation == PageOrientation.Horizontal,
                            onClick = { viewModel.onEditingGroupOrientationChange(PageOrientation.Horizontal) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) { Text(stringResource(R.string.orientation_horizontal), fontSize = scaledSp(14.sp)) }
                    }

                    OutlinedTextField(
                        value = sheetCountString,
                        onValueChange = {
                            sheetCountString = it
                            viewModel.onEditingGroupSheetCountChange(it)
                        },
                        label = { Text(stringResource(R.string.sheet_count_label), fontSize = scaledSp(14.sp)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = editingGroup.sheetCount <= 0,
                        supportingText = {
                            if (editingGroup.sheetCount <= 0) {
                                Text(stringResource(id = R.string.error_sheet_count_invalid), fontSize = scaledSp(12.sp))
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = scaledSp(16.sp))
                    )
                }

                Text(stringResource(R.string.photos_per_sheet_label), style = MaterialTheme.typography.labelMedium, fontSize = scaledSp(14.sp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = editingGroup.photosPerSheet == 1,
                        onClick = { viewModel.onEditingGroupPhotosPerSheetChange(1) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text(stringResource(R.string.one_photo), fontSize = scaledSp(14.sp)) }
                    SegmentedButton(
                        selected = editingGroup.photosPerSheet == 2,
                        onClick = { viewModel.onEditingGroupPhotosPerSheetChange(2) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text(stringResource(R.string.two_photos), fontSize = scaledSp(14.sp)) }
                }

                OutlinedTextField(
                    value = imageSpacingString,
                    onValueChange = {
                        imageSpacingString = it
                        viewModel.onEditingGroupImageSpacingChange(it)
                    },
                    label = { Text("Separación entre fotos (dp)", fontSize = scaledSp(14.sp)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = scaledSp(16.sp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { navController.navigate(Screen.GroupHeaderStyle.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val buttonText = if (editingGroup.optionalTextStyle.content.isNotBlank()) {
                        stringResource(id = R.string.dialog_edit_header_button)
                    } else {
                        stringResource(id = R.string.dialog_add_header_button)
                    }
                    Text(buttonText, fontSize = scaledSp(14.sp))
                }

                if (originalGroup != null && originalGroup.imageUris.isNotEmpty() && !isConfigValid && editingGroup.sheetCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.warning_photo_quota_mismatch,
                            editingGroup.totalPhotosRequired,
                            originalGroup.imageUris.size
                        ),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = scaledSp(12.sp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.saveEditingGroup(context) },
                enabled = isConfigValid && editingGroup.sheetCount > 0
            ) { Text(stringResource(R.string.save_button), fontSize = scaledSp(14.sp)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button), fontSize = scaledSp(14.sp))
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp)
    )
}
