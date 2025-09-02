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
    val initialTextStyle = editingGroup?.optionalTextStyle

    if (initialTextStyle == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay un grupo en edición.")
        }
        return
    }

    var localTextStyle by remember { mutableStateOf(initialTextStyle) }

    // Listener para el resultado del ColorPickerScreen
    LaunchedEffect(key1 = navController.currentBackStackEntry) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("selected_color")
            ?.observe(navController.currentBackStackEntry!!) { result ->
                val parts = result.split(":")
                val colorHex = parts[1]
                val newColor = Color(android.graphics.Color.parseColor("#$colorHex"))
                localTextStyle = localTextStyle.copy(fontColor = newColor)
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
        var fontSizeInput by remember(localTextStyle.fontSize) { mutableStateOf(localTextStyle.fontSize.toString()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = localTextStyle.content,
                onValueChange = { localTextStyle = localTextStyle.copy(content = it) },
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
                    checked = localTextStyle.allCaps,
                    onCheckedChange = { localTextStyle = localTextStyle.copy(allCaps = it) }
                )
            }

            Divider()

            OutlinedTextField(
                value = fontSizeInput,
                onValueChange = {
                    fontSizeInput = it
                    val newSize = it.toIntOrNull() ?: 0
                    localTextStyle = localTextStyle.copy(fontSize = newSize)
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
            val selectedAlignmentIndex = alignmentOptions.indexOfFirst { it.first == localTextStyle.textAlign }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                alignmentOptions.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = index == selectedAlignmentIndex,
                        onClick = { localTextStyle = localTextStyle.copy(textAlign = option.first) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = alignmentOptions.size)
                    ) {
                        Text(option.second)
                    }
                }
            }

            Text("Color del Texto")
            OutlinedButton(
                onClick = {
                    val colorHex = String.format("%06X", (0xFFFFFF and localTextStyle.fontColor.toArgb()))
                    navController.navigate(Screen.ColorPicker.withArgs(localTextStyle.id, colorHex))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Color Actual", color = MaterialTheme.colorScheme.onSurface)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(localTextStyle.fontColor, shape = CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.onEditingGroupHeaderStyleChange(localTextStyle)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar y Regresar")
            }
        }
    }
}
