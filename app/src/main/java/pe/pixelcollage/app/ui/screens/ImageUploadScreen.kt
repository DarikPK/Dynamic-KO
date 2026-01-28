package pe.pixelcollage.app.ui.screens

import androidx.compose.foundation.layout.Box
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // Importación añadida
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close // Icono para eliminar
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults // Para el fondo del botón
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import pe.pixelcollage.app.R
import pe.pixelcollage.app.viewmodel.InnerPagesViewModel
import pe.pixelcollage.app.viewmodel.InnerPagesViewModelFactory
import pe.pixelcollage.app.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageUploadScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    groupId: String
) {
    val innerPagesViewModel: InnerPagesViewModel = viewModel(
        factory = InnerPagesViewModelFactory(projectViewModel)
    )

    val pageGroups by innerPagesViewModel.pageGroups.collectAsState()
    val group = pageGroups.find { it.id == groupId }
    val context = LocalContext.current
    val hasChanges by innerPagesViewModel.hasChangesInGroup.collectAsState()
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = hasChanges) {
        showExitConfirmDialog = true
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Cambios sin guardar") },
            text = { Text("Has modificado las imágenes. ¿Qué deseas hacer?") },
            confirmButton = {
                TextButton(onClick = {
                    innerPagesViewModel.onSaveChanges(context)
                    showExitConfirmDialog = false
                    navController.popBackStack()
                }) {
                    Text("Guardar y Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    innerPagesViewModel.discardChanges()
                    showExitConfirmDialog = false
                    navController.popBackStack()
                }) {
                    Text("Descartar y Salir")
                }
            }
        )
    }


    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (hasChanges) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    LaunchedEffect(groupId) {
        innerPagesViewModel.loadOriginalUrisForGroup(groupId)
    }

    LaunchedEffect(Unit) {
        innerPagesViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            innerPagesViewModel.onImagesSelectedForGroup(context, uris, groupId)
        }
    }

    if (group == null) {
        Text("Error: Grupo no encontrado.")
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.image_upload_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showExitConfirmDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cover_setup_navigate_back_description)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(CircleShape)
                            .background(if (hasChanges) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .border(
                                width = if (hasChanges) 1.5.dp else 0.dp,
                                color = if (hasChanges) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        IconButton(
                            onClick = {
                                innerPagesViewModel.onSaveChanges(context)
                                innerPagesViewModel.loadOriginalUrisForGroup(groupId) // Recargar estado
                                Toast.makeText(context, R.string.page_groups_saved_toast, Toast.LENGTH_SHORT).show()
                            },
                            enabled = hasChanges,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = stringResource(id = R.string.save_page_groups_button_description),
                                tint = if (hasChanges) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = group.groupName.takeIf { it.isNotBlank() } ?: stringResource(R.string.group_item_unnamed_group, group.id.substring(0, 6)),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(
                    R.string.image_upload_quota_message,
                    group.totalPhotosRequired,
                    group.imageUris.size
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                color = if (group.isPhotoQuotaMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(group.imageUris, key = { it }) { uriString ->
                    Box(modifier = Modifier.aspectRatio(1f)) {
                        AsyncImage(
                            model = uriString,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { innerPagesViewModel.removeSingleImageFromGroup(context, groupId, uriString) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                ),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar imagen" // TODO: Externalize
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { multipleImagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.image_upload_add_photos_button))
            }

            Button(
                onClick = { navController.popBackStack() },
                enabled = group.isPhotoQuotaMet,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.image_upload_done_button))
            }
        }
    }
}
