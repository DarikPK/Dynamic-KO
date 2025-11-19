package pe.pixelcollage.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pe.pixelcollage.app.R
import pe.pixelcollage.app.data.model.DocumentType
import pe.pixelcollage.app.data.model.PageOrientation
import pe.pixelcollage.app.ui.components.ResponsiveMainButton
import pe.pixelcollage.app.ui.components.ResponsiveTopAppBar
import pe.pixelcollage.app.ui.util.Responsive
import pe.pixelcollage.app.ui.util.scaledSp
import pe.pixelcollage.app.viewmodel.CoverSetupViewModel
import pe.pixelcollage.app.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverSetupScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    projectViewModel: ProjectViewModel,
    coverSetupViewModel: CoverSetupViewModel
) {
    val coverConfig by coverSetupViewModel.coverConfig.collectAsState()
    val projectCoverConfig by projectViewModel.currentCoverConfig.collectAsState()
    val sunatData by projectViewModel.sunatData.collectAsState()
    val context = LocalContext.current
    var hasChanges by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (hasChanges) {
                    projectViewModel.saveCoverConfigAndProcessImage(context, coverConfig)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(projectCoverConfig) {
        coverSetupViewModel.loadInitialConfig(projectCoverConfig)
    }

    LaunchedEffect(sunatData) {
        sunatData?.let {
            coverSetupViewModel.onSunatDataReceived(it)
        }
    }

    LaunchedEffect(coverConfig, projectCoverConfig) {
        hasChanges = coverConfig != projectCoverConfig
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
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Salir sin guardar", fontSize = scaledSp(20)) },
            text = { Text("Has realizado cambios en la portada pero no los has guardado. ¿Estás seguro de que quieres salir?", fontSize = scaledSp(16)) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    navController.popBackStack()
                }) {
                    Text("Sí, salir", fontSize = scaledSp(14))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No, quedarse", fontSize = scaledSp(14))
                }
            }
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coverSetupViewModel.onMainImageSelected(it, context.contentResolver)
        }
    }

    val dimensions = Responsive.dimensions

    Scaffold(
        topBar = {
            ResponsiveTopAppBar(
                title = { Text(stringResource(id = R.string.cover_setup_title), fontSize = scaledSp(22)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showDialog = true
                        } else {
                            navController.popBackStack()
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
                    IconButton(onClick = { navController.navigate(pe.pixelcollage.app.ui.navigation.Screen.AdvancedCoverOptions.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Opciones Avanzadas",
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
                                projectViewModel.saveCoverConfigAndProcessImage(context, coverConfig)
                                Toast.makeText(context, context.getString(R.string.cover_config_saved_toast), Toast.LENGTH_SHORT).show()
                            },
                            enabled = hasChanges,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = stringResource(id = R.string.save_cover_config_button_description),
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = columnModifier
                    .fillMaxSize()
                    .padding(dimensions.externalMargin)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.verticalSpacing)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = coverConfig.showClientPrefix,
                        onClick = { coverSetupViewModel.onShowClientPrefixChange(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Cliente", fontSize = scaledSp(14)) }
                    SegmentedButton(
                        selected = !coverConfig.showClientPrefix,
                        onClick = { coverSetupViewModel.onShowClientPrefixChange(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("-", fontSize = scaledSp(14)) }
                }
                OutlinedTextField(
                    value = coverConfig.clientNameStyle.content,
                    onValueChange = { coverSetupViewModel.onClientNameChange(it) },
                    label = { Text("Fila 1", fontSize = scaledSp(14)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = dimensions.buttonMinHeight),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = scaledSp(16))
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = coverConfig.documentType == DocumentType.RUC,
                        onClick = { coverSetupViewModel.onDocumentTypeChange(DocumentType.RUC) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) { Text("RUC", fontSize = scaledSp(14)) }
                    SegmentedButton(
                        selected = coverConfig.documentType == DocumentType.DNI,
                        onClick = { coverSetupViewModel.onDocumentTypeChange(DocumentType.DNI) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) { Text("DNI", fontSize = scaledSp(14)) }
                    SegmentedButton(
                        selected = coverConfig.documentType == DocumentType.NONE,
                        onClick = { coverSetupViewModel.onDocumentTypeChange(DocumentType.NONE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) { Text("-", fontSize = scaledSp(14)) }
                }
                OutlinedTextField(
                    value = coverConfig.rucStyle.content,
                    onValueChange = { coverSetupViewModel.onRucChange(it) },
                    label = { Text("Fila 2", fontSize = scaledSp(14)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = dimensions.buttonMinHeight),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (coverConfig.documentType == DocumentType.NONE) KeyboardType.Text else KeyboardType.Number
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = scaledSp(16))
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = coverConfig.showAddressPrefix,
                        onClick = { coverSetupViewModel.onShowAddressPrefixChange(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Dirección", fontSize = scaledSp(14)) }
                    SegmentedButton(
                        selected = !coverConfig.showAddressPrefix,
                        onClick = { coverSetupViewModel.onShowAddressPrefixChange(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("-", fontSize = scaledSp(14)) }
                }
                OutlinedTextField(
                    value = coverConfig.subtitleStyle.content,
                    onValueChange = { coverSetupViewModel.onAddressChange(it) },
                    label = { Text("Fila 3", fontSize = scaledSp(14)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = dimensions.buttonMinHeight),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = scaledSp(16))
                )

                Spacer(modifier = Modifier.height(8.dp))

                ResponsiveMainButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    text = stringResource(id = R.string.cover_setup_select_image_button),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val screenHeight = LocalConfiguration.current.screenHeightDp.dp

                if (coverConfig.mainImageUri != null) {
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = screenHeight * 0.55f)) {
                        val request = if (coverConfig.forceFullResCover) {
                            ImageRequest.Builder(context)
                                .data(coverConfig.mainImageUri)
                                .allowHardware(false)
                                .size(coil.size.Size.ORIGINAL)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build()
                        } else {
                            ImageRequest.Builder(context)
                                .data(coverConfig.mainImageUri)
                                .allowHardware(true)
                                .size(coil.size.Size(1080, 1080))
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .crossfade(true)
                                .build()
                        }

                        AsyncImage(
                            model = request,
                            contentDescription = stringResource(R.string.cover_image_selected_description),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { coverSetupViewModel.clearMainImage() },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar imagen",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .heightIn(max = screenHeight * 0.55f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.cover_no_image_selected),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = scaledSp(12)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Orientación de foto recomendada",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = scaledSp(18)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = coverConfig.pageOrientation == PageOrientation.Vertical,
                        onClick = { coverSetupViewModel.onPageOrientationChange(PageOrientation.Vertical) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text(stringResource(R.string.orientation_vertical), fontSize = scaledSp(14)) }
                    SegmentedButton(
                        selected = coverConfig.pageOrientation == PageOrientation.Horizontal,
                        onClick = { coverSetupViewModel.onPageOrientationChange(PageOrientation.Horizontal) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text(stringResource(R.string.orientation_horizontal), fontSize = scaledSp(14)) }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
