package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.DefaultCoverConfig
import com.example.dynamiccollage.data.model.TextStyleConfig
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextCustomizationSection(
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
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            colorOptions.forEach { (name, colorValue) ->
                OutlinedButton(
                    onClick = { onTextStyleChange(null, null, colorValue) },
                    modifier = Modifier.padding(8.dp),
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
