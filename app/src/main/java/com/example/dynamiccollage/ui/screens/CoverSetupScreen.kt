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
            OutlinedTextField(
                value = coverConfig.clientNameStyle.content,
                onValueChange = { coverSetupViewModel.onClientNameChange(it) },
                label = { Text(stringResource(id = R.string.cover_setup_client_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = coverConfig.documentType == DocumentType.RUC,
                    onClick = { coverSetupViewModel.onDocumentTypeChange(DocumentType.RUC) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("RUC") }
                SegmentedButton(
                    selected = coverConfig.documentType == DocumentType.DNI,
                    onClick = { coverSetupViewModel.onDocumentTypeChange(DocumentType.DNI) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("DNI") }
            }
            OutlinedTextField(
                value = coverConfig.rucStyle.content,
                onValueChange = { coverSetupViewModel.onRucChange(it) },
                label = { Text(coverConfig.documentType.name) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = coverConfig.subtitleStyle.content,
                onValueChange = { coverSetupViewModel.onAddressChange(it) },
                label = { Text(stringResource(id = R.string.cover_setup_address_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = coverConfig.showAddressPrefix,
                    onCheckedChange = { coverSetupViewModel.onShowAddressPrefixChange(it) }
                )
                Text("Incluir prefijo 'Dirección:'")
            }

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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = coverConfig.allCaps,
                    onCheckedChange = { coverSetupViewModel.onAllCapsChange(it) }
                )
                Text("Poner todo el texto en mayúsculas")
            }

            var showAdvancedOptions by remember { mutableStateOf(false) }

            TextButton(onClick = { showAdvancedOptions = !showAdvancedOptions }) {
                Text(if (showAdvancedOptions) "Ocultar Opciones Avanzadas" else "Mostrar Opciones Avanzadas")
            }

            if (showAdvancedOptions) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        stringResource(id = R.string.cover_setup_text_customization_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    TextCustomizationSection(
                        label = stringResource(id = R.string.field_client_name),
                        textStyleConfig = coverConfig.clientNameStyle,
                        onTextStyleChange = { newSize, newAlign, newColor ->
                            coverSetupViewModel.onTextStyleChange(DefaultCoverConfig.CLIENT_NAME_ID, newSize, newAlign, newColor)
                        }
                    )

                    TextCustomizationSection(
                        label = coverConfig.documentType.name,
                        textStyleConfig = coverConfig.rucStyle,
                        onTextStyleChange = { newSize, newAlign, newColor ->
                            coverSetupViewModel.onTextStyleChange(DefaultCoverConfig.RUC_ID, newSize, newAlign, newColor)
                        }
                    )

                    TextCustomizationSection(
                        label = stringResource(id = R.string.field_address),
                        textStyleConfig = coverConfig.subtitleStyle,
                        onTextStyleChange = { newSize, newAlign, newColor ->
                            coverSetupViewModel.onTextStyleChange(DefaultCoverConfig.SUBTITLE_ID, newSize, newAlign, newColor)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { /* TODO: Navigate to new RowStyleEditor screen */
                            navController.navigate(Screen.RowStyleEditor.route)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Personalizar Estilos de Fila (Bordes, Fondos)")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    // Ajuste aquí: coverConfig.marginTop ya es Float (cm), no necesita .dp
                    // La sección MarginCustomizationSection debe ser adaptada para tomar Float y su lógica interna también.
                    MarginCustomizationSection(
                        marginTop = coverConfig.marginTop, // Pasar Float directamente
                        marginBottom = coverConfig.marginBottom,
                        marginLeft = coverConfig.marginLeft,
                        marginRight = coverConfig.marginRight,
                        onMarginChange = { top, bottom, left, right ->
                            coverSetupViewModel.onMarginChange(top, bottom, left, right)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    LayoutWeightsCustomizationSection(
                        clientWeight = coverConfig.clientWeight,
                        rucWeight = coverConfig.rucWeight,
                        addressWeight = coverConfig.addressWeight,
                        separationWeight = coverConfig.separationWeight,
                        photoWeight = coverConfig.photoWeight,
                        onWeightChange = { client, ruc, address, separation, photo ->
                            coverSetupViewModel.onWeightChange(client, ruc, address, separation, photo)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LayoutWeightsCustomizationSection(
    clientWeight: Float,
    rucWeight: Float,
    addressWeight: Float,
    separationWeight: Float,
    photoWeight: Float,
    onWeightChange: (client: String?, ruc: String?, address: String?, separation: String?, photo: String?) -> Unit
) {
    var clientInput by remember(clientWeight) { mutableStateOf(clientWeight.toString()) }
    var rucInput by remember(rucWeight) { mutableStateOf(rucWeight.toString()) }
    var addressInput by remember(addressWeight) { mutableStateOf(addressWeight.toString()) }
    var separationInput by remember(separationWeight) { mutableStateOf(separationWeight.toString()) }
    var photoInput by remember(photoWeight) { mutableStateOf(photoWeight.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Pesos de Diseño de Portada",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = "Peso Cliente",
                value = clientInput,
                onValueChange = { clientInput = it; onWeightChange(it, null, null, null, null) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = "Peso RUC",
                value = rucInput,
                onValueChange = { rucInput = it; onWeightChange(null, it, null, null, null) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = "Peso Dirección",
                value = addressInput,
                onValueChange = { addressInput = it; onWeightChange(null, null, it, null, null) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = "Peso Separación",
                value = separationInput,
                onValueChange = { separationInput = it; onWeightChange(null, null, null, it, null) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = "Peso Foto",
                value = photoInput,
                onValueChange = { photoInput = it; onWeightChange(null, null, null, null, it) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextCustomizationSection(
    label: String,
    textStyleConfig: TextStyleConfig,
    onTextStyleChange: (newSize: Float?, newAlign: TextAlign?, newColor: Color?) -> Unit
) {
    var currentFontSizeSlider: Float by remember(textStyleConfig.fontSize) {
        mutableStateOf(textStyleConfig.fontSize.toFloat())
    }
    val colorOptions = mapOf(
        stringResource(R.string.color_black) to Color.Black,
        stringResource(R.string.color_gray) to Color.Gray,
        stringResource(R.string.color_blue) to Color.Blue,
        stringResource(R.string.color_red) to Color.Red,
        stringResource(R.string.color_green) to Color.Green
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Text(stringResource(id = R.string.cover_setup_font_size_label, currentFontSizeSlider))
        Slider(
            value = currentFontSizeSlider,
            onValueChange = { currentFontSizeSlider = it },
            valueRange = 8f..72f,
            steps = 63,
            onValueChangeFinished = {
                onTextStyleChange(currentFontSizeSlider, null, null)
            }
        )
        Text(stringResource(id = R.string.cover_setup_alignment_label))
        val alignmentOptions = listOf(
            TextAlign.Start to stringResource(id = R.string.cover_setup_align_start),
            TextAlign.Center to stringResource(id = R.string.cover_setup_align_center),
            TextAlign.End to stringResource(id = R.string.cover_setup_align_end)
        )
        val selectedAlignmentIndex = alignmentOptions.indexOfFirst { it.first == textStyleConfig.textAlign }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            alignmentOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = index == selectedAlignmentIndex,
                    onClick = { onTextStyleChange(null, option.first, null) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = alignmentOptions.size)
                ) {
                    Text(option.second)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(id = R.string.cover_setup_text_color_label))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            colorOptions.forEach { (name, colorValue) ->
                OutlinedButton(
                    onClick = { onTextStyleChange(null, null, colorValue) },
                    modifier = Modifier.padding(8.dp), // Ajustado el padding aquí también
                    border = if (textStyleConfig.fontColor == colorValue) ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp) else ButtonDefaults.outlinedButtonBorder,
                ) {
                    Box(modifier = Modifier.size(20.dp).background(colorValue))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(name, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun MarginCustomizationSection(
    marginTop: Float, // Cambiado a Float
    marginBottom: Float, // Cambiado a Float
    marginLeft: Float, // Cambiado a Float
    marginRight: Float, // Cambiado a Float
    onMarginChange: (top: String?, bottom: String?, left: String?, right: String?) -> Unit
) {
    // Los valores ya son Float (cm), solo se convierten a String para el TextField
    var topInput by remember(marginTop) { mutableStateOf(marginTop.toString()) }
    var bottomInput by remember(marginBottom) { mutableStateOf(marginBottom.toString()) }
    var leftInput by remember(marginLeft) { mutableStateOf(marginLeft.toString()) }
    var rightInput by remember(marginRight) { mutableStateOf(marginRight.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(id = R.string.cover_setup_margins_customization_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_top),
                value = topInput,
                onValueChange = { topInput = it; onMarginChange(it, null, null, null) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_bottom),
                value = bottomInput,
                onValueChange = { bottomInput = it; onMarginChange(null, it, null, null) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_left),
                value = leftInput,
                onValueChange = { leftInput = it; onMarginChange(null, null, it, null) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_right),
                value = rightInput,
                onValueChange = { rightInput = it; onMarginChange(null, null, null, it) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MarginTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun BorderCustomizationSection(
    borderColor: Color,
    borderVisibleTop: Boolean,
    borderVisibleBottom: Boolean,
    borderVisibleLeft: Boolean,
    borderVisibleRight: Boolean,
    onBorderColorChange: (Color) -> Unit,
    onBorderVisibilityChange: (top: Boolean?, bottom: Boolean?, left: Boolean?, right: Boolean?) -> Unit
) {
    val colorOptions = mapOf(
        stringResource(R.string.color_black) to Color.Black,
        stringResource(R.string.color_gray) to Color.Gray,
        stringResource(R.string.color_blue) to Color.Blue,
        stringResource(R.string.color_red) to Color.Red,
        stringResource(R.string.color_green) to Color.Green
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(id = R.string.cover_setup_border_customization_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(stringResource(id = R.string.cover_setup_border_color_label))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), // Añadido scroll por si no caben
            horizontalArrangement = Arrangement.spacedBy(4.dp) // Espacio entre botones
        ) {
            colorOptions.forEach { (name, colorValue) ->
                OutlinedButton(
                    onClick = { onBorderColorChange(colorValue) },
                    modifier = Modifier.padding(horizontal = 2.dp),
                    border = if (borderColor == colorValue) ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp) else ButtonDefaults.outlinedButtonBorder,
                ) {
                    Box(modifier = Modifier.size(20.dp).background(colorValue))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(name, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(id = R.string.cover_setup_border_visibility_label))
        Column {
            BorderVisibilityCheckbox(
                label = stringResource(id = R.string.cover_setup_border_top),
                checked = borderVisibleTop,
                onCheckedChange = { onBorderVisibilityChange(it, null, null, null) }
            )
            BorderVisibilityCheckbox(
                label = stringResource(id = R.string.cover_setup_border_bottom),
                checked = borderVisibleBottom,
                onCheckedChange = { onBorderVisibilityChange(null, it, null, null) }
            )
            BorderVisibilityCheckbox(
                label = stringResource(id = R.string.cover_setup_border_left),
                checked = borderVisibleLeft,
                onCheckedChange = { onBorderVisibilityChange(null, null, it, null) }
            )
            BorderVisibilityCheckbox(
                label = stringResource(id = R.string.cover_setup_border_right),
                checked = borderVisibleRight,
                onCheckedChange = { onBorderVisibilityChange(null, null, null, it) }
            )
        }
    }
}

@Composable
fun BorderVisibilityCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun CoverSetupScreenPreviewWithImage() {
    DynamicCollageTheme {
        val context = LocalContext.current
        val activity = context as ComponentActivity
        CoverSetupScreen(
            navController = rememberNavController(),
            projectViewModel = viewModel(viewModelStoreOwner = activity),
            coverSetupViewModel = viewModel(viewModelStoreOwner = activity)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CoverSetupScreenPreviewWithoutImage() {
    DynamicCollageTheme {
        val context = LocalContext.current
        val activity = context as ComponentActivity
        CoverSetupScreen(
            navController = rememberNavController(),
            projectViewModel = viewModel(viewModelStoreOwner = activity),
            coverSetupViewModel = viewModel(viewModelStoreOwner = activity)
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CoverSetupScreenDarkPreview() {
    DynamicCollageTheme(darkTheme = true) {
        val context = LocalContext.current
        val activity = context as ComponentActivity
        CoverSetupScreen(
            navController = rememberNavController(),
            projectViewModel = viewModel(viewModelStoreOwner = activity),
            coverSetupViewModel = viewModel(viewModelStoreOwner = activity)
        )
    }
}
