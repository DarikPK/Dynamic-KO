package com.example.dynamiccollage.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.roundToInt

private val rainbowColors = listOf(
    Color.Red, Color(0xFFFFA500) /*Orange*/, Color.Yellow, Color.Green,
    Color.Blue, Color(0xFF4B0082) /*Indigo*/, Color(0xFF9400D3) /*Violet*/
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColorPickerScreen(
    navController: NavController,
    fieldId: String,
    initialColorHex: String
) {
    val initialColor = Color(android.graphics.Color.parseColor("#$initialColorHex"))
    var selectedBaseColor by remember { mutableStateOf(initialColor) }
    var finalColor by remember { mutableStateOf(initialColor) }
    var sliderPosition by remember { mutableStateOf(0.5f) }

    var rInput by remember { mutableStateOf("") }
    var gInput by remember { mutableStateOf("") }
    var bInput by remember { mutableStateOf("") }

    LaunchedEffect(finalColor) {
        rInput = (finalColor.red * 255).roundToInt().toString()
        gInput = (finalColor.green * 255).roundToInt().toString()
        bInput = (finalColor.blue * 255).roundToInt().toString()
    }

    LaunchedEffect(sliderPosition, selectedBaseColor) {
        val t = sliderPosition
        finalColor = if (t < 0.5f) {
            lerp(Color.Black, selectedBaseColor, t * 2)
        } else {
            lerp(selectedBaseColor, Color.White, (t - 0.5f) * 2)
        }
    }

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
                        val colorHex = String.format("%06X", (0xFFFFFF and finalColor.toArgb()))
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
                    .background(finalColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            )

            Text("Colores Base")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 7
            ) {
                rainbowColors.forEach { color ->
                    val isSelected = selectedBaseColor.toArgb() == color.toArgb()
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                selectedBaseColor = color
                                sliderPosition = 0.5f // Reset slider
                                finalColor = color
                            }
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }

            Divider()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tonalidad (Claro/Oscuro)")
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    modifier = Modifier.fillMaxWidth(),
                    track = {
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color.Black, selectedBaseColor, Color.White)
                                    ),
                                    shape = CircleShape
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }
                )
            }


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
                        val g = gInput.toIntOrNull()?.coerceIn(0, 255)
                        val b = bInput.toIntOrNull()?.coerceIn(0, 255)
                        if (r != null && g != null && b != null) {
                            finalColor = Color(r, g, b)
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
                        val r = rInput.toIntOrNull()?.coerceIn(0, 255)
                        val g = it.toIntOrNull()?.coerceIn(0, 255)
                        val b = bInput.toIntOrNull()?.coerceIn(0, 255)
                        if (r != null && g != null && b != null) {
                            finalColor = Color(r, g, b)
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
                        val r = rInput.toIntOrNull()?.coerceIn(0, 255)
                        val g = gInput.toIntOrNull()?.coerceIn(0, 255)
                        val b = it.toIntOrNull()?.coerceIn(0, 255)
                        if (r != null && g != null && b != null) {
                            finalColor = Color(r, g, b)
                        }
                    },
                    label = { Text("B") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
