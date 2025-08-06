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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private fun generateRainbowPalette(): List<Color> {
    val colors = mutableListOf<Color>()
    // 7 tonos base del arcoiris (H en HSV)
    val hues = floatArrayOf(0f, 30f, 60f, 120f, 240f, 270f, 300f)
    val brightnessSteps = 5
    val saturation = 0.9f

    hues.forEach { hue ->
        (1..brightnessSteps).forEach { step ->
            val brightness = (step.toFloat() / brightnessSteps).coerceIn(0.1f, 1.0f)
            colors.add(Color.hsv(hue, saturation, brightness))
        }
    }
    return colors
}

private val predefinedColors = generateRainbowPalette()

private class HexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = size.minDimension / 2f
            moveTo(centerX + radius, centerY)
            for (i in 1..5) {
                val angle = i * 60.0 * (Math.PI / 180.0)
                lineTo(centerX + (radius * cos(angle)).toFloat(), centerY + (radius * sin(angle)).toFloat())
            }
            close()
        }
        return Outline.Generic(path)
    }
}

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

            Text("Paleta de Colores")
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                val boxWidth = this.maxWidth
                val center = Offset(boxWidth.value / 2, boxWidth.value / 2)
                val numHues = 7
                val numShades = 5
                val angleStep = 360f / numHues
                val maxRadius = boxWidth.value / 2 * 0.9f
                val radiusStep = maxRadius / numShades
                val hexagonSize = (radiusStep * 0.9f).dp

                predefinedColors.forEachIndexed { index, color ->
                    val hueIndex = index / numShades
                    val shadeIndex = index % numShades

                    val angle = Math.toRadians((hueIndex * angleStep).toDouble())
                    val radius = (shadeIndex + 1) * radiusStep

                    val x = center.x + (radius * cos(angle)).toFloat() - (hexagonSize.value / 2 * density.density)
                    val y = center.y + (radius * sin(angle)).toFloat() - (hexagonSize.value / 2 * density.density)

                    val isSelected = currentColor.toArgb() == color.toArgb()
                    Box(
                        modifier = Modifier
                            .offset(x = (x / density.density).dp, y = (y / density.density).dp)
                            .size(hexagonSize)
                            .clip(HexagonShape())
                            .background(color)
                            .clickable { currentColor = color }
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = HexagonShape()
                            )
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
