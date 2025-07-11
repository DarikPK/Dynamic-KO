package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.ui.components.PageGroupItem // Importar PageGroupItem
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.ComponentActivity // Para obtener ViewModel con alcance de Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Save // Importar icono de guardar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.widget.Toast // Para el mensaje de guardado
import com.example.dynamiccollage.ui.components.CreateEditGroupDialog // Importar el diálogo
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamiccollage.R // Asegúrate de que R se importe correctamente
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme
import com.example.dynamiccollage.viewmodel.InnerPagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InnerPagesScreen(
    navController: NavController,
    // navController: NavController, // Eliminado navController duplicado
    innerPagesViewModel: InnerPagesViewModel = viewModel(),
    projectViewModel: ProjectViewModel // Ahora se recibe como parámetro
) {
    val pageGroups by innerPagesViewModel.pageGroups.collectAsState()
    val showDialog by innerPagesViewModel.showCreateGroupDialog.collectAsState()
    val editingGroup by innerPagesViewModel.editingGroup.collectAsState()
    val currentGroupAddingImages by innerPagesViewModel.currentGroupAddingImages.collectAsState()
    val context = LocalContext.current

    // Cargar la configuración inicial del ProjectViewModel
    LaunchedEffect(projectViewModel.currentPageGroups.value) {
        innerPagesViewModel.loadInitialPageGroups(projectViewModel.currentPageGroups.value)
    }

    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            innerPagesViewModel.onImagesSelectedForGroup(uris)
        }
    }

    LaunchedEffect(currentGroupAddingImages) {
        if (currentGroupAddingImages != null) {
            multipleImagePickerLauncher.launch("image/*")
        }
    }

    if (showDialog) {
        CreateEditGroupDialog(
            editingGroup = editingGroup,
            viewModel = innerPagesViewModel, // Pasar el ViewModel de la pantalla
            onDismiss = { innerPagesViewModel.onDismissCreateGroupDialog() }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.inner_pages_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cover_setup_navigate_back_description)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        projectViewModel.setPageGroups(pageGroups) // Guardar la lista actual de grupos
                        Toast.makeText(context, context.getString(R.string.page_groups_saved_toast), Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(id = R.string.save_page_groups_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp), // Padding general para el contenido
            horizontalAlignment = Alignment.CenterHorizontally
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
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Un poco más de espacio
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
                                innerPagesViewModel.removePageGroup(groupId)
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
        InnerPagesScreen(rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InnerPagesScreenDarkPreview() {
    DynamicCollageTheme(darkTheme = true) {
        InnerPagesScreen(rememberNavController())
    }
}
