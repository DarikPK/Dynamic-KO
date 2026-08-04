package pe.pixelcollage.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform

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
