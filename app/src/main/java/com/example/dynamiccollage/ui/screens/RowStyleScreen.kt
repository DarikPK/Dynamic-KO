package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.RowStyle
import com.example.dynamiccollage.ui.components.ColorPickerDialog
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel
import com.example.dynamiccollage.viewmodel.RowStyleViewModel
import com.example.dynamiccollage.viewmodel.RowType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowStyleScreen(
    navController: NavController,
    projectViewModel: com.example.dynamiccollage.viewmodel.ProjectViewModel,
    coverSetupViewModel: CoverSetupViewModel,
    rowStyleViewModel: RowStyleViewModel = viewModel()
) {
    val coverConfig by coverSetupViewModel.coverConfig.collectAsState()

    LaunchedEffect(Unit) {
        rowStyleViewModel.loadStyles(
            client = coverConfig.clientNameStyle.rowStyle,
            ruc = coverConfig.rucStyle.rowStyle,
            address = coverConfig.subtitleStyle.rowStyle,
            photo = coverConfig.photoStyle
        )
    }

    val clientStyle by rowStyleViewModel.clientRowStyle.collectAsState()
    val rucStyle by rowStyleViewModel.rucRowStyle.collectAsState()
    val addressStyle by rowStyleViewModel.addressRowStyle.collectAsState()
    val photoStyle by rowStyleViewModel.photoRowStyle.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Estilos de Fila") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val updatedConfig = coverConfig.copy(
                            clientNameStyle = coverConfig.clientNameStyle.copy(rowStyle = clientStyle),
                            rucStyle = coverConfig.rucStyle.copy(rowStyle = rucStyle),
                            subtitleStyle = coverConfig.subtitleStyle.copy(rowStyle = addressStyle),
                            photoStyle = photoStyle
                        )
                        projectViewModel.updateCoverConfig(updatedConfig)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Guardar")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RowStyleCustomizationSection(
                title = "Fila Cliente",
                rowStyle = clientStyle,
                onBackgroundColorChange = { color -> rowStyleViewModel.updateBackgroundColor(RowType.CLIENT, color) },
                onPaddingChange = { top, bottom, left, right -> rowStyleViewModel.updatePadding(RowType.CLIENT, top, bottom, left, right) },
                onBorderColorChange = { color -> rowStyleViewModel.updateBorderColor(RowType.CLIENT, color) },
                onBorderThicknessChange = { thickness -> rowStyleViewModel.updateBorderThickness(RowType.CLIENT, thickness) },
                onBorderVisibilityChange = { t, b, l, r -> rowStyleViewModel.updateBorderVisibility(RowType.CLIENT, t, b, l, r) }
            )
            RowStyleCustomizationSection(
                title = "Fila RUC",
                rowStyle = rucStyle,
                onBackgroundColorChange = { color -> rowStyleViewModel.updateBackgroundColor(RowType.RUC, color) },
                onPaddingChange = { top, bottom, left, right -> rowStyleViewModel.updatePadding(RowType.RUC, top, bottom, left, right) },
                onBorderColorChange = { color -> rowStyleViewModel.updateBorderColor(RowType.RUC, color) },
                onBorderThicknessChange = { thickness -> rowStyleViewModel.updateBorderThickness(RowType.RUC, thickness) },
                onBorderVisibilityChange = { t, b, l, r -> rowStyleViewModel.updateBorderVisibility(RowType.RUC, t, b, l, r) }
            )
            RowStyleCustomizationSection(
                title = "Fila Dirección",
                rowStyle = addressStyle,
                onBackgroundColorChange = { color -> rowStyleViewModel.updateBackgroundColor(RowType.ADDRESS, color) },
                onPaddingChange = { top, bottom, left, right -> rowStyleViewModel.updatePadding(RowType.ADDRESS, top, bottom, left, right) },
                onBorderColorChange = { color -> rowStyleViewModel.updateBorderColor(RowType.ADDRESS, color) },
                onBorderThicknessChange = { thickness -> rowStyleViewModel.updateBorderThickness(RowType.ADDRESS, thickness) },
                onBorderVisibilityChange = { t, b, l, r -> rowStyleViewModel.updateBorderVisibility(RowType.ADDRESS, t, b, l, r) }
            )
            RowStyleCustomizationSection(
                title = "Fila Foto",
                rowStyle = photoStyle,
                onBackgroundColorChange = { color -> rowStyleViewModel.updateBackgroundColor(RowType.PHOTO, color) },
                onPaddingChange = { top, bottom, left, right -> rowStyleViewModel.updatePadding(RowType.PHOTO, top, bottom, left, right) },
                onBorderColorChange = { color -> rowStyleViewModel.updateBorderColor(RowType.PHOTO, color) },
                onBorderThicknessChange = { thickness -> rowStyleViewModel.updateBorderThickness(RowType.PHOTO, thickness) },
                onBorderVisibilityChange = { t, b, l, r -> rowStyleViewModel.updateBorderVisibility(RowType.PHOTO, t, b, l, r) }
            )
        }
    }
}

@Composable
fun RowStyleCustomizationSection(
    title: String,
    rowStyle: RowStyle,
    onBackgroundColorChange: (Color) -> Unit,
    onPaddingChange: (top: String?, bottom: String?, left: String?, right: String?) -> Unit,
    onBorderColorChange: (Color) -> Unit,
    onBorderThicknessChange: (String) -> Unit,
    onBorderVisibilityChange: (top: Boolean?, bottom: Boolean?, left: Boolean?, right: Boolean?) -> Unit
) {
    var topPaddingInput by remember(rowStyle.padding.top) { mutableStateOf(rowStyle.padding.top.toString()) }
    var bottomPaddingInput by remember(rowStyle.padding.bottom) { mutableStateOf(rowStyle.padding.bottom.toString()) }
    var leftPaddingInput by remember(rowStyle.padding.left) { mutableStateOf(rowStyle.padding.left.toString()) }
    var rightPaddingInput by remember(rowStyle.padding.right) { mutableStateOf(rowStyle.padding.right.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))

        // Background Color
        ColorSelector(
            label = "Color de Fondo",
            selectedColor = rowStyle.backgroundColor,
            onColorSelected = onBackgroundColorChange
        )

        // Padding
        Text("Padding (puntos)", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = topPaddingInput,
                onValueChange = {
                    topPaddingInput = it
                    onPaddingChange(it, null, null, null)
                },
                label = { Text("Superior") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = bottomPaddingInput,
                onValueChange = {
                    bottomPaddingInput = it
                    onPaddingChange(null, it, null, null)
                },
                label = { Text("Inferior") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = leftPaddingInput,
                onValueChange = {
                    leftPaddingInput = it
                    onPaddingChange(null, null, it, null)
                },
                label = { Text("Izquierdo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = rightPaddingInput,
                onValueChange = {
                    rightPaddingInput = it
                    onPaddingChange(null, null, null, it)
                },
                label = { Text("Derecho") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }


        Divider()

        // Border Properties
        Text("Bordes", style = MaterialTheme.typography.titleMedium)

        ColorSelector(
            label = "Color de Borde",
            selectedColor = rowStyle.border.color,
            onColorSelected = onBorderColorChange
        )

        var thicknessInput by remember(rowStyle.border.thickness) { mutableStateOf(rowStyle.border.thickness.toString()) }
        OutlinedTextField(
            value = thicknessInput,
            onValueChange = {
                thicknessInput = it
                onBorderThicknessChange(it)
            },
            label = { Text("Grosor de Borde (puntos)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            BorderCheckbox("Arriba", rowStyle.border.top) { onBorderVisibilityChange(it, null, null, null) }
            BorderCheckbox("Abajo", rowStyle.border.bottom) { onBorderVisibilityChange(null, it, null, null) }
            BorderCheckbox("Izquierda", rowStyle.border.left) { onBorderVisibilityChange(null, null, it, null) }
            BorderCheckbox("Derecha", rowStyle.border.right) { onBorderVisibilityChange(null, null, null, it) }
        }
    }
}

@Composable
fun ColorSelector(label: String, selectedColor: Color, onColorSelected: (Color) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ColorPickerDialog(
            onDismissRequest = { showDialog = false },
            onColorSelected = {
                onColorSelected(it)
                showDialog = false
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Button(onClick = { showDialog = true }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(selectedColor)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface)
                )
                Text("Cambiar Color")
            }
        }
    }
}

@Composable
fun BorderCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}
