package pe.pixelcollage.app.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import pe.pixelcollage.app.data.model.BackgroundPatternType
import pe.pixelcollage.app.data.model.ColorTheme
import pe.pixelcollage.app.data.model.ColorThemes
import pe.pixelcollage.app.data.model.GeneratedBackgroundConfig
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object BackgroundGenerator {

    // Paletas de colores basadas en los temas
    private val colorPalettes = mapOf(
        "SkyBlue" to listOf(Color.valueOf(0xFFDBE5F1), Color.valueOf(0xFF73A1D3), Color.valueOf(0xFF2C74B5)),
        "Ruby Red" to listOf(Color.valueOf(0xFFF5D0D7), Color.valueOf(0xFFD46A7E), Color.valueOf(0xFF9B1B30)),
        "Emerald Green" to listOf(Color.valueOf(0xFFD4EEE6), Color.valueOf(0xFF50C878), Color.valueOf(0xFF009B77)),
        "Golden Sun" to listOf(Color.valueOf(0xFFFDF0D5), Color.valueOf(0xFFFBC02D), Color.valueOf(0xFFE49B0F)),
        "Amethyst Purple" to listOf(Color.valueOf(0xFFE9D6F5), Color.valueOf(0xFF9966CC), Color.valueOf(0xFF6A0DAD)),
        "Obsidian Black" to listOf(Color.valueOf(0xFFE0E0E0), Color.valueOf(0xFF808080), Color.valueOf(0xFF000000)),
        "Graphite Gray" to listOf(Color.valueOf(0xFFF0F0F0), Color.valueOf(0xFFA0A0A0), Color.valueOf(0xFF4C4C4C)),
        "Mocha Brown" to listOf(Color.valueOf(0xFFEAE0D9), Color.valueOf(0xFFB59477), Color.valueOf(0xFF6F4E37)),
        "Ocean Teal" to listOf(Color.valueOf(0xFFD4EBEB), Color.valueOf(0xFF48D1CC), Color.valueOf(0xFF008080)),
        "Crimson Velvet" to listOf(Color.valueOf(0xFFF5D0D0), Color.valueOf(0xFFDC143C), Color.valueOf(0xFF8B0000))
    )

    fun drawGeneratedBackground(
        canvas: Canvas,
        config: GeneratedBackgroundConfig,
        width: Float,
        height: Float,
        colorTheme: ColorTheme // Se mantiene por firma, pero usamos el del config
    ) {
        val backgroundColor = if (config.combineWithSolidColor || config.patternType == BackgroundPatternType.SÓLIDO) {
            config.solidColor.toArgb()
        } else {
            Color.WHITE
        }
        canvas.drawColor(backgroundColor)

        if (config.patternType == BackgroundPatternType.SÓLIDO) return

        val paint = Paint().apply { isAntiAlias = true }
        val random = if (config.isRandom) Random else Random(config.patternType.ordinal)
        val palette = colorPalettes[config.colorThemeName] ?: colorPalettes["SkyBlue"]!!

        when (config.patternType) {
            BackgroundPatternType.LOW_POLY -> drawLowPoly(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.CRISTALES -> drawCrystals(canvas, width, height, paint)
            BackgroundPatternType.GEOMETRICO -> drawGeometric(canvas, width, height, paint)
            BackgroundPatternType.PAPELES_SUPERPUESTOS -> drawOverlappingPapers(canvas, width, height, paint)
            BackgroundPatternType.ONDAS_ABSTRACTAS -> drawAbstractWaves(canvas, width, height, paint)
            BackgroundPatternType.BOKEH_DORADO -> drawGoldenBokeh(canvas, width, height, paint)
            else -> { /* No-op */ }
        }
    }

    private fun drawLowPoly(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Color>
    ) {
        val points = mutableListOf<Pair<Float, Float>>()
        // Density controla el número de puntos. Rango de 20 a 100
        val numPoints = (20 + (config.density * 8)).toInt()

        for (i in 0 until numPoints) {
            points.add(Pair(random.nextFloat() * width, random.nextFloat() * height))
        }
        points.add(Pair(0f, 0f)); points.add(Pair(width, 0f)); points.add(Pair(0f, height)); points.add(Pair(width, height))

        // Size controla el número de triángulos. Rango 1 a 3 veces numPoints
        val numTriangles = (numPoints * (1 + config.size / 5)).toInt()

        for (i in 0 until numTriangles) {
            val p1 = points.random(random)
            val p2 = points.random(random)
            val p3 = points.random(random)

            val path = Path().apply { moveTo(p1.first, p1.second); lineTo(p2.first, p2.second); lineTo(p3.first, p3.second); close() }

            val color = palette.random(random)
            // Transparency controla el alfa. Rango de 10 a 200
            val alpha = (10 + config.transparency * 19).toInt()
            paint.style = Paint.Style.FILL
            paint.color = color.toArgb()
            paint.alpha = alpha
            canvas.drawPath(path, paint)
        }
    }

    private fun drawCrystals(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Color>
    ) {
        val numShapes = (10 + config.density * 4).toInt()
        val baseSize = (20 + config.size * 15).toInt()

        for (i in 0 until numShapes) {
            val path = Path()
            val startX = random.nextFloat() * width
            val startY = -height * 0.1f
            path.moveTo(startX, startY)
            path.lineTo(startX + random.nextInt(-baseSize, baseSize), height * 0.3f * random.nextFloat())
            path.lineTo(startX + random.nextInt(-baseSize * 2, baseSize * 2), height * 0.4f * random.nextFloat())
            path.lineTo(startX + random.nextInt(-baseSize, baseSize), startY + 10)
            path.close()

            val color = palette.random(random)
            val alpha = (20 + config.transparency * 8).toInt()
            paint.style = Paint.Style.FILL
            paint.color = color.toArgb()
            paint.alpha = alpha
            canvas.drawPath(path, paint)
        }
    }

    private fun drawGeometric(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Color>
    ) {
        val hexSize = (10 + config.size * 8).toFloat()
        val hexWidth = hexSize * 2
        val hexHeight = kotlin.math.sqrt(3.0).toFloat() * hexSize

        for (y in -hexHeight.toInt()..(height + hexHeight).toInt() step (hexHeight.toInt() / (1 + (config.density / 2).toInt()))) {
            for (x in -hexWidth.toInt()..(width + hexWidth).toInt() step (hexWidth * 0.75f).toInt()) {
                val path = Path()
                val centerX = x.toFloat()
                val centerY = if ((x / (hexWidth * 0.75f)).toInt() % 2 == 0) y.toFloat() else y.toFloat() + hexHeight / 2
                path.moveTo(centerX + hexSize * cos(0f), centerY + hexSize * sin(0f))
                for (j in 1..6) {
                    path.lineTo(centerX + hexSize * cos(j * 2 * Math.PI / 6).toFloat(), centerY + hexSize * sin(j * 2 * Math.PI / 6).toFloat())
                }
                path.close()

                val color = palette.random(random)
                val alpha = (10 + config.transparency * 4).toInt()
                paint.style = Paint.Style.FILL
                paint.color = color.toArgb()
                paint.alpha = alpha
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun drawOverlappingPapers(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Color>
    ) {
        val numPapers = (5 + config.density * 3).toInt()
        canvas.drawColor(palette[0].toArgb())

        for (i in 0 until numPapers) {
            val paperWidth = width * (0.2f + config.size / 20f)
            val paperHeight = height * (0.2f + config.size / 20f)
            val x = random.nextFloat() * width - paperWidth / 2
            val y = random.nextFloat() * height - paperHeight / 2

            canvas.save()
            canvas.rotate(random.nextFloat() * 90 - 45f, x + paperWidth / 2, y + paperHeight / 2)

            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            val alpha = (150 + config.transparency * 10).toInt()
            paint.alpha = alpha
            paint.setShadowLayer(15f, 5f, 5f, Color.argb(50, 0, 0, 0))
            canvas.drawRect(x, y, x + paperWidth, y + paperHeight, paint)
            paint.clearShadowLayer()

            canvas.restore()
        }
    }

    private fun drawAbstractWaves(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Color>
    ) {
        val numWaves = (2 + config.density).toInt()
        for (i in 0 until numWaves) {
            val path = Path()
            path.moveTo(-width * 0.2f, height * (0.3f + random.nextFloat() * 0.4f))
            path.cubicTo(width * 0.25f, height * random.nextFloat(), width * 0.75f, height * random.nextFloat(), width * 1.2f, height * (0.3f + random.nextFloat() * 0.4f))

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = height * (0.05f + config.size / 100f)
            val startColor = palette[1].toArgb()
            val endColor = Color.TRANSPARENT
            paint.shader = LinearGradient(0f, 0f, 0f, height, startColor, endColor, Shader.TileMode.CLAMP)
            val alpha = (config.transparency * 25.5).toInt()
            paint.alpha = alpha
            canvas.drawPath(path, paint)
            paint.shader = null
        }
    }

    private fun drawGoldenBokeh(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Color>
    ) {
        canvas.drawColor(palette[2].toArgb())
        val numCircles = (20 + config.density * 15).toInt()

        for (i in 0 until numCircles) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val radius = (10 + config.size * 15).toInt()
            val alpha = (10 + config.transparency * 5).toInt()
            paint.color = palette[0].toArgb()
            paint.alpha = alpha
            paint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, random.nextInt(radius).toFloat(), paint)
        }
    }
}
