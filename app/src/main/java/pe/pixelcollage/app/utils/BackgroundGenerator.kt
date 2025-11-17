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
        colorTheme: ColorTheme // Aunque no se use en los nuevos, lo mantenemos por firma
    ) {
        // Para el modo SÓLIDO, usamos el color definido en la configuración
        if (config.patternType == BackgroundPatternType.SÓLIDO) {
            canvas.drawColor(config.solidColor.toArgb())
            return
        }

        val paint = Paint().apply {
            isAntiAlias = true
            alpha = (config.opacity * 255).toInt()
        }

        when (config.patternType) {
            BackgroundPatternType.LOW_POLY -> drawLowPoly(canvas, width, height, paint)
            BackgroundPatternType.CRISTALES -> drawCrystals(canvas, width, height, paint)
            BackgroundPatternType.GEOMETRICO -> drawGeometric(canvas, width, height, paint)
            BackgroundPatternType.PAPELES_SUPERPUESTOS -> drawOverlappingPapers(canvas, width, height, paint)
            BackgroundPatternType.ONDAS_ABSTRACTAS -> drawAbstractWaves(canvas, width, height, paint)
            BackgroundPatternType.BOKEH_DORADO -> drawGoldenBokeh(canvas, width, height, paint)
            else -> { /* No-op */ }
        }
    }

    private fun drawLowPoly(canvas: Canvas, width: Float, height: Float, paint: Paint) {
        val random = Random(10) // Semilla fija para consistencia
        val points = mutableListOf<Pair<Float, Float>>()
        val numPoints = 50

        // Generar puntos aleatorios
        for (i in 0 until numPoints) {
            points.add(Pair(random.nextFloat() * width, random.nextFloat() * height))
        }
        // Añadir las esquinas para cubrir todo el lienzo
        points.add(Pair(0f, 0f))
        points.add(Pair(width, 0f))
        points.add(Pair(0f, height))
        points.add(Pair(width, height))

        // Crear triángulos aleatorios
        for (i in 0 until numPoints * 2) {
            val p1 = points.random(random)
            val p2 = points.random(random)
            val p3 = points.random(random)

            val path = Path().apply {
                moveTo(p1.first, p1.second)
                lineTo(p2.first, p2.second)
                lineTo(p3.first, p3.second)
                close()
            }

            // Paleta de colores rosados/púrpuras
            val red = 230 + random.nextInt(26) // 230-255
            val green = 210 + random.nextInt(26) // 210-235
            val blue = 220 + random.nextInt(26)  // 220-245
            val alpha = 50 + random.nextInt(100) // Opacidad variada

            paint.style = Paint.Style.FILL
            paint.setARGB(alpha, red, green, blue)
            canvas.drawPath(path, paint)
        }
    }

    private fun drawCrystals(canvas: Canvas, width: Float, height: Float, paint: Paint) {
         val random = Random(20)
        val numShapes = 30

        for (i in 0 until numShapes) {
            val path = Path()
            val startX = random.nextFloat() * width
            val startY = -height * 0.1f
            path.moveTo(startX, startY)

            // Crear formas tipo cristal desde los bordes superior e inferior
            path.lineTo(startX + random.nextInt(-80, 80), height * 0.3f * random.nextFloat())
            path.lineTo(startX + random.nextInt(-150, 150), height * 0.4f * random.nextFloat())
            path.lineTo(startX + random.nextInt(-80, 80), startY + 10)
            path.close()

            val path2 = Path()
            val startX2 = random.nextFloat() * width
            val startY2 = height * 1.1f
            path2.moveTo(startX2, startY2)
            path2.lineTo(startX2 + random.nextInt(-80, 80), height - (height * 0.3f * random.nextFloat()))
            path2.lineTo(startX2 + random.nextInt(-150, 150), height - (height * 0.4f * random.nextFloat()))
            path2.lineTo(startX2 + random.nextInt(-80, 80), startY2 - 10)
            path2.close()


            // Paleta de azules/púrpuras
            val r = 100 + random.nextInt(100)
            val g = 120 + random.nextInt(100)
            val b = 200 + random.nextInt(56)
            val alpha = 30 + random.nextInt(70)

            paint.style = Paint.Style.FILL
            paint.setARGB(alpha, r, g, b)
            canvas.drawPath(path, paint)
            canvas.drawPath(path2, paint)
        }
    }

    private fun drawGeometric(canvas: Canvas, width: Float, height: Float, paint: Paint) {
        val random = Random(30)
        val hexSize = 80f
        val hexWidth = hexSize * 2
        val hexHeight = kotlin.math.sqrt(3.0).toFloat() * hexSize

        for (y in -hexHeight.toInt()..(height + hexHeight).toInt() step (hexHeight / 2).toInt()) {
            for (x in -hexWidth.toInt()..(width + hexWidth).toInt() step (hexWidth * 0.75f).toInt()) {
                val path = Path()
                val centerX = x.toFloat()
                val centerY = if ((x / (hexWidth * 0.75f)).toInt() % 2 == 0) y.toFloat() else y.toFloat() + hexHeight / 2
                path.moveTo(centerX + hexSize * cos(0f), centerY + hexSize * sin(0f))
                for (j in 1..6) {
                    path.lineTo(centerX + hexSize * cos(j * 2 * Math.PI / 6).toFloat(), centerY + hexSize * sin(j * 2 * Math.PI / 6).toFloat())
                }
                path.close()

                // Paleta de azules claros
                paint.style = Paint.Style.FILL
                paint.setARGB(random.nextInt(10, 50), 0, 180, 255)
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun drawOverlappingPapers(canvas: Canvas, width: Float, height: Float, paint: Paint) {
        val random = Random(40)
        val numPapers = 25
        canvas.drawColor(Color.rgb(240, 240, 240)) // Un fondo ligeramente gris

        for (i in 0 until numPapers) {
            val paperWidth = width * (0.4f + random.nextFloat() * 0.5f)
            val paperHeight = height * (0.4f + random.nextFloat() * 0.5f)
            val x = random.nextFloat() * width - paperWidth / 2
            val y = random.nextFloat() * height - paperHeight / 2

            canvas.save()
            canvas.rotate(random.nextFloat() * 90 - 45f, x + paperWidth / 2, y + paperHeight / 2)

            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            paint.setShadowLayer(15f, 5f, 5f, Color.argb(50, 0, 0, 0))
            canvas.drawRect(x, y, x + paperWidth, y + paperHeight, paint)
            paint.clearShadowLayer()

            canvas.restore()
        }
    }

    private fun drawAbstractWaves(canvas: Canvas, width: Float, height: Float, paint: Paint) {
        val random = Random(50)
        val numWaves = 5

        for (i in 0 until numWaves) {
            val path = Path()
            path.moveTo(-width * 0.2f, height * (0.3f + random.nextFloat() * 0.4f))

            val cp1x = width * 0.25f
            val cp1y = height * random.nextFloat()
            val cp2x = width * 0.75f
            val cp2y = height * random.nextFloat()
            val endX = width * 1.2f
            val endY = height * (0.3f + random.nextFloat() * 0.4f)
            path.cubicTo(cp1x, cp1y, cp2x, cp2y, endX, endY)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = height * (0.1f + random.nextFloat() * 0.2f)
            paint.shader = LinearGradient(0f, 0f, 0f, height,
                Color.argb(80, 180, 220, 255),
                Color.argb(0, 200, 230, 255),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(path, paint)
            paint.shader = null
        }
    }

    private fun drawGoldenBokeh(canvas: Canvas, width: Float, height: Float, paint: Paint) {
        val random = Random(60)
        val numCircles = 100
        canvas.drawColor(Color.rgb(40, 30, 10))

        // Círculos grandes y difusos
        for (i in 0 until numCircles) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val radius = 20 + random.nextInt(150)
            val alpha = 10 + random.nextInt(40)
            val color = Color.argb(alpha, 255, 220, 150)
            paint.color = color
            paint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, radius.toFloat(), paint)
        }
        // Chispas brillantes
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        for (i in 0 until 15) {
             val x = random.nextFloat() * width
             val y = random.nextFloat() * height
             val size = 5 + random.nextInt(10)
             paint.color = Color.argb(200, 255, 255, 200)
             canvas.drawLine(x - size, y, x + size, y, paint)
             canvas.drawLine(x, y - size, x, y + size, paint)
        }
    }
}
