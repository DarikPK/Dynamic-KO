package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.DefaultCoverConfig
import com.example.dynamiccollage.data.model.TextStyleConfig
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedCoverOptionsScreen(
    navController: NavController,
    coverSetupViewModel: CoverSetupViewModel
) {
    val coverConfig by coverSetupViewModel.coverConfig.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Opciones Avanzadas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
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
        ) {
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

            MarginCustomizationSection(
                marginTop = coverConfig.marginTop,
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
    var clientInput by remember { mutableStateOf(clientWeight.toString()) }
    var rucInput by remember { mutableStateOf(rucWeight.toString()) }
    var addressInput by remember { mutableStateOf(addressWeight.toString()) }
    var separationInput by remember { mutableStateOf(separationWeight.toString()) }
    var photoInput by remember { mutableStateOf(photoWeight.toString()) }

    // NOTA: Se eliminan los LaunchedEffect para evitar que el campo se reinicie al borrar.
    // El estado se inicializa una vez y luego vive independientemente en la UI.

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
                onValueChange = {
                    clientInput = it
                    onWeightChange(it, null, null, null, null)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = "Peso RUC",
                value = rucInput,
                onValueChange = {
                    rucInput = it
                    onWeightChange(null, it, null, null, null)
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = "Peso Dirección",
                value = addressInput,
                onValueChange = {
                    addressInput = it
                    onWeightChange(null, null, it, null, null)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = "Peso Separación",
                value = separationInput,
                onValueChange = {
                    separationInput = it
                    onWeightChange(null, null, null, it, null)
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = "Peso Foto",
                value = photoInput,
                onValueChange = {
                    photoInput = it
                    onWeightChange(null, null, null, null, it)
                },
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
    marginTop: Float,
    marginBottom: Float,
    marginLeft: Float,
    marginRight: Float,
    onMarginChange: (top: String?, bottom: String?, left: String?, right: String?) -> Unit
) {
    var topInput by remember { mutableStateOf(marginTop.toString()) }
    var bottomInput by remember { mutableStateOf(marginBottom.toString()) }
    var leftInput by remember { mutableStateOf(marginLeft.toString()) }
    var rightInput by remember { mutableStateOf(marginRight.toString()) }

    // NOTA: Se eliminan los LaunchedEffect para evitar que el campo se reinicie al borrar.

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
                onValueChange = {
                    topInput = it
                    onMarginChange(it, null, null, null)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_bottom),
                value = bottomInput,
                onValueChange = {
                    bottomInput = it
                    onMarginChange(null, it, null, null)
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_left),
                value = leftInput,
                onValueChange = {
                    leftInput = it
                    onMarginChange(null, null, it, null)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_right),
                value = rightInput,
                onValueChange = {
                    rightInput = it
                    onMarginChange(null, null, null, it)
                },
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
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}
