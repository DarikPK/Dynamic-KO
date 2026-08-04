package pe.pixelcollage.app.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Un componente de estrella decorativa (Sparkle) animado de forma independiente y premium.
 * El brillo (opacidad) y el tamaño (escala) varían de forma asíncrona usando valores
 * de duración y retrasos iniciales aleatorios, simulando un centelleo natural del cielo.
 */
@Composable
fun Sparkle(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFC0CB), // Rosa claro decorativo por defecto
    alpha: Float = 0.95f, // Para compatibilidad con llamadas existentes
    minAlpha: Float = 0.2f,
    maxAlpha: Float = alpha,
    minScale: Float = 0.4f,
    maxScale: Float = 1.15f
) {
    // Generar valores aleatorios estables para cada instancia de Sparkle
    val duration = remember { (1800..3200).random() }
    val delay = remember { (0..1200).random() }

    val infiniteTransition = rememberInfiniteTransition(label = "SparkleTransition")

    // Animación para el brillo (opacidad)
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SparkleAlpha"
    )

    // Animación para el tamaño (escala)
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SparkleScale"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            val cx = w / 2f
            val cy = h / 2f
            val rx = w / 2f
            val ry = h / 2f
            moveTo(cx, cy - ry)
            quadraticBezierTo(cx, cy, cx + rx, cy)
            quadraticBezierTo(cx, cy, cx, cy + ry)
            quadraticBezierTo(cx, cy, cx - rx, cy)
            quadraticBezierTo(cx, cy, cx, cy - ry)
            close()
        }
        withTransform({
            scale(scaleX = scale, scaleY = scale)
        }) {
            drawPath(path = path, color = color.copy(alpha = alphaAnim))
        }
    }
}

/**
 * Un modificador premium que añade un reflejo multicolor brillante sobre cualquier elemento visual.
 * El destello se desplaza lentamente de izquierda a derecha de forma periódica, respetando
 * la silueta y transparencia del contenido original.
 */
fun Modifier.multicolorShimmer(): Modifier = composed {
    // Transición infinita con un retardo para pausar el ciclo entre barridos
    val infiniteTransition = rememberInfiniteTransition(label = "ShimmerTransition")
    val xProgress by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(4500, StartOffsetType.Delay)
        ),
        label = "ShimmerProgress"
    )

    // Colores elegantes y mágicos del destello: Azul celeste, Lavanda, Blanco brillante, Oro
    val shimmerColors = listOf(
        Color.Transparent,
        Color(0xFF80D0FF).copy(alpha = 0.15f), // Azul celeste suave
        Color(0xFFE0B0FF).copy(alpha = 0.45f), // Lavanda premium
        Color.White.copy(alpha = 0.85f),       // Destello de luz central brillante
        Color(0xFFFFD700).copy(alpha = 0.45f), // Oro suave
        Color.Transparent
    )

    this.then(
        Modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                // 1. Dibujar el contenido original
                drawContent()

                val width = size.width
                val height = size.height

                val startX = width * xProgress
                val endX = startX + (width * 0.5f)

                val brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(startX, 0f),
                    end = Offset(endX, height)
                )

                // Dibujar el gradiente usando BlendMode.SrcAtop para respetar la transparencia del logo
                drawRect(
                    brush = brush,
                    blendMode = BlendMode.SrcAtop
                )
            }
    )
}

/**
 * Un componente animado de selección discontinua ("marching ants") de alto rendimiento.
 * Traza los bordes de la máscara pixel-a-pixel y anima el patrón de rayas alternadas blancas
 * y negras en tiempo real para simular movimiento continuo.
 */
@Composable
fun MarchingAntsOutline(
    mask: Bitmap,
    modifier: Modifier = Modifier
) {
    // Recalcular la trayectoria del contorno únicamente cuando la máscara cambia
    val path = remember(mask) {
        val p = Path()
        val width = mask.width
        val height = mask.height
        val step = 4
        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                val pixel = mask.getPixel(x, y)
                val alpha = Color.alpha(pixel)
                if (alpha > 50) {
                    var isEdge = false
                    if (x - step >= 0 && Color.alpha(mask.getPixel(x - step, y)) < 50) isEdge = true
                    else if (x + step < width && Color.alpha(mask.getPixel(x + step, y)) < 50) isEdge = true
                    else if (y - step >= 0 && Color.alpha(mask.getPixel(x, y - step)) < 50) isEdge = true
                    else if (y + step < height && Color.alpha(mask.getPixel(x, y + step)) < 50) isEdge = true

                    if (isEdge) {
                        p.addRect(androidx.compose.ui.geometry.Rect(x.toFloat(), y.toFloat(), (x + step).toFloat(), (y + step).toFloat()))
                    }
                }
            }
        }
        p
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MarchingAntsTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "MarchingAntsPhase"
    )

    Canvas(modifier = modifier) {
        // Dibujar borde negro de fondo para máximo contraste
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = 6f)
        )
        // Dibujar líneas discontinuas blancas encima (Líneas de hormigas)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), phase)
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 3f, pathEffect = dashEffect)
        )
    }
}
