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
            BackgroundPatternType.LOW_POLY -> drawLowPoly(canvas, config, width, height, paint, colors)
            BackgroundPatternType.CRYSTALS -> drawCrystals(canvas, config, width, height, paint, colors)
            BackgroundPatternType.SOFT_WAVES -> drawSoftWaves(canvas, config, width, height, paint, colors)
            BackgroundPatternType.BOKEH -> drawBokeh(canvas, config, width, height, paint, colors)
            BackgroundPatternType.HEXAGONS -> drawHexagons(canvas, config, width, height, paint, colors)
            BackgroundPatternType.PAPER_LAYERS -> drawPaperLayers(canvas, config, width, height, paint, colors)
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
