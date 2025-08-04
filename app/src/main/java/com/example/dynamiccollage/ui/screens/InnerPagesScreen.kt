package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Para obtener ViewModels específicos de pantalla
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.ui.components.CreateEditGroupDialog
import com.example.dynamiccollage.ui.components.PageGroupItem
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme
import com.example.dynamiccollage.viewmodel.InnerPagesViewModel
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import com.example.dynamiccollage.viewmodel.ViewModelFactory
import androidx.activity.ComponentActivity // Para previews, si es necesario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InnerPagesScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel // Se recibe como parámetro explícito
) {
    val innerPagesViewModel: InnerPagesViewModel = viewModel(factory = ViewModelFactory(projectViewModel))

    val pageGroups by innerPagesViewModel.pageGroups.collectAsState()
    val showDialog by innerPagesViewModel.showCreateGroupDialog.collectAsState()
    val editingGroup by innerPagesViewModel.editingGroup.collectAsState()
    val currentGroupAddingImages by innerPagesViewModel.currentGroupAddingImages.collectAsState()
    val context = LocalContext.current

    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            currentGroupAddingImages?.let { groupId ->
                val uriStrings = uris.map { it.toString() }
                innerPagesViewModel.onImagesSelectedForGroup(uriStrings, groupId)
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
                .padding(16.dp),
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
                                innerPagesViewModel.removePageGroup(groupId)
                            },
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
        val context = LocalContext.current
        InnerPagesScreen(
            navController = rememberNavController(),
            projectViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InnerPagesScreenDarkPreview() {
    DynamicCollageTheme(darkTheme = true) {
        val context = LocalContext.current
        InnerPagesScreen(
            navController = rememberNavController(),
            projectViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)
        )
    }
}
