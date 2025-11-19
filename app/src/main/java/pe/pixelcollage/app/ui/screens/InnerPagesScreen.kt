package pe.pixelcollage.app.ui.screens


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import pe.pixelcollage.app.R
import pe.pixelcollage.app.ui.theme.DynamicCollageTheme
import pe.pixelcollage.app.ui.components.ConfirmationDialog
import pe.pixelcollage.app.ui.components.CreateEditGroupDialog
import pe.pixelcollage.app.ui.components.PageGroupItem
import pe.pixelcollage.app.ui.components.ResponsiveMainButton
import pe.pixelcollage.app.ui.components.ResponsiveTopAppBar
import pe.pixelcollage.app.ui.components.SettingsDialog
import pe.pixelcollage.app.ui.util.Responsive
import pe.pixelcollage.app.ui.util.scaledSp
import pe.pixelcollage.app.viewmodel.InnerPagesViewModel
import pe.pixelcollage.app.viewmodel.InnerPagesViewModelFactory
import pe.pixelcollage.app.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InnerPagesScreen(
    navController: NavController,
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
    val originalPageGroups by innerPagesViewModel.originalPageGroups.collectAsState()

    var hasChanges by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (hasChanges) {
                    innerPagesViewModel.onSaveChanges(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(pageGroups, originalPageGroups) {
        hasChanges = pageGroups != originalPageGroups
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

    BackHandler(enabled = hasChanges) {
        showExitConfirmDialog = true
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Salir sin guardar", fontSize = scaledSp(20)) },
            text = { Text("Has realizado cambios en las páginas interiores pero no los has guardado. ¿Estás seguro de que quieres salir?", fontSize = scaledSp(16)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmDialog = false
                    innerPagesViewModel.discardChanges()
                    navController.popBackStack()
                }) {
                    Text("Sí, salir", fontSize = scaledSp(14))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("No, quedarse", fontSize = scaledSp(14))
                }
            }
        )
    }

    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            multipleImagePickerLauncher.launch("image/*")
        } else {
            showPermissionDeniedDialog = true
        }
    }

    fun requestPermissionOrLaunchPicker(groupId: String) {
        innerPagesViewModel.setGroupAddingImages(groupId) // Set the group ID first
        when (ContextCompat.checkSelfPermission(context, permission)) {
            PackageManager.PERMISSION_GRANTED -> {
                multipleImagePickerLauncher.launch("image/*")
            }
            else -> {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                    showPermissionRationaleDialog = true
                } else {
                    permissionLauncher.launch(permission)
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = innerPagesViewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    ConfirmationDialog(
        show = groupToDelete != null,
        onDismiss = { innerPagesViewModel.onDismissRemoveGroupDialog() },
        onConfirm = { innerPagesViewModel.onConfirmRemoveGroup(context) },
        title = "Eliminar Grupo",
        message = "Estás seguro de que quieres eliminar este grupo?"
    )

    ConfirmationDialog(
        show = imagesToDelete != null,
        onDismiss = { innerPagesViewModel.onDismissRemoveImagesDialog() },
        onConfirm = { innerPagesViewModel.onConfirmRemoveImages(context) },
        title = "Eliminar Imágenes",
        message = "Estás seguro de que quieres eliminar todas las imágenes de este grupo?"
    )

    // Dialog for permission rationale
    ConfirmationDialog(
        show = showPermissionRationaleDialog,
        onDismiss = { showPermissionRationaleDialog = false },
        onConfirm = { permissionLauncher.launch(permission) },
        title = "Permiso Necesario",
        message = "Para seleccionar imágenes de tu galería, la aplicación necesita permiso para acceder a tus archivos multimedia. Por favor, concede el permiso cuando se te solicite."
    )

    // Dialog for permanently denied permission
    ConfirmationDialog(
        show = showPermissionDeniedDialog,
        onDismiss = { showPermissionDeniedDialog = false },
        onConfirm = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        title = "Permiso Denegado",
        message = "El permiso para acceder a la galería fue denegado permanentemente. Para usar esta función, debes habilitarlo manualmente desde los ajustes de la aplicación.",
        confirmButtonText = "Ir a Ajustes",
        dismissButtonText = "Entendido"
    )


    if (showDialog) {
        CreateEditGroupDialog(
            context = context,
            navController = navController,
            editingGroup = editingGroup,
            viewModel = innerPagesViewModel,
            onDismiss = { innerPagesViewModel.onDismissCreateGroupDialog() }
        )
    }

    val dimensions = Responsive.dimensions

    Scaffold(
        topBar = {
            ResponsiveTopAppBar(
                title = { Text(stringResource(id = R.string.inner_pages_title), fontSize = scaledSp(22.sp, isTopBar = true)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showExitConfirmDialog = true
                        } else {
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
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cover_setup_navigate_back_description),
                            modifier = Modifier.size(dimensions.topBarIconSize)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Ajustes",
                            modifier = Modifier.size(dimensions.topBarIconSize)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(dimensions.topBarHeight - 8.dp)
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
                }
            )
        }
    ) { paddingValues ->
        val columnModifier = if (dimensions.screenWidthClass == pe.pixelcollage.app.ui.util.ScreenWidthClass.Large) {
            Modifier.widthIn(max = 600.dp)
        } else {
            Modifier
        }

        Column(
            modifier = columnModifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(dimensions.externalMargin)
        ) {
            ResponsiveMainButton(
                onClick = { innerPagesViewModel.onAddNewGroupClicked() },
                text = "Añadir Grupo",
                icon = Icons.Filled.Add
            )
            Spacer(Modifier.height(16.dp))

            if (pageGroups.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "No groups icon",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "Crea tu primer grupo",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = scaledSp(22)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Añade un grupo para empezar a organizar tus imágenes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = scaledSp(16)
                    )
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
                                requestPermissionOrLaunchPicker(groupId)
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
            innerPagesViewModel = viewModel(factory = InnerPagesViewModelFactory(projectViewModel))
        )
    }
}
