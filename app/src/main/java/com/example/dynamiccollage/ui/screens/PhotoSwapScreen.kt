package com.example.dynamiccollage.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dynamiccollage.utils.ImageUtils
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSwapScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val allPhotos by remember { derivedStateOf { projectViewModel.getAllImageUris() } }
    var firstSelection by remember { mutableStateOf<String?>(null) }
    var secondSelection by remember { mutableStateOf<String?>(null) }

    val firstPhotoOrientation by remember(firstSelection) {
        derivedStateOf {
            firstSelection?.let { ImageUtils.getImageOrientation(context, it) }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Intercambiar Fotos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            if (firstSelection != null && secondSelection != null) {
                FloatingActionButton(onClick = {
                    projectViewModel.swapPhotos(context, firstSelection!!, secondSelection!!)
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmar Intercambio")
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = Modifier.padding(paddingValues).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allPhotos, key = { it }) { uri ->
                val isSelected = uri == firstSelection || uri == secondSelection
                val isCompatible = firstSelection == null || ImageUtils.getImageOrientation(context, uri) == firstPhotoOrientation

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .border(
                            width = if (isSelected) 4.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .alpha(if (isCompatible) 1f else 0.4f)
                        .clickable(enabled = isCompatible) {
                            if (firstSelection == null) {
                                firstSelection = uri
                            } else if (secondSelection == null) {
                                if (uri != firstSelection) {
                                    secondSelection = uri
                                } else {
                                    // Deselect if tapping the same image again
                                    firstSelection = null
                                }
                            } else {
                                // Both are selected, reset and start a new selection
                                if (uri == firstSelection) {
                                    firstSelection = secondSelection
                                    secondSelection = null
                                } else if (uri == secondSelection) {
                                    secondSelection = null
                                } else {
                                    firstSelection = uri
                                    secondSelection = null
                                }
                            }
                        }
                ) {
                    AsyncImage(
                        model = Uri.parse(uri),
                        contentDescription = "Foto para intercambiar",
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
