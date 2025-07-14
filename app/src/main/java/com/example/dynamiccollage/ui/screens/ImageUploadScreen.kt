package com.example.dynamiccollage.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close // Icono para eliminar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults // Para el fondo del botón
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dynamiccollage.R
import com.example.dynamiccollage.viewmodel.InnerPagesViewModel
import com.example.dynamiccollage.viewmodel.InnerPagesViewModelFactory
import com.example.dynamiccollage.viewmodel.ProjectViewModel

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

    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            innerPagesViewModel.onImagesSelectedForGroup(uris, groupId)
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cover_setup_navigate_back_description)
                        )
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
                            onClick = { innerPagesViewModel.removeImageFromGroup(groupId, uriString) },
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
