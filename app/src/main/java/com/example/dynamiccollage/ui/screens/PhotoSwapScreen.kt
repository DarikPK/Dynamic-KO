package com.example.dynamiccollage.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val coverPhotoUri by projectViewModel.currentCoverConfig.collectAsState()
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
                    projectViewModel.generatePdf(context, "swapped_preview")
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
            itemsIndexed(allPhotos, key = { _, uri -> uri }) { index, uri ->
                val isSelected = uri == firstSelection || uri == secondSelection
                val isCover = uri == coverPhotoUri.mainImageUri
                val isFirstSelectionCover = firstSelection == coverPhotoUri.mainImageUri

                val isCompatible = firstSelection == null ||
                        isFirstSelectionCover ||
                        isCover ||
                        (ImageUtils.getImageOrientation(context, uri) == firstPhotoOrientation)

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
                                    firstSelection = null
                                }
                            } else {
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
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = (index + 1).toString(),
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                            .padding(4.dp)
                    )
                    if (isCover) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Foto de portada",
                            tint = Color.Yellow,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
