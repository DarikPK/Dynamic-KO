package pe.pixelcollage.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import pe.pixelcollage.app.ui.components.ResponsiveTopAppBar
import pe.pixelcollage.app.ui.util.Responsive
import pe.pixelcollage.app.ui.util.scaledSp
import pe.pixelcollage.app.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val recycledUris by projectViewModel.recycledUris.collectAsState()
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    if (selectedImageUri != null) {
        FullScreenImageView(uri = selectedImageUri!!) {
            selectedImageUri = null
        }
    } else {
        val dimensions = Responsive.dimensions
        Scaffold(
            topBar = {
                ResponsiveTopAppBar(
                    title = { Text("Papelera", fontSize = scaledSp(22)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", modifier = Modifier.size(dimensions.topBarIconSize))
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (recycledUris.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("La papelera está vacía", fontSize = scaledSp(18))
                }
            } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier.padding(paddingValues).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recycledUris, key = { it }) { uri ->
                    Box(modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { selectedImageUri = uri }) {
                        AsyncImage(
                            model = Uri.parse(uri),
                            contentDescription = "Imagen en la papelera",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = { projectViewModel.restorePhoto(context, uri) }) {
                                Icon(Icons.Default.Restore, contentDescription = "Restaurar", tint = Color.White)
                            }
                            IconButton(onClick = { projectViewModel.deletePhotoPermanently(context, uri) }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Eliminar permanentemente", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun FullScreenImageView(uri: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = Uri.parse(uri),
            contentDescription = "Imagen a pantalla completa",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("Volver", fontSize = scaledSp(14))
        }
    }
}