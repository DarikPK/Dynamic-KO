package com.example.dynamiccollage.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dynamiccollage.utils.ImageUtils

@Composable
fun SwapPhotoDialog(
    onDismissRequest: () -> Unit,
    allPhotos: List<String>,
    firstPhotoUri: String,
    onPhotoSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val firstPhotoOrientation = remember(firstPhotoUri) {
        ImageUtils.getImageOrientation(context, firstPhotoUri)
    }

    val swappablePhotos = remember(allPhotos, firstPhotoOrientation) {
        allPhotos.filter { uri ->
            uri != firstPhotoUri && ImageUtils.getImageOrientation(context, uri) == firstPhotoOrientation
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Seleccionar Foto") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                items(swappablePhotos) { uri ->
                    AsyncImage(
                        model = Uri.parse(uri),
                        contentDescription = "Photo to swap",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onPhotoSelected(uri) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
