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

    // Paletas de colores basadas en los temas, usando enteros ARGB
    private val colorPalettes = mapOf(
        "SkyBlue" to listOf(0xFFDBE5F1.toInt(), 0xFF73A1D3.toInt(), 0xFF2C74B5.toInt()),
        "Ruby Red" to listOf(0xFFF5D0D7.toInt(), 0xFFD46A7E.toInt(), 0xFF9B1B30.toInt()),
        "Emerald Green" to listOf(0xFFD4EEE6.toInt(), 0xFF50C878.toInt(), 0xFF009B77.toInt()),
        "Golden Sun" to listOf(0xFFFDF0D5.toInt(), 0xFFFBC02D.toInt(), 0xFFE49B0F.toInt()),
        "Amethyst Purple" to listOf(0xFFE9D6F5.toInt(), 0xFF9966CC.toInt(), 0xFF6A0DAD.toInt()),
        "Obsidian Black" to listOf(0xFFE0E0E0.toInt(), 0xFF808080.toInt(), 0xFF000000.toInt()),
        "Graphite Gray" to listOf(0xFFF0F0F0.toInt(), 0xFFA0A0A0.toInt(), 0xFF4C4C4C.toInt()),
        "Mocha Brown" to listOf(0xFFEAE0D9.toInt(), 0xFFB59477.toInt(), 0xFF6F4E37.toInt()),
        "Ocean Teal" to listOf(0xFFD4EBEB.toInt(), 0xFF48D1CC.toInt(), 0xFF008080.toInt()),
        "Crimson Velvet" to listOf(0xFFF5D0D0.toInt(), 0xFFDC143C.toInt(), 0xFF8B0000.toInt())
    )

    fun drawGeneratedBackground(
        canvas: Canvas,
        config: GeneratedBackgroundConfig,
        width: Float,
        height: Float
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
            BackgroundPatternType.CRISTALES -> drawCrystals(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.GEOMETRICO -> drawGeometric(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.PAPELES_SUPERPUESTOS -> drawOverlappingPapers(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.ONDAS_ABSTRACTAS -> drawAbstractWaves(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.BOKEH_DORADO -> drawGoldenBokeh(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.RAYAS_DIAGONALES -> drawDiagonalStripes(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.TRAMA_DE_PUNTOS -> drawSubtleDotGrid(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.ACUARELA -> drawWatercolorWash(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.TEXTURA_PAPEL -> drawPaperTexture(canvas, width, height, paint, config, random, palette)
            BackgroundPatternType.METAL_CEPILLADO -> drawBrushedMetal(canvas, width, height, paint, config, random, palette)
            else -> { /* No-op */ }
        }
    }

    private fun drawLowPoly(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Int>
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

            val colorInt = palette.random(random)
            val alpha = (10 + config.transparency * 19).toInt()
            paint.style = Paint.Style.FILL
            paint.setARGB(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
            canvas.drawPath(path, paint)
        }
    }

    private fun drawCrystals(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Int>
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

            // Dibuja también desde abajo
            val path2 = Path()
            val startX2 = random.nextFloat() * width
            val startY2 = height * 1.1f
            path2.moveTo(startX2, startY2)
            path2.lineTo(startX2 + random.nextInt(-baseSize, baseSize), height - (height * 0.3f * random.nextFloat()))
            path2.lineTo(startX2 + random.nextInt(-baseSize * 2, baseSize * 2), height - (height * 0.4f * random.nextFloat()))
            path2.lineTo(startX2 + random.nextInt(-baseSize, baseSize), startY2 - 10)
            path2.close()

            val colorInt = palette.random(random)
            val alpha = (20 + config.transparency * 8).toInt()
            paint.style = Paint.Style.FILL
            paint.setARGB(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
            canvas.drawPath(path, paint)
            canvas.drawPath(path2, paint)
        }
    }

    private fun drawGeometric(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Int>
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

                val colorInt = palette.random(random)
                val alpha = (10 + config.transparency * 4).toInt()
                paint.style = Paint.Style.FILL
                paint.setARGB(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun drawOverlappingPapers(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Int>
    ) {
        val numPapers = (5 + config.density * 3).toInt()
        val colorInt = palette[0]
        canvas.drawColor(Color.rgb(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt)))


        for (i in 0 until numPapers) {
            val paperWidth = width * (0.2f + config.size / 20f)
            val paperHeight = height * (0.2f + config.size / 20f)
            val x = random.nextFloat() * width - paperWidth / 2
            val y = random.nextFloat() * height - paperHeight / 2

            canvas.save()
            canvas.rotate(random.nextFloat() * 90 - 45f, x + paperWidth / 2, y + paperHeight / 2)

            paint.style = Paint.Style.FILL
            val alpha = (150 + config.transparency * 10).toInt()
            paint.setARGB(alpha, 255, 255, 255)
            paint.setShadowLayer(15f, 5f, 5f, Color.argb(50, 0, 0, 0))
            canvas.drawRect(x, y, x + paperWidth, y + paperHeight, paint)
            paint.clearShadowLayer()

            canvas.restore()
        }
    }

    private fun drawAbstractWaves(
        canvas: Canvas, width: Float, height: Float, paint: Paint,
        config: GeneratedBackgroundConfig, random: Random, palette: List<Int>
    ) {
        val numWaves = (2 + config.density).toInt()
        for (i in 0 until numWaves) {
            val path = Path()
            path.moveTo(-width * 0.2f, height * (0.3f + random.nextFloat() * 0.4f))
            path.cubicTo(width * 0.25f, height * random.nextFloat(), width * 0.75f, height * random.nextFloat(), width * 1.2f, height * (0.3f + random.nextFloat() * 0.4f))

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = height * (0.05f + config.size / 100f)
            val startColor = palette[1]
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
        config: GeneratedBackgroundConfig, random: Random, palette: List<Int>
    ) {
        val colorInt = palette[2]
        canvas.drawColor(Color.rgb(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt)))
        val numCircles = (20 + config.density * 15).toInt()

        for (i in 0 until numCircles) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val radius = (10 + config.size * 15).toInt()
            val alpha = (10 + config.transparency * 5).toInt()
            val circleColor = palette[0]
            paint.setARGB(alpha, Color.red(circleColor), Color.green(circleColor), Color.blue(circleColor))
            paint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, random.nextInt(radius).toFloat(), paint)
        }
    }

    private fun drawDiagonalStripes(canvas: Canvas, width: Float, height: Float, paint: Paint, config: GeneratedBackgroundConfig, random: Random, palette: List<Int>) {
        val stripeWidth = 20 + config.size * 10
        val gapWidth = 20 + config.density * 10
        val totalWidth = stripeWidth + gapWidth
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stripeWidth
        val alpha = (10 + config.transparency * 10).toInt()
        val colorInt = palette[1]
        paint.setARGB(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))

        val path = Path()
        for (i in -((width + height) / totalWidth).toInt()..((width + height) / totalWidth).toInt()) {
            path.moveTo(i * totalWidth - height, 0f)
            path.lineTo(i * totalWidth + width, height + width)
        }
        canvas.drawPath(path, paint)
    }

    private fun drawSubtleDotGrid(canvas: Canvas, width: Float, height: Float, paint: Paint, config: GeneratedBackgroundConfig, random: Random, palette: List<Int>) {
        val dotRadius = 1 + config.size
        val gap = 10 + config.density * 5
        paint.style = Paint.Style.FILL
        val alpha = (20 + config.transparency * 20).toInt()
        val colorInt = palette[1]
        paint.setARGB(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))

        for (x in 0..(width / gap).toInt()) {
            for (y in 0..(height / gap).toInt()) {
                val jitterX = if (config.isRandom) random.nextFloat() * gap / 2 - gap / 4 else 0f
                val jitterY = if (config.isRandom) random.nextFloat() * gap / 2 - gap / 4 else 0f
                canvas.drawCircle(x * gap + jitterX, y * gap + jitterY, dotRadius, paint)
            }
        }
    }

    private fun drawWatercolorWash(canvas: Canvas, width: Float, height: Float, paint: Paint, config: GeneratedBackgroundConfig, random: Random, palette: List<Int>) {
        val numBlobs = (10 + config.density * 2).toInt()
        for (i in 0 until numBlobs) {
            val cx = random.nextFloat() * width
            val cy = random.nextFloat() * height
            val radius = (width / 4) + (config.size * 20) * random.nextFloat()
            val colorInt = palette.random(random)
            val alpha = (5 + config.transparency * 2).toInt()
            paint.style = Paint.Style.FILL
            paint.setARGB(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
            canvas.drawCircle(cx, cy, radius, paint)
        }
    }

    private fun drawPaperTexture(canvas: Canvas, width: Float, height: Float, paint: Paint, config: GeneratedBackgroundConfig, random: Random, palette: List<Int>) {
        val numGrains = (5000 + config.density * 2000).toInt()
        val grainSize = 1 + config.size / 5
        val alpha = (5 + config.transparency * 3).toInt()
        val colorInt = palette[1]
        paint.setARGB(alpha, Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))

        for (i in 0 until numGrains) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            canvas.drawCircle(x, y, grainSize * random.nextFloat(), paint)
        }
    }

    private fun drawBrushedMetal(canvas: Canvas, width: Float, height: Float, paint: Paint, config: GeneratedBackgroundConfig, random: Random, palette: List<Int>) {
        val numLines = (100 + config.density * 50).toInt()
        paint.strokeWidth = 1 + config.size / 10
        val baseColor = palette[0]
        val alpha = (5 + config.transparency * 2).toInt()
        paint.setARGB(alpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))

        for (i in 0 until numLines) {
            val y = random.nextFloat() * height
            val lineAlpha = alpha + random.nextInt(-5, 5)
            val colorVariation = random.nextInt(-10, 10)
            val r = (Color.red(baseColor) + colorVariation).coerceIn(0, 255)
            val g = (Color.green(baseColor) + colorVariation).coerceIn(0, 255)
            val b = (Color.blue(baseColor) + colorVariation).coerceIn(0, 255)
            paint.setARGB(lineAlpha.coerceIn(0,255), r, g, b)
            canvas.drawLine(0f, y, width, y, paint)
        }
    }
}
