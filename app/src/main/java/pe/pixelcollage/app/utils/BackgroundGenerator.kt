package pe.pixelcollage.app.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import pe.pixelcollage.app.data.model.BackgroundPatternType
import pe.pixelcollage.app.data.model.ColorTheme
import pe.pixelcollage.app.data.model.GeneratedBackgroundConfig
import android.graphics.Color
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object BackgroundGenerator {
    fun drawGeneratedBackground(
        canvas: Canvas,
        config: GeneratedBackgroundConfig,
        width: Float,
        height: Float,
        colorTheme: ColorTheme
    ) {
        if (!config.enabled) return

        val paint = Paint().apply {
            isAntiAlias = true
            alpha = (config.opacity * 255).toInt()
        }

        // Use the specified color if available, otherwise use a list from the theme
        val colors = config.color?.let { listOf(it) } ?: listOf(
            colorTheme.textColor.hashCode(),
            colorTheme.rucBackgroundColor.hashCode(),
            colorTheme.borderColor.hashCode()
        )

        when (config.patternType) {
            BackgroundPatternType.NONE -> { /* No-op, do nothing */ }
            // Original Patterns
            BackgroundPatternType.CURVAS -> drawCurves(canvas, config, width, height, paint, colors)
            BackgroundPatternType.FIGURAS_GEOMETRICAS -> drawGeometricShapes(canvas, config, width, height, paint, colors)
            BackgroundPatternType.LINEAS -> drawLines(canvas, config, width, height, paint, colors)
            BackgroundPatternType.PUNTOS -> drawDots(canvas, config, width, height, paint, colors)
            BackgroundPatternType.ESPIRALES -> drawSpirals(canvas, config, width, height, paint, colors)
            BackgroundPatternType.ONDAS -> drawWaves(canvas, config, width, height, paint, colors)
            BackgroundPatternType.ONDAS_ALEATORIAS -> drawRandomWaves(canvas, config, width, height, paint, colors)
            BackgroundPatternType.BURBUJAS -> drawBubbles(canvas, config, width, height, paint, colors)
            BackgroundPatternType.ESTRELLAS -> drawStars(canvas, config, width, height, paint, colors)
            BackgroundPatternType.MOSAICO -> drawMosaic(canvas, config, width, height, paint, colors)
            // New Image-based Patterns
            BackgroundPatternType.LOW_POLY -> drawLowPoly(canvas, config, width, height, paint, colors)
            BackgroundPatternType.CRYSTALS -> drawCrystals(canvas, config, width, height, paint, colors)
            BackgroundPatternType.SOFT_WAVES -> drawSoftWaves(canvas, config, width, height, paint, colors)
            BackgroundPatternType.BOKEH -> drawBokeh(canvas, config, width, height, paint, colors)
            BackgroundPatternType.HEXAGONS -> drawHexagons(canvas, config, width, height, paint, colors)
            BackgroundPatternType.PAPER_LAYERS -> drawPaperLayers(canvas, config, width, height, paint, colors)
        }
    }

    private fun drawCurves(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numElements = (width * height / 10000 * config.density).toInt()

        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            paint.strokeWidth = random.nextFloat() * config.size
            paint.style = Paint.Style.STROKE

            val path = Path()
            path.moveTo(random.nextFloat() * width, random.nextFloat() * height)
            path.cubicTo(
                random.nextFloat() * width, random.nextFloat() * height,
                random.nextFloat() * width, random.nextFloat() * height,
                random.nextFloat() * width, random.nextFloat() * height
            )
            canvas.drawPath(path, paint)
        }
    }

    private fun drawGeometricShapes(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numElements = (width * height / 10000 * config.density).toInt()

        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            paint.style = if (random.nextBoolean()) Paint.Style.FILL else Paint.Style.STROKE
            paint.strokeWidth = random.nextFloat() * 5 + 1

            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val size = random.nextFloat() * config.size * 2

            canvas.save()
            canvas.rotate(random.nextFloat() * 360, x, y)

            when (random.nextInt(3)) {
                0 -> canvas.drawRect(x - size / 2, y - size / 2, x + size / 2, y + size / 2, paint)
                1 -> canvas.drawCircle(x, y, size / 2, paint)
                2 -> {
                    val path = Path()
                    path.moveTo(x, y - size / 2)
                    path.lineTo(x + size / 2, y + size / 2)
                    path.lineTo(x - size / 2, y + size / 2)
                    path.close()
                    canvas.drawPath(path, paint)
                }
            }
            canvas.restore()
        }
    }

    private fun drawLines(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numElements = (width * height / 15000 * config.density).toInt()
        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            paint.strokeWidth = random.nextFloat() * config.size
            val x1 = random.nextFloat() * width
            val y1 = random.nextFloat() * height
            val x2 = random.nextFloat() * width
            val y2 = random.nextFloat() * height
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
    }

    private fun drawDots(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        paint.style = Paint.Style.FILL
        val numElements = (width * height / 1000 * config.density).toInt()
        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            canvas.drawCircle(random.nextFloat() * width, random.nextFloat() * height, random.nextFloat() * config.size, paint)
        }
    }

    private fun drawSpirals(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numElements = (width * height / 50000 * config.density).toInt().coerceAtLeast(1)
        paint.style = Paint.Style.STROKE

        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            paint.strokeWidth = random.nextFloat() * config.size / 2f + 1f

            val centerX = random.nextFloat() * width
            val centerY = random.nextFloat() * height
            val path = Path()
            path.moveTo(centerX, centerY)

            val maxRadius = config.size * 5 * (random.nextFloat() * 0.5f + 0.5f)
            val coils = 5f
            var angle = 0f
            while (angle < coils * 2 * Math.PI) {
                val radius = maxRadius * (angle / (coils * 2 * Math.PI.toFloat()))
                val x = centerX + radius * cos(angle)
                val y = centerY + radius * sin(angle)
                path.lineTo(x, y)
                angle += 0.1f
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawWaves(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numElements = (height / (config.size * 2) * config.density).toInt().coerceAtLeast(1)
        paint.style = Paint.Style.STROKE

        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            paint.strokeWidth = random.nextFloat() * config.size + 1f

            val path = Path()
            val startY = random.nextFloat() * height
            path.moveTo(-width * 0.1f, startY)

            val amplitude = config.size * (random.nextFloat() + 0.5f)
            val frequency = random.nextFloat() * 0.05f + 0.01f
            var x = 0f
            while (x < width * 1.1f) {
                val y = startY + amplitude * sin(x * frequency)
                path.lineTo(x, y)
                x += 10f
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawRandomWaves(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numElements = (height / (config.size * 2) * config.density).toInt().coerceAtLeast(1)
        paint.style = Paint.Style.STROKE

        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            paint.strokeWidth = random.nextFloat() * config.size + 1f

            val path = Path()
            val startY = random.nextFloat() * height
            path.moveTo(-width * 0.1f, startY)

            val amplitude = config.size * (random.nextFloat() + 0.5f)
            val frequency = random.nextFloat() * 0.05f + 0.01f
            var x = 0f
            while (x < width * 1.1f) {
                val y = startY + amplitude * sin(x * frequency)
                path.lineTo(x, y)
                x += 10f
            }
            canvas.save()
            canvas.rotate(random.nextFloat() * 360, width / 2, height / 2)
            canvas.drawPath(path, paint)
            canvas.restore()
        }
    }

    private fun drawBubbles(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        paint.style = Paint.Style.STROKE
        val numElements = (width * height / 10000 * config.density).toInt()
        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            paint.alpha = (config.opacity * 255).toInt()
            paint.strokeWidth = random.nextFloat() * config.size / 2f + 1f
            canvas.drawCircle(random.nextFloat() * width, random.nextFloat() * height, random.nextFloat() * config.size * 2, paint)
        }
    }

    private fun drawStars(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        paint.style = Paint.Style.FILL
        val numElements = (width * height / 10000 * config.density).toInt()
        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            paint.alpha = (config.opacity * 255).toInt()
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val size = random.nextFloat() * config.size * 2
            val path = Path()
            path.moveTo(x, y - size)
            path.lineTo(x + size * 0.2f, y - size * 0.2f)
            path.lineTo(x + size, y)
            path.lineTo(x + size * 0.2f, y + size * 0.2f)
            path.lineTo(x, y + size)
            path.lineTo(x - size * 0.2f, y + size * 0.2f)
            path.lineTo(x - size, y)
            path.lineTo(x - size * 0.2f, y - size * 0.2f)
            path.close()
            canvas.save()
            canvas.rotate(random.nextFloat() * 360, x, y)
            canvas.drawPath(path, paint)
            canvas.restore()
        }
    }

    private fun drawMosaic(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        paint.style = Paint.Style.FILL
        val numElements = (width * height / 5000 * config.density).toInt()
        for (i in 0 until numElements) {
            paint.color = colors.random(random)
            paint.alpha = (config.opacity * 255).toInt()
            val x1 = random.nextFloat() * width
            val y1 = random.nextFloat() * height
            val x2 = x1 + (random.nextFloat() - 0.5f) * config.size * 5
            val y2 = y1 + (random.nextFloat() - 0.5f) * config.size * 5
            val x3 = x1 + (random.nextFloat() - 0.5f) * config.size * 5
            val y3 = y1 + (random.nextFloat() - 0.5f) * config.size * 5
            val path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.close()
            canvas.drawPath(path, paint)
        }
    }

    // --- New Image-Based Pattern Implementations ---

    private fun drawLowPoly(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numPoints = (30 * config.density).toInt()
        val points = List(numPoints) { Pair(random.nextFloat() * width, random.nextFloat() * height) } +
                listOf(Pair(0f, 0f), Pair(width, 0f), Pair(0f, height), Pair(width, height)) // Add corners for better coverage

        // Simple triangulation approximation by connecting nearby points
        for (i in points.indices) {
            for (j in i + 1 until points.size) {
                for (k in j + 1 until points.size) {
                    val p1 = points[i]
                    val p2 = points[j]
                    val p3 = points[k]

                    // Draw triangle if points are reasonably close
                    val dist1 = sqrt((p1.first - p2.first).pow(2) + (p1.second - p2.second).pow(2))
                    val dist2 = sqrt((p2.first - p3.first).pow(2) + (p2.second - p3.second).pow(2))
                    if (dist1 < width * 0.3 && dist2 < width * 0.3) {
                         paint.color = colors.random(random)
                         paint.style = Paint.Style.FILL

                         val path = Path()
                         path.moveTo(p1.first, p1.second)
                         path.lineTo(p2.first, p2.second)
                         path.lineTo(p3.first, p3.second)
                         path.close()
                         canvas.drawPath(path, paint)
                    }
                }
            }
        }
    }

    private fun drawCrystals(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numShapes = (50 * config.density).toInt()
        paint.style = Paint.Style.FILL

        for (i in 0 until numShapes) {
            val path = Path()
            val startX = random.nextFloat() * width
            val startY = random.nextFloat() * height
            path.moveTo(startX, startY)

            val numVertices = random.nextInt(3, 7)
            for (v in 0 until numVertices) {
                path.lineTo(
                    startX + (random.nextFloat() - 0.5f) * width * 0.4f * config.size / 20f,
                    startY + (random.nextFloat() - 0.5f) * height * 0.4f * config.size / 20f
                )
            }
            path.close()

            // Use a color with transparency to get the overlap effect
            val baseColor = android.graphics.Color.valueOf(colors.random(random))
            paint.setARGB(
                (config.opacity * 100).toInt(), // Lower alpha for transparency
                (baseColor.red() * 255).toInt(),
                (baseColor.green() * 255).toInt(),
                (baseColor.blue() * 255).toInt()
            )
            canvas.drawPath(path, paint)
        }
    }

    private fun drawSoftWaves(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numWaves = (10 * config.density).toInt()
        paint.style = Paint.Style.FILL

        for (i in 0 until numWaves) {
            val path = Path()
            val startY = random.nextFloat() * height
            val amplitude = random.nextFloat() * height * 0.3f * (config.size / 20f)
            val frequency = 0.005f + random.nextFloat() * 0.005f

            path.moveTo(0f, startY)
            var x = 0f
            while(x < width) {
                path.lineTo(x, startY + sin(x * frequency) * amplitude)
                x += 20
            }
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()

            val baseColor = android.graphics.Color.valueOf(colors.random(random))
            paint.setARGB(
                (config.opacity * 150).toInt(),
                (baseColor.red() * 255).toInt(),
                (baseColor.green() * 255).toInt(),
                (baseColor.blue() * 255).toInt()
            )
            canvas.drawPath(path, paint)
        }
    }

    private fun drawBokeh(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numCircles = (100 * config.density).toInt()
        paint.style = Paint.Style.FILL

        for (i in 0 until numCircles) {
            val centerX = random.nextFloat() * width
            val centerY = random.nextFloat() * height
            val radius = random.nextFloat() * 50f * (config.size / 10f)

            val baseColor = android.graphics.Color.valueOf(colors.random(random))
            paint.setARGB(
                (config.opacity * 80).toInt(), // Low alpha for bokeh effect
                (baseColor.red() * 255).toInt(),
                (baseColor.green() * 255).toInt(),
                (baseColor.blue() * 255).toInt()
            )
            canvas.drawCircle(centerX, centerY, radius, paint)
        }
    }

    private fun drawHexagons(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val hexSize = 30f * (config.size / 10f)
        paint.style = Paint.Style.FILL

        val hexWidth = hexSize * sqrt(3f)
        val hexHeight = hexSize * 2

        for (row in 0 until (height / (hexHeight * 0.75f)).toInt()) {
            for (col in 0 until (width / hexWidth).toInt()) {
                val x = col * hexWidth + if (row % 2 == 1) hexWidth / 2f else 0f
                val y = row * hexHeight * 0.75f

                if (random.nextFloat() < config.density) {
                    val path = createHexagonPath(x, y, hexSize)
                    val baseColor = android.graphics.Color.valueOf(colors.random(random))
                    paint.setARGB(
                         (config.opacity * 120).toInt(),
                         (baseColor.red() * 255).toInt(),
                         (baseColor.green() * 255).toInt(),
                         (baseColor.blue() * 255).toInt()
                    )
                    canvas.drawPath(path, paint)
                }
            }
        }
    }

    private fun createHexagonPath(x: Float, y: Float, size: Float): Path {
        val path = Path()
        val angle = (Math.PI / 3.0).toFloat() // 60 degrees
        path.moveTo(x + size * cos(0f), y + size * sin(0f))
        for (i in 1 until 6) {
            path.lineTo(x + size * cos(angle * i), y + size * sin(angle * i))
        }
        path.close()
        return path
    }


    private fun drawPaperLayers(canvas: Canvas, config: GeneratedBackgroundConfig, width: Float, height: Float, paint: Paint, colors: List<Int>) {
        val random = Random
        val numLayers = (25 * config.density).toInt()
        paint.style = Paint.Style.FILL

        for (i in 0 until numLayers) {
            canvas.save()
            val rotation = (random.nextFloat() - 0.5f) * 20f * (config.size / 10f)
            canvas.rotate(rotation, width / 2f, height / 2f)

            val baseColor = android.graphics.Color.valueOf(colors.random(random))
            paint.setARGB(
                (config.opacity * 255).toInt(),
                (baseColor.red() * 255).toInt(),
                (baseColor.green() * 255).toInt(),
                (baseColor.blue() * 255).toInt()
            )

            val offsetX = (random.nextFloat() - 0.5f) * width * 0.1f
            val offsetY = (random.nextFloat() - 0.5f) * height * 0.1f
            canvas.drawRect(offsetX, offsetY, width + offsetX, height + offsetY, paint)
            canvas.restore()
        }
    }

}
