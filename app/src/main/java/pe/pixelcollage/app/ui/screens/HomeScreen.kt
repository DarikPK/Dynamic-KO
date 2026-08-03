package pe.pixelcollage.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

            // Tarjeta principal de Crear Collage (Morado/Violeta) - Compactada
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
fun CreateCollageCardRefined(
    onNavigateToTemplates: () -> Unit,
    onNavigateToClassic: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF7E57C2), Color(0xFF5E35B1))
                    )
                )
                .padding(14.dp) // Reducido el padding para compactar
        ) {
            Text(
                text = "Crear collage",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Diseña collages increíbles a tu manera",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Accesos compactados
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp) // Reducida la separación de 10.dp a 6.dp
            ) {
                AccessItemRefined(
                    icon = Icons.Default.AddCircleOutline,
                    title = "Desde cero",
                    subtitle = "Empieza con un collage vacío",
                    enabled = false,
                    onClick = {}
                )
                AccessItemRefined(
                    icon = Icons.Default.Dashboard,
                    title = "Plantillas",
                    subtitle = "Elige un diseño para comenzar",
                    enabled = true,
                    onClick = onNavigateToTemplates
                )
                AccessItemRefined(
                    icon = Icons.Default.FlashOn,
                    title = "Collage rápido",
                    subtitle = "Selecciona tus fotos y crea en segundos",
                    enabled = true,
                    onClick = onNavigateToClassic
                )
                AccessItemRefined(
                    icon = Icons.Default.Code,
                    title = "Modelo personalizado",
                    subtitle = "Abre un diseño mediante código",
                    enabled = false,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun EditPhotoCardRefined() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF29B6F6), Color(0xFF0288D1)) // Azul más vivo y elegante
                    )
                )
                .padding(14.dp)
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
                        .background(Color.Black.copy(alpha = 0.35f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRÓXIMAMENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        fontSize = 8.sp,
                        maxLines = 1
                    )
                }
            }
            Text(
                text = "Mejora, transforma y personaliza tus fotos",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
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

@Composable
fun EditToolButton(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(Color.White.copy(alpha = 0.22f), shape = RoundedCornerShape(10.dp)) // Mejorado contraste
            .clickable(enabled = false) {}
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f), // Perfectamente legible
            textAlign = TextAlign.Center,
            maxLines = 1
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
    // Legibilidad mejorada para deshabilitados (0.75f/0.65f en lugar de 0.5f)
    val textAlpha = if (enabled) 1f else 0.75f
    val subtextAlpha = if (enabled) 0.7f else 0.65f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (enabled) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f)
            )
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp), // Reducido el padding vertical de 12 a 8 para compactar
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp) // Reducido de 40.dp a 32.dp
                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = textAlpha),
                    modifier = Modifier.size(18.dp) // Reducido de 22.dp a 18.dp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = textAlpha),
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = subtextAlpha),
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = subtextAlpha * 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }

        // Insignia PRÓXIMAMENTE de alta calidad en la esquina superior derecha
        if (!enabled) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 12.dp)
                    .background(Color.Black.copy(alpha = 0.45f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    text = "PRÓXIMAMENTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700), // Amarillo dorado de alta visibilidad
                    fontSize = 7.sp,
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
