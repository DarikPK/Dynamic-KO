package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.ui.components.ConfirmationDialog
import com.example.dynamiccollage.ui.components.CreateEditGroupDialog
import com.example.dynamiccollage.ui.components.PageGroupItem
import com.example.dynamiccollage.ui.components.SettingsDialog
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme
import com.example.dynamiccollage.viewmodel.InnerPagesViewModel
import com.example.dynamiccollage.viewmodel.InnerPagesViewModelFactory
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InnerPagesScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    innerPagesViewModel: InnerPagesViewModel
) {
    val pageGroups by innerPagesViewModel.pageGroups.collectAsState()
    val showDialog by innerPagesViewModel.showCreateGroupDialog.collectAsState()
    val editingGroup by innerPagesViewModel.editingGroup.collectAsState()
    val currentGroupAddingImages by innerPagesViewModel.currentGroupAddingImages.collectAsState()
    val context = LocalContext.current
    val groupToDelete by innerPagesViewModel.showDeleteGroupDialog.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    val imagesToDelete by innerPagesViewModel.showDeleteImagesDialog.collectAsState()

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = innerPagesViewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (groupToDelete != null) {
        ConfirmationDialog(
            title = "Eliminar Grupo",
            message = "Estás seguro de que quieres eliminar este grupo?",
            onConfirm = { innerPagesViewModel.onConfirmRemoveGroup(context) },
            onDismiss = { innerPagesViewModel.onDismissRemoveGroupDialog() }
        )
    }

    if (imagesToDelete != null) {
        ConfirmationDialog(
            title = "Eliminar Imágenes",
            message = "Estás seguro de que quieres eliminar todas las imágenes de este grupo?",
            onConfirm = { innerPagesViewModel.onConfirmRemoveImages() },
            onDismiss = { innerPagesViewModel.onDismissRemoveImagesDialog() }
        )
    }

    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            currentGroupAddingImages?.let { groupId ->
                innerPagesViewModel.onImagesSelectedForGroup(context, uris, groupId)
            }
        }
    }

    LaunchedEffect(currentGroupAddingImages) {
        if (currentGroupAddingImages != null) {
            multipleImagePickerLauncher.launch("image/*")
        }
    }

    if (showDialog) {
        CreateEditGroupDialog(
            context = context,
            navController = navController,
            editingGroup = editingGroup,
            viewModel = innerPagesViewModel,
            onDismiss = { innerPagesViewModel.onDismissCreateGroupDialog() }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.inner_pages_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        val allGroupsValid = pageGroups.all { group ->
                            if (group.smartLayoutEnabled) {
                                group.imageUris.isNotEmpty()
                            } else {
                                group.isPhotoQuotaMet
                            }
                        }
                        if (allGroupsValid) {
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, R.string.error_all_groups_must_be_valid, Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cover_setup_navigate_back_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Ajustes"
                        )
                    }
                    IconButton(onClick = {
                        innerPagesViewModel.saveEditingGroup(context)
                        Toast.makeText(context, R.string.page_groups_saved_toast, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(id = R.string.save_page_groups_button_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { innerPagesViewModel.onAddNewGroupClicked() }) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.inner_pages_add_group_fab_description)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (pageGroups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.inner_pages_no_groups))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pageGroups, key = { group -> group.id }) { pageGroup ->
                        PageGroupItem(
                            pageGroup = pageGroup,
                            onAddImagesClicked = { groupId ->
                                innerPagesViewModel.onAddImagesClickedForGroup(groupId)
                            },
                            onEditGroupClicked = { groupToEdit ->
                                innerPagesViewModel.onEditGroupClicked(groupToEdit)
                            },
                            onDeleteGroupClicked = { groupId ->
                                innerPagesViewModel.onRemoveGroupClicked(groupId)
                            },
                            onDeleteImagesClicked = { groupId ->
                                innerPagesViewModel.onRemoveImagesClicked(groupId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InnerPagesScreenPreview() {
    DynamicCollageTheme {
        val projectViewModel: ProjectViewModel = viewModel()
        InnerPagesScreen(
            navController = rememberNavController(),
            projectViewModel = projectViewModel,
            innerPagesViewModel = viewModel(factory = InnerPagesViewModelFactory(projectViewModel))
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InnerPagesScreenDarkPreview() {
    DynamicCollageTheme(darkTheme = true) {
        val projectViewModel: ProjectViewModel = viewModel()
        InnerPagesScreen(
            navController = rememberNavController(),
            projectViewModel = projectViewModel,
            innerPagesViewModel = viewModel(factory = InnerPagesViewModelFactory(projectViewModel))
        )
    }
}
