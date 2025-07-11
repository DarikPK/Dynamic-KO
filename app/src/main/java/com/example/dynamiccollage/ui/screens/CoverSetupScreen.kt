package com.example.dynamiccollage.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save // Importar icono de guardar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import android.widget.Toast
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner // Importar LocalViewModelStoreOwner
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.DefaultCoverConfig
import com.example.dynamiccollage.data.model.TextStyleConfig
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverSetupScreen(
    navController: NavController,
    coverSetupViewModel: CoverSetupViewModel = viewModel(),
    projectViewModel: ProjectViewModel // Ahora se recibe como parámetro
) {
    // val projectViewModel: ProjectViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity) -> Eliminado
    val coverConfig by coverSetupViewModel.coverConfig.collectAsState()
    val context = LocalContext.current

    // Cargar la configuración inicial del ProjectViewModel cuando la pantalla se lanza por primera vez
    // o cuando la configuración del proyecto cambie (ej. por un reset).
    LaunchedEffect(projectViewModel.currentCoverConfig.value) {
        coverSetupViewModel.loadInitialConfig(projectViewModel.currentCoverConfig.value)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onMainImageSelected(uri)
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

            OutlinedTextField(
                value = coverConfig.rucStyle.content,
                onValueChange = { coverSetupViewModel.onRucChange(it) },
                label = { Text(stringResource(id = R.string.cover_setup_ruc_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = coverConfig.subtitleStyle.content,
                onValueChange = { coverSetupViewModel.onAddressChange(it) },
                label = { Text(stringResource(id = R.string.cover_setup_address_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
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
                    contentDescription = "Imagen principal seleccionada", // TODO: Externalize
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f) // Proporción de ejemplo, ajustar según necesidad
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline),
                    contentScale = ContentScale.Fit // O ContentScale.Crop según el diseño deseado
                )
            } else {
                Box( // Placeholder si no hay imagen
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ninguna imagen seleccionada", style = MaterialTheme.typography.bodySmall) // TODO: Externalize
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                stringResource(id = R.string.cover_setup_text_customization_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Personalización para Nombre del Cliente
            TextCustomizationSection(
                label = stringResource(id = R.string.field_client_name),
                textStyleConfig = coverConfig.clientNameStyle,
                onTextStyleChange = { newSize, newAlign, newColor ->
                    viewModel.onTextStyleChange(DefaultCoverConfig.CLIENT_NAME_ID, newSize, newAlign, newColor)
                }
            )

            // Personalización para RUC
            TextCustomizationSection(
                label = stringResource(id = R.string.field_ruc),
                textStyleConfig = coverConfig.rucStyle,
                onTextStyleChange = { newSize, newAlign, newColor ->
                    viewModel.onTextStyleChange(DefaultCoverConfig.RUC_ID, newSize, newAlign, newColor)
                }
            )

            // Personalización para Dirección
            TextCustomizationSection(
                label = stringResource(id = R.string.field_address),
                textStyleConfig = coverConfig.subtitleStyle,
                onTextStyleChange = { newSize, newAlign, newColor ->
                    viewModel.onTextStyleChange(DefaultCoverConfig.SUBTITLE_ID, newSize, newAlign, newColor)
                }
            )


            // Personalización para Dirección
            TextCustomizationSection(
                label = stringResource(id = R.string.field_address),
                textStyleConfig = coverConfig.subtitleStyle,
                onTextStyleChange = { newSize, newAlign ->
                    viewModel.onTextStyleChange(DefaultCoverConfig.SUBTITLE_ID, newSize, newAlign)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            BorderCustomizationSection(
                borderColor = coverConfig.borderColor,
                borderVisibleTop = coverConfig.borderVisibleTop,
                borderVisibleBottom = coverConfig.borderVisibleBottom,
                borderVisibleLeft = coverConfig.borderVisibleLeft,
                borderVisibleRight = coverConfig.borderVisibleRight,
                onBorderColorChange = { viewModel.onBorderColorChange(it) },
                onBorderVisibilityChange = { top, bottom, left, right ->
                    viewModel.onBorderVisibilityChange(top, bottom, left, right)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            MarginCustomizationSection(
                marginTop = coverConfig.marginTop,
                marginBottom = coverConfig.marginBottom,
                marginLeft = coverConfig.marginLeft,
                marginRight = coverConfig.marginRight,
                onMarginChange = { top, bottom, left, right ->
                    viewModel.onMarginChange(top, bottom, left, right)
                }
            )

            // Aquí se añadirán más adelante:
            // - Vista previa de la portada (maqueta)
            // - Botones para guardar/cargar plantillas
            Spacer(modifier = Modifier.height(24.dp)) // Espacio al final
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
    var currentFontSizeSlider by remember(textStyleConfig.fontSize) { mutableStateOf(textStyleConfig.fontSize.value) }
    val colorOptions = mapOf(
        stringResource(R.string.color_black) to Color.Black,
        stringResource(R.string.color_gray) to Color.Gray,
        // stringResource(R.string.color_white) to Color.White, // Blanco puede ser problemático en tema claro
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

        // Slider para Tamaño de Fuente
        Text(stringResource(id = R.string.cover_setup_font_size_label, currentFontSizeSlider))
        Slider(
            value = currentFontSizeSlider,
            onValueChange = { currentFontSizeSlider = it },
            valueRange = 8f..72f, // Rango de tamaño de fuente (ej: 8sp a 72sp)
            steps = 63, // (72-8) / 1 step
            onValueChangeFinished = {
                onTextStyleChange(currentFontSizeSlider, null, null)
            }
        )

        // SegmentedButton para Alineación
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

        // Selector de Color del Texto
        Text(stringResource(id = R.string.cover_setup_text_color_label))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            colorOptions.forEach { (name, colorValue) ->
                OutlinedButton(
                    onClick = { onTextStyleChange(null, null, colorValue) },
                    modifier = Modifier.padding(horizontal = 2.dp),
                    border = if (textStyleConfig.fontColor == colorValue) ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp) else ButtonDefaults.outlinedButtonBorder,
                ) {
                    Box(modifier = Modifier.size(20.dp).background(colorValue))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(name, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        // TODO: Añadir selector de Familia de Fuentes (si se implementa)
    }
}


@Preview(showBackground = true)
@Composable
fun CoverSetupScreenPreviewWithImage() {
    DynamicCollageTheme {
        val previewViewModel = CoverSetupViewModel()
        // Simular una imagen seleccionada para la preview
        // previewViewModel.onMainImageSelected(android.net.Uri.EMPTY)
        CoverSetupScreen(navController = rememberNavController(), viewModel = previewViewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun CoverSetupScreenPreviewWithoutImage() {
    DynamicCollageTheme {
        CoverSetupScreen(rememberNavController(), viewModel = CoverSetupViewModel())
    }
}

@Composable
fun MarginCustomizationSection(
    marginTop: Dp,
    marginBottom: Dp,
    marginLeft: Dp,
    marginRight: Dp,
    onMarginChange: (top: String?, bottom: String?, left: String?, right: String?) -> Unit
) {
    // Factor de conversión para mostrar de Dp a cm en la UI (aproximado)
    val dpToCmRatio = 1f / 37.8f

    // Estados locales para los TextFields, inicializados con el valor convertido de Dp a cm
    var topInput by remember(marginTop) { mutableStateOf((marginTop.value * dpToCmRatio).toString()) }
    var bottomInput by remember(marginBottom) { mutableStateOf((marginBottom.value * dpToCmRatio).toString()) }
    var leftInput by remember(marginLeft) { mutableStateOf((marginLeft.value * dpToCmRatio).toString()) }
    var rightInput by remember(marginRight) { mutableStateOf((marginRight.value * dpToCmRatio).toString()) }

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
        // Se pueden añadir más colores o un selector de color más avanzado
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

        // Selector de Color del Borde
        Text(stringResource(id = R.string.cover_setup_border_color_label))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
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

        // Visibilidad de Lados del Borde
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


@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CoverSetupScreenDarkPreview() {
    DynamicCollageTheme(darkTheme = true) {
        CoverSetupScreen(rememberNavController(), viewModel = CoverSetupViewModel())
    }
}
