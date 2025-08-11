package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.viewmodel.InnerPagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupHeaderStyleScreen(
    navController: NavController,
    viewModel: InnerPagesViewModel
) {
    val editingGroup by viewModel.editingGroup.collectAsState()
    val textStyle = editingGroup?.optionalTextStyle

    // Listener para el resultado del ColorPickerScreen
    LaunchedEffect(key1 = navController.currentBackStackEntry) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("selected_color")
            ?.observe(navController.currentBackStackEntry!!) { result ->
                val parts = result.split(":")
                // val fieldId = parts[0] // No es necesario aquí, solo hay un campo de color
                val colorHex = parts[1]
                val newColor = Color(android.graphics.Color.parseColor("#$colorHex"))
                viewModel.onEditingGroupFontColorChange(newColor)
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_color")
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Estilo de Encabezado") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (textStyle != null) {
            var fontSizeInput by remember(textStyle.fontSize) { mutableStateOf(textStyle.fontSize.toString()) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = textStyle.content,
                    onValueChange = { viewModel.onEditingGroupOptionalTextChange(it) },
                    label = { Text("Texto del Encabezado") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Poner en mayúsculas")
                    Switch(
                        checked = textStyle.allCaps,
                        onCheckedChange = { viewModel.onEditingGroupOptionalTextAllCapsChange(it) }
                    )
                }

                Divider()

                OutlinedTextField(
                    value = fontSizeInput,
                    onValueChange = {
                        fontSizeInput = it
                        viewModel.onEditingGroupFontSizeChange(it)
                    },
                    label = { Text("Tamaño de Fuente") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Alineación del Texto")
                val alignmentOptions = listOf(
                    TextAlign.Start to "Izquierda",
                    TextAlign.Center to "Centro",
                    TextAlign.End to "Derecha"
                )
                val selectedAlignmentIndex = alignmentOptions.indexOfFirst { it.first == textStyle.textAlign }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    alignmentOptions.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = index == selectedAlignmentIndex,
                            onClick = { viewModel.onEditingGroupTextAlignChange(option.first) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = alignmentOptions.size)
                        ) {
                            Text(option.second)
                        }
                    }
                }

                Text("Color del Texto")
                OutlinedButton(
                    onClick = {
                        val colorHex = String.format("%06X", (0xFFFFFF and textStyle.fontColor.toArgb()))
                        navController.navigate(Screen.ColorPicker.withArgs(textStyle.id, colorHex))
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
                                .background(textStyle.fontColor, shape = CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay un grupo en edición.")
            }
        }
    }
}
