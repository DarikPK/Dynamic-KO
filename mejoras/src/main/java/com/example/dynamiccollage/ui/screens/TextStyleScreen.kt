package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.DefaultCoverConfig
import com.example.dynamiccollage.data.model.TextStyleConfig
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextStyleScreen(
    navController: NavController,
    coverSetupViewModel: CoverSetupViewModel,
    projectViewModel: ProjectViewModel
) {
    val coverConfig by coverSetupViewModel.coverConfig.collectAsState()
    val context = LocalContext.current

    // Listener para el resultado del ColorPickerScreen
    LaunchedEffect(key1 = navController.currentBackStackEntry) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("selected_color")
            ?.observe(navController.currentBackStackEntry!!) { result ->
                val parts = result.split(":")
                val fieldId = parts[0]
                val colorHex = parts[1]
                val newColor = Color(android.graphics.Color.parseColor("#$colorHex"))
                coverSetupViewModel.onTextStyleChange(fieldId, newColor = newColor)

                // Limpiar el estado para no volver a procesarlo
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_color")
            }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Estilo de Texto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        projectViewModel.updateCoverConfig(coverConfig)
                        Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Guardar Cambios")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            Divider()

            TextCustomizationSection(
                label = stringResource(id = R.string.field_client_name),
                fieldId = DefaultCoverConfig.CLIENT_NAME_ID,
                textStyleConfig = coverConfig.clientNameStyle,
                navController = navController,
                onTextStyleChange = { newSize, newAlign, newColor ->
                    coverSetupViewModel.onTextStyleChange(DefaultCoverConfig.CLIENT_NAME_ID, newSize, newAlign, newColor)
                }
            )

            TextCustomizationSection(
                label = coverConfig.documentType.name,
                fieldId = DefaultCoverConfig.RUC_ID,
                textStyleConfig = coverConfig.rucStyle,
                navController = navController,
                onTextStyleChange = { newSize, newAlign, newColor ->
                    coverSetupViewModel.onTextStyleChange(DefaultCoverConfig.RUC_ID, newSize, newAlign, newColor)
                }
            )

            TextCustomizationSection(
                label = stringResource(id = R.string.field_address),
                fieldId = DefaultCoverConfig.SUBTITLE_ID,
                textStyleConfig = coverConfig.subtitleStyle,
                navController = navController,
                onTextStyleChange = { newSize, newAlign, newColor ->
                    coverSetupViewModel.onTextStyleChange(DefaultCoverConfig.SUBTITLE_ID, newSize, newAlign, newColor)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TextCustomizationSection(
    label: String,
    fieldId: String,
    textStyleConfig: TextStyleConfig,
    navController: NavController,
    onTextStyleChange: (newSize: Float?, newAlign: TextAlign?, newColor: Color?) -> Unit
) {
    var fontSizeInput by remember(textStyleConfig.fontSize) { mutableStateOf(textStyleConfig.fontSize.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = fontSizeInput,
            onValueChange = {
                fontSizeInput = it
                it.toFloatOrNull()?.let { floatValue ->
                    onTextStyleChange(floatValue, null, null)
                }
            },
            label = { Text("Tamaño de Fuente") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
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

        OutlinedButton(
            onClick = {
                val colorHex = String.format("%06X", (0xFFFFFF and textStyleConfig.fontColor.toArgb()))
                navController.navigate(Screen.ColorPicker.withArgs(fieldId, colorHex))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Color Actual")
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(textStyleConfig.fontColor, shape = CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
        }
    }
}
