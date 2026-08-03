package pe.pixelcollage.app.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import pe.pixelcollage.app.ui.navigation.Screen
import pe.pixelcollage.app.viewmodel.MainViewModel
import pe.pixelcollage.app.viewmodel.ProjectViewModel
import pe.pixelcollage.app.viewmodel.UserState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    projectViewModel: ProjectViewModel,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val userState by mainViewModel.userState.collectAsState()
    val currentUser = (userState as? UserState.Authenticated)?.user

    val coverConfig by projectViewModel.currentCoverConfig.collectAsState()
    val pageGroups by projectViewModel.currentPageGroups.collectAsState()
    val hasContent by projectViewModel.hasContent.collectAsState()

    // Cargar el proyecto cuando entra en composición
    LaunchedEffect(Unit) {
        projectViewModel.loadProject(context)
    }

    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    // Fondo neutro y elegante para resaltar las tarjetas
    val backgroundColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.background
    } else {
        Color(0xFFF7F7F9) // Un gris-marfil claro sumamente elegante y neutro
    }

    Scaffold(
        bottomBar = {
            HomeNavigationBar(currentRoute = Screen.Home.route, navController = navController)
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera con Logotipo, Ajustes y Nombre
            HeaderSection(
                onSettingsClick = { navController.navigate("account_management") }
            )

            // Tarjeta principal de Crear Collage (Morado/Violeta) - Rediseño Premium con previsualización refinada
            CreateCollageCardRefined(
                onNavigateToTemplates = { navController.navigate(Screen.Templates.route) },
                onNavigateToClassic = { navController.navigate(Screen.Main.route) }
            )

            // Tarjeta secundaria de Editar Foto (Azul) - Refinada
            EditPhotoCardRefined()

            // Sección de Recientes - Mejorada
            RecientesSectionRefined(
                hasContent = hasContent,
                clientName = coverConfig.clientNameStyle.content,
                mainImageUri = coverConfig.mainImageUri,
                pageGroupsCount = pageGroups.size,
                onOpenProject = {
                    navController.navigate(Screen.Main.route)
                },
                onStartNewProject = {
                    navController.navigate(Screen.Templates.route)
                }
            )

            // Versión de la app
            AppVersionSection(context = context)
        }
    }
}

@Composable
fun HeaderSection(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF7E57C2), Color(0xFF5E35B1))),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Pixel Collage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Todo para tus fotos, en un solo lugar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), shape = CircleShape)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes de cuenta",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun Sparkle(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFC0CB), // Rosa claro decorativo
    alpha: Float = 0.6f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            val cx = w / 2f
            val cy = h / 2f
            val rx = w / 2f
            val ry = h / 2f
            moveTo(cx, cy - ry)
            quadraticTo(cx, cy, cx + rx, cy)
            quadraticTo(cx, cy, cx, cy + ry)
            quadraticTo(cx, cy, cx - rx, cy)
            quadraticTo(cx, cy, cx, cy - ry)
            close()
        }
        drawPath(path = path, color = color.copy(alpha = alpha))
    }
}

@Composable
fun AmbientGlow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(110.dp) // Tamaño aproximado del 70% del corazón
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f), // Blanco brillante de baja opacidad
                        Color(0xFFE1BEE7).copy(alpha = 0.08f), // Ligero matiz violeta
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

@Composable
fun CollageHeroPreview(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .rotate(6f) // Inclinación suave refinada entre 5° y 8°
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(10.dp)) // Sombra extremadamente suave
            .clip(RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = painterResource(id = pe.pixelcollage.app.R.drawable.heart_collage_preview),
            contentDescription = "Previsualización de Collage",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun CreateCollageCardRefined(
    onNavigateToTemplates: () -> Unit,
    onNavigateToClassic: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp)), // Sombra exterior muy suave
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1B0B2E), // Morado profundo
                            Color(0xFF3B1E63)  // Violeta
                        )
                    )
                )
        ) {
            // Un toque de magenta en la esquina superior derecha para un degradado premium
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFD81B60).copy(alpha = 0.22f), // Ligero matiz magenta
                                Color.Transparent
                            ),
                            radius = 450f
                        )
                    )
            )

            // Fina línea de iluminación superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.15f))
                    .align(Alignment.TopCenter)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna Izquierda: Título, Subtítulo y Accesos (60% del ancho)
                Column(
                    modifier = Modifier.weight(0.58f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Título con destellos decorativos al lado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Crear collage",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        // Tres pequeños destellos junto al título
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Sparkle(modifier = Modifier.size(10.dp), alpha = 0.8f)
                            Sparkle(modifier = Modifier.size(6.dp), alpha = 0.6f)
                            Sparkle(modifier = Modifier.size(8.dp), alpha = 0.7f)
                        }
                    }

                    Text(
                        text = "Diseña collages increíbles a tu manera",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f), // Menor contraste para jerarquía clara
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Accesos de opciones
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AccessItemRefined(
                            icon = Icons.Default.AddCircleOutline,
                            title = "Desde cero",
                            subtitle = "Empieza vacío",
                            enabled = false,
                            onClick = {}
                        )
                        AccessItemRefined(
                            icon = Icons.Default.Dashboard,
                            title = "Plantillas",
                            subtitle = "Elige un diseño",
                            enabled = true,
                            onClick = onNavigateToTemplates
                        )
                        AccessItemRefined(
                            icon = Icons.Default.FlashOn,
                            title = "Collage rápido",
                            subtitle = "Crea en segundos",
                            enabled = true,
                            onClick = onNavigateToClassic
                        )
                        AccessItemRefined(
                            icon = Icons.Default.Code,
                            title = "Modelo personalizado",
                            subtitle = "Usa un código",
                            enabled = false,
                            onClick = {}
                        )
                    }
                }

                // Columna Derecha: Resplandor, Héroe Corazón y 4-5 destellos (42% del ancho)
                Box(
                    modifier = Modifier
                        .weight(0.42f)
                        .padding(start = 6.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    // 1. Resplandor radial detrás del corazón
                    AmbientGlow()

                    // 2. Imagen héroe corazón con inclinación suave (6 grados) y sombra extremadamente suave
                    CollageHeroPreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f)
                    )

                    // 3. Destellos alrededor del corazón (irregular, discreto, no simétrico)
                    Sparkle(modifier = Modifier.align(Alignment.TopStart).offset(x = (-4).dp, y = (-8).dp).size(12.dp), alpha = 0.65f)
                    Sparkle(modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-2).dp).size(8.dp), alpha = 0.5f)
                    Sparkle(modifier = Modifier.align(Alignment.BottomStart).offset(x = (-6).dp, y = 10.dp).size(9.dp), alpha = 0.55f)
                    Sparkle(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 8.dp, y = 4.dp).size(11.dp), alpha = 0.7f)
                    Sparkle(modifier = Modifier.align(Alignment.CenterEnd).offset(x = 12.dp, y = (-25).dp).size(7.dp), alpha = 0.45f)
                }
            }
        }
    }
}

@Composable
fun EditPhotoCardRefined() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF152A38), Color(0xFF0F1E29)) // Un azul-gris profundo y secundario sumamente premium
                    )
                )
        ) {
            // Fina línea de iluminación superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.10f))
                    .align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar foto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    // Insignia general para evitar redundancia en los botones
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.45f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRÓXIMAMENTE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700),
                            fontSize = 8.sp,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = "Mejora, transforma y personaliza tus fotos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Cuadrícula 2x2 para mayor compactación y alto contraste legible
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EditToolButton(text = "Eliminar objeto", modifier = Modifier.weight(1f))
                        EditToolButton(text = "Quitar fondo", modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EditToolButton(text = "Mejorar calidad", modifier = Modifier.weight(1f))
                        EditToolButton(text = "Ver todas", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun EditToolButton(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.01f))
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = false) {}
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.60f), // Sutil pero perfectamente legible
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 12.sp
        )
    }
}

@Composable
fun AccessItemRefined(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val textAlpha = if (enabled) 1.0f else 0.72f
    val subtextAlpha = if (enabled) 0.75f else 0.60f

    val buttonBackground = if (enabled) {
        Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.08f))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.02f))
        )
    }

    val borderBrush = if (enabled) {
        Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.01f))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(brush = buttonBackground)
            .border(width = 0.8.dp, brush = borderBrush, shape = RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = textAlpha),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = textAlpha),
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = subtextAlpha),
                    fontSize = 10.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = subtextAlpha),
                modifier = Modifier.size(16.dp)
            )
        }

        // Insignia PRÓXIMAMENTE de alta calidad en la esquina superior derecha
        if (!enabled) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp, end = 8.dp)
                    .background(Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    text = "PRÓXIMAMENTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700), // Amarillo dorado de alta visibilidad
                    fontSize = 6.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun RecientesSectionRefined(
    hasContent: Boolean,
    clientName: String,
    mainImageUri: String?,
    pageGroupsCount: Int,
    onOpenProject: () -> Unit,
    onStartNewProject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Recientes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (hasContent) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenProject),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mainImageUri != null) {
                        AsyncImage(
                            model = mainImageUri,
                            contentDescription = "Portada",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (clientName.isNotBlank()) clientName else "Proyecto sin título",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Collage Clásico • $pageGroupsCount grupos de páginas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onOpenProject) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Continuar editando",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            // Estado vacío alineado exactamente con los requerimientos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Aún no tienes proyectos recientes",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tus collages y ediciones aparecerán aquí",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = onStartNewProject,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(text = "Crear collage", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeNavigationBar(currentRoute: String, navController: NavController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { /* Ya estamos aquí */ },
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.ImageManager.route) },
            icon = { Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Fotos") },
            label = { Text("Fotos") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Templates.route) },
            icon = { Icon(imageVector = Icons.Default.GridView, contentDescription = "Modelos") },
            label = { Text("Modelos") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("account_management") },
            icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Perfil") },
            label = { Text("Perfil") }
        )
    }
}

@Composable
fun AppVersionSection(context: Context) {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    Text(
        text = "v$versionName",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}