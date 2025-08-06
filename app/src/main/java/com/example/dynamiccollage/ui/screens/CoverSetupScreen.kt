package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.DefaultCoverConfig // Usado por onTextStyleChange
import com.example.dynamiccollage.data.model.DocumentType
import com.example.dynamiccollage.data.model.PageOrientation // NUEVA IMPORTACIÓN
import com.example.dynamiccollage.data.model.TextStyleConfig
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import androidx.activity.ComponentActivity // Para Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverSetupScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    coverSetupViewModel: CoverSetupViewModel
) {
    val coverConfig by coverSetupViewModel.coverConfig.collectAsState()
    val projectCoverConfig by projectViewModel.currentCoverConfig.collectAsState()
    val sunatData by projectViewModel.sunatData.collectAsState()
    val context = LocalContext.current
    // val detectedPhotoOrientation by coverSetupViewModel.detectedPhotoOrientation.collectAsState() // Para Paso 2

    LaunchedEffect(projectCoverConfig) {
        coverSetupViewModel.loadInitialConfig(projectCoverConfig)
    }

    LaunchedEffect(sunatData) {
        sunatData?.let {
            coverSetupViewModel.onSunatDataReceived(it)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coverSetupViewModel.onMainImageSelected(it, context.contentResolver)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.cover_setup_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cover_setup_navigate_back_description)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AdvancedCoverOptions.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Opciones Avanzadas"
                        )
                    }
                    IconButton(onClick = {
                        projectViewModel.updateCoverConfig(coverConfig)
                        Toast.makeText(context, context.getString(R.string.cover_config_saved_toast), Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(id = R.string.save_cover_config_button_description)
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Fila 1", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = coverConfig.clientNameStyle.content,
                onValueChange = { coverSetupViewModel.onClientNameChange(it) },
                label = { Text(stringResource(id = R.string.cover_setup_client_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Fila 2", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = coverConfig.documentType == DocumentType.RUC,
                    onClick = { coverSetupViewModel.onDocumentTypeChange(DocumentType.RUC) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) { Text("RUC") }
                SegmentedButton(
                    selected = coverConfig.documentType == DocumentType.DNI,
                    onClick = { coverSetupViewModel.onDocumentTypeChange(DocumentType.DNI) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) { Text("DNI") }
                SegmentedButton(
                    selected = coverConfig.documentType == DocumentType.NONE,
                    onClick = { coverSetupViewModel.onDocumentTypeChange(DocumentType.NONE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) { Text("-") }
            }
            OutlinedTextField(
                value = coverConfig.rucStyle.content,
                onValueChange = { coverSetupViewModel.onRucChange(it) },
                label = { Text(coverConfig.documentType.name) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Fila 3", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = coverConfig.showAddressPrefix,
                    onClick = { coverSetupViewModel.onShowAddressPrefixChange(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Dirección") }
                SegmentedButton(
                    selected = !coverConfig.showAddressPrefix,
                    onClick = { coverSetupViewModel.onShowAddressPrefixChange(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("-") }
            }

            OutlinedTextField(
                value = coverConfig.subtitleStyle.content,
                onValueChange = { coverSetupViewModel.onAddressChange(it) },
                label = { Text(stringResource(id = R.string.cover_setup_address_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(id = R.string.cover_setup_select_image_button))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visualización de la imagen seleccionada
            if (coverConfig.mainImageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(coverConfig.mainImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.cover_image_selected_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.cover_no_image_selected),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Mensaje de orientación de foto detectada (para Paso 2)
            // val photoOrientationText = // ... lógica del mensaje
            // Text(text = photoOrientationText, ...)

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Selector de Orientación de Portada (NUEVO)
            Text(
                "Orientación de foto recomendada",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = coverConfig.pageOrientation == PageOrientation.Vertical,
                    onClick = { coverSetupViewModel.onPageOrientationChange(PageOrientation.Vertical) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text(stringResource(R.string.orientation_vertical)) }
                SegmentedButton(
                    selected = coverConfig.pageOrientation == PageOrientation.Horizontal,
                    onClick = { coverSetupViewModel.onPageOrientationChange(PageOrientation.Horizontal) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text(stringResource(R.string.orientation_horizontal)) }
            }




            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

