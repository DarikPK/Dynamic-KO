package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
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

    val canvasSize = remember { mutableStateOf(0f) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(touchPosition) {
        touchPosition?.let { pos ->
            val size = canvasSize.value
            if (size > 0) {
                val centerX = size / 2f
                val centerY = size / 2f
                val radius = size / 2f

                val dx = pos.x - centerX
                val dy = pos.y - centerY
                val d = sqrt(dx * dx + dy * dy)

                val newSaturation = (d / radius).coerceIn(0f, 1f)
                saturation = newSaturation

                val angle = atan2(dy, dx)
                val newHue = (Math.toDegrees(angle.toDouble()).toFloat() + 360) % 360
                hue = newHue

                currentColor = Color.hsv(newHue, newSaturation, value)
            }
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

            // Rueda de Color (Estática por ahora)
            val hueBrush = Brush.sweepGradient(
                colors = listOf(
                    Color.Red, Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow, Color.Red
                ),
                center = Offset.Zero
            )
            val saturationBrush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent),
                center = Offset.Zero,
                radius = 0f
            )

            var touchPosition by remember { mutableStateOf<Offset?>(null) }
            val canvasSize = remember { mutableStateOf(0f) }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> touchPosition = offset },
                            onDrag = { change, _ -> touchPosition = change.position }
                        )
                    }
            ) {
                canvasSize.value = size.minDimension
                // El centro del Canvas será el nuevo origen (0,0) para los gradientes
                val canvasSize = size.minDimension
                val center = Offset(canvasSize / 2, canvasSize / 2)
                val radius = canvasSize / 2

                val translatedHueBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Red, Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow, Color.Red
                    ),
                    center = center
                )
                val translatedSaturationBrush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = radius
                )

                drawCircle(brush = translatedHueBrush, radius = radius, center = center)
                drawCircle(brush = translatedSaturationBrush, radius = radius, center = center)
                // Borde exterior
                drawCircle(color = Color.LightGray, radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))

                // Dibujar el indicador visual (selector)
                val angle = Math.toRadians(hue.toDouble())
                val r = saturation * radius
                val selectorX = center.x + (r * cos(angle)).toFloat()
                val selectorY = center.y + (r * sin(angle)).toFloat()
                val selectorPosition = Offset(selectorX, selectorY)

                drawCircle(color = Color.Black, radius = 10.dp.toPx(), center = selectorPosition, style = Stroke(width = 3.dp.toPx()))
                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = selectorPosition, style = Stroke(width = 1.dp.toPx()))
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
