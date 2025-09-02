package com.example.dynamiccollage.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary // Asegurar que esta importación es correcta o ajustar el icono
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.data.model.PageOrientation
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme

@Composable
fun PageGroupItem(
    pageGroup: PageGroup,
    onAddImagesClicked: (String) -> Unit,
    onEditGroupClicked: (PageGroup) -> Unit,
    onDeleteGroupClicked: (String) -> Unit,
    onDeleteImagesClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pageGroup.groupName.isNotBlank()) {
                    Text(
                        text = pageGroup.groupName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(R.string.group_item_unnamed_group, pageGroup.id.substring(0, 6)), // Mostrar parte del ID si no tiene nombre
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onDeleteGroupClicked(pageGroup.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.group_item_delete_button_description), tint = MaterialTheme.colorScheme.error)
                }
            }

            InfoRow(
                label = stringResource(R.string.group_item_layout_type),
                value = if (pageGroup.smartLayoutEnabled) stringResource(R.string.smart_layout_title) else stringResource(R.string.manual_layout_title)
            )

            if (pageGroup.smartLayoutEnabled) {
                InfoRow(
                    label = stringResource(R.string.group_item_photos_loaded),
                    value = "${pageGroup.imageUris.size}"
                )
            } else {
                InfoRow(label = stringResource(R.string.group_orientation_label), value = pageGroup.orientation.name)
                InfoRow(label = stringResource(R.string.photos_per_sheet_label), value = "${pageGroup.photosPerSheet}")
                InfoRow(
                    label = stringResource(R.string.group_item_total_photos_required),
                    value = "${pageGroup.imageUris.size}/${pageGroup.totalPhotosRequired}",
                    isMet = pageGroup.isPhotoQuotaMet,
                    metColor = MaterialTheme.colorScheme.primary,
                    notMetColor = MaterialTheme.colorScheme.error
                )
            }

            if (pageGroup.optionalTextStyle.isVisible) {
                Text(
                    text = "\"${pageGroup.optionalTextStyle.content}\"",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            // TODO: Miniaturas de imágenes cargadas (horizontal scroll)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onAddImagesClicked(pageGroup.id) },
                    enabled = pageGroup.smartLayoutEnabled || !pageGroup.isPhotoQuotaMet
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.group_item_add_images_button))
                }
                Row {
                    IconButton(onClick = onMoveUp, enabled = !isFirst) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Mover arriba")
                    }
                    IconButton(onClick = onMoveDown, enabled = !isLast) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Mover abajo")
                    }

                    if (pageGroup.imageUris.isNotEmpty()) {
                        IconButton(onClick = { onDeleteImagesClicked(pageGroup.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Loaded Images", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = { onEditGroupClicked(pageGroup) }) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.group_item_edit_button_description))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isMet: Boolean? = null, // Para colorear condicionalmente
    metColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    notMetColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row {
        Text("$label: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        val valueColor = when (isMet) {
            true -> metColor
            false -> notMetColor
            null -> MaterialTheme.colorScheme.onSurface
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}

@Preview(showBackground = true)
@Composable
fun PageGroupItemPreview() {
    DynamicCollageTheme {
        PageGroupItem(
            pageGroup = PageGroup(
                id = "preview-123",
                groupName = "Grupo de Prueba",
                orientation = PageOrientation.Vertical,
                photosPerSheet = 2,
                sheetCount = 3,
                optionalTextStyle = com.example.dynamiccollage.data.model.TextStyleConfig(content="Texto opcional de ejemplo"),
                imageUris = List(5) { "uri_placeholder" } // 5 de 6 fotos cargadas
            ),
            isFirst = false,
            isLast = false,
            onMoveUp = {},
            onMoveDown = {},
            onAddImagesClicked = {},
            onEditGroupClicked = {},
            onDeleteGroupClicked = {},
            onDeleteImagesClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PageGroupItemUnnamedPreview() {
    DynamicCollageTheme {
        PageGroupItem(
            pageGroup = PageGroup(
                id = "test-id-unnamed",
                orientation = PageOrientation.Horizontal,
                photosPerSheet = 1,
                sheetCount = 1,
                imageUris = List(1) { "uri_placeholder" } // Cuota cumplida
            ),
            isFirst = true,
            isLast = true,
            onMoveUp = {},
            onMoveDown = {},
            onAddImagesClicked = {},
            onEditGroupClicked = {},
            onDeleteGroupClicked = {},
            onDeleteImagesClicked = {}
        )
    }
}
