package pe.pixelcollage.app.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import pe.pixelcollage.app.data.model.BackgroundPatternType
import pe.pixelcollage.app.data.model.ColorTheme
import pe.pixelcollage.app.data.model.GeneratedBackgroundConfig
import kotlin.math.cos
import kotlin.math.sin
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

        // Convert compose colors to Android graphics color integers
        val colors = listOf(
            colorTheme.textColor.hashCode(),
            colorTheme.rucBackgroundColor.hashCode(),
            colorTheme.borderColor.hashCode()
        )

        when (config.patternType) {
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
}
