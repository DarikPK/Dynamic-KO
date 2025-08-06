package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.lang.Float.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColorPickerScreen(
    navController: NavController,
    fieldId: String,
    initialColorHex: String
) {
    val initialColor = Color(android.graphics.Color.parseColor("#$initialColorHex"))
    var currentColor by remember { mutableStateOf(initialColor) }

    // State for HSV color model
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(currentColor.toArgb(), hsv)
    var hue by remember { mutableStateOf(hsv[0]) }
    var saturation by remember { mutableStateOf(hsv[1]) }
    var value by remember { mutableStateOf(hsv[2]) }

    // State for RGB text fields
    var rInput by remember { mutableStateOf((currentColor.red * 255).roundToInt().toString()) }
    var gInput by remember { mutableStateOf((currentColor.green * 255).roundToInt().toString()) }
    var bInput by remember { mutableStateOf((currentColor.blue * 255).roundToInt().toString()) }

    // Update everything when currentColor changes
    LaunchedEffect(currentColor) {
        android.graphics.Color.colorToHSV(currentColor.toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        rInput = (currentColor.red * 255).roundToInt().toString()
        gInput = (currentColor.green * 255).roundToInt().toString()
        bInput = (currentColor.blue * 255).roundToInt().toString()
    }

    val predefinedColors = listOf(
        Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
        Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
        Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B), Color.Black, Color.White
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Seleccionar Color") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val colorHex = String.format("%06X", (0xFFFFFF and currentColor.toArgb()))
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_color", "$fieldId:$colorHex")
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Confirmar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(currentColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            )

            Text("Colores Predefinidos")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                predefinedColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { currentColor = color }
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = rInput,
                    onValueChange = {
                        rInput = it
                        val r = it.toIntOrNull()?.coerceIn(0, 255)
                        if (r != null) {
                            currentColor = Color(r, (currentColor.green * 255).roundToInt(), (currentColor.blue * 255).roundToInt())
                        }
                    },
                    label = { Text("R") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = gInput,
                    onValueChange = {
                        gInput = it
                        val g = it.toIntOrNull()?.coerceIn(0, 255)
                        if (g != null) {
                            currentColor = Color((currentColor.red * 255).roundToInt(), g, (currentColor.blue * 255).roundToInt())
                        }
                    },
                    label = { Text("G") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bInput,
                    onValueChange = {
                        bInput = it
                        val b = it.toIntOrNull()?.coerceIn(0, 255)
                        if (b != null) {
                            currentColor = Color((currentColor.red * 255).roundToInt(), (currentColor.green * 255).roundToInt(), b)
                        }
                    },
                    label = { Text("B") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Brillo")
            Slider(
                value = value,
                onValueChange = {
                    value = it
                    currentColor = Color.hsv(hue, saturation, value)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
