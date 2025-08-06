package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColorPickerScreen(
    navController: NavController,
    fieldId: String,
    initialColorHex: String
) {
    val initialColor = Color(android.graphics.Color.parseColor("#$initialColorHex"))
    var currentColor by remember { mutableStateOf(initialColor) }

    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(currentColor.toArgb(), hsv)
    var hue by remember { mutableStateOf(hsv[0]) }
    var saturation by remember { mutableStateOf(hsv[1]) }
    var value by remember { mutableStateOf(hsv[2]) }

    var rInput by remember { mutableStateOf((currentColor.red * 255).roundToInt().toString()) }
    var gInput by remember { mutableStateOf((currentColor.green * 255).roundToInt().toString()) }
    var bInput by remember { mutableStateOf((currentColor.blue * 255).roundToInt().toString()) }

    LaunchedEffect(currentColor) {
        android.graphics.Color.colorToHSV(currentColor.toArgb(), hsv)
        if (hsv[0] != hue) hue = hsv[0]
        if (hsv[1] != saturation) saturation = hsv[1]
        if (hsv[2] != value) value = hsv[2]

        val r = (currentColor.red * 255).roundToInt().toString()
        val g = (currentColor.green * 255).roundToInt().toString()
        val b = (currentColor.blue * 255).roundToInt().toString()
        if (rInput != r) rInput = r
        if (gInput != g) gInput = g
        if (bInput != b) bInput = b
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

            val onColorFromWheel: (Offset, IntSize) -> Unit = { offset, size ->
                val canvasRadius = size.width / 2f
                val centerX = canvasRadius
                val centerY = canvasRadius

                val dx = offset.x - centerX
                val dy = offset.y - centerY
                val distance = sqrt(dx * dx + dy * dy)

                if (distance <= canvasRadius) {
                    val newSaturation = (distance / canvasRadius).coerceIn(0f, 1f)
                    val angle = atan2(dy, dx)
                    val newHue = (Math.toDegrees(angle.toDouble()).toFloat() + 360) % 360

                    hue = newHue
                    saturation = newSaturation
                    currentColor = Color.hsv(newHue, newSaturation, value)
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onColorFromWheel(it, size) },
                            onDrag = { change, _ -> onColorFromWheel(change.position, size) }
                        )
                    }
            ) {
                val canvasRadius = size.minDimension / 2f
                val center = Offset(canvasRadius, canvasRadius)

                val hueBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Red, Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow, Color.Red
                    ),
                    center = center
                )
                val saturationBrush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = canvasRadius
                )

                drawCircle(brush = hueBrush, radius = canvasRadius, center = center)
                drawCircle(brush = saturationBrush, radius = canvasRadius, center = center)
                drawCircle(color = Color.LightGray, radius = canvasRadius, center = center, style = Stroke(width = 2.dp.toPx()))

                val angle = Math.toRadians(hue.toDouble())
                val r = saturation * canvasRadius
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
