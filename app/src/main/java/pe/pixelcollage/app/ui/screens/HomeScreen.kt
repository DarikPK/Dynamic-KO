package pe.pixelcollage.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

    Scaffold(
        bottomBar = {
            HomeNavigationBar(currentRoute = Screen.Home.route, navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera con Logotipo e Identidad
            HeaderSection(onLogoutClick = { mainViewModel.logout() }, showLogout = currentUser != null)

            // Tarjeta principal de Crear Collage (Morado/Violeta)
            CreateCollageCard(isCompact = isCompact, onNavigateToTemplates = {
                navController.navigate(Screen.Templates.route)
            }, onNavigateToClassic = {
                navController.navigate(Screen.Main.route)
            })

            // Tarjeta secundaria de Editar Foto (Azul)
            EditPhotoCard(isCompact = isCompact)

            // Sección de Recientes
            RecientesSection(
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
fun HeaderSection(onLogoutClick: () -> Unit, showLogout: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Pixel Collage",
                    style = MaterialTheme.typography.titleLarge,
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
        if (showLogout) {
            IconButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CreateCollageCard(
    isCompact: Boolean,
    onNavigateToTemplates: () -> Unit,
    onNavigateToClassic: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF673AB7), Color(0xFF512DA8))
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "Crear collage",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Diseña collages increíbles a tu manera",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Accesos
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccessItem(
                    icon = Icons.Default.AddCircleOutline,
                    title = "Desde cero",
                    subtitle = "Empieza con un collage vacío",
                    enabled = false,
                    onClick = {}
                )
                AccessItem(
                    icon = Icons.Default.Dashboard,
                    title = "Plantillas",
                    subtitle = "Elige un diseño para comenzar",
                    enabled = true,
                    onClick = onNavigateToTemplates
                )
                AccessItem(
                    icon = Icons.Default.FlashOn,
                    title = "Collage rápido",
                    subtitle = "Selecciona tus fotos y crea en segundos",
                    enabled = true,
                    onClick = onNavigateToClassic
                )
                AccessItem(
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
fun EditPhotoCard(isCompact: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0288D1), Color(0xFF01579B))
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "Editar foto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Mejora, transforma y personaliza tus fotos",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Accesos rápidos horizontales de edición (Próximamente)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tools = listOf("Eliminar objeto", "Quitar fondo", "Mejorar calidad", "Ver todas")
                tools.forEach { tool ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                            .clickable(enabled = false) {}
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tool,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccessItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (enabled) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = alpha),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = alpha)
                )
                if (!enabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PRÓXIMAMENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow.copy(alpha = 0.8f),
                        fontSize = 9.sp
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = alpha * 0.7f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = alpha * 0.5f)
        )
    }
}

@Composable
fun RecientesSection(
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
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mainImageUri != null) {
                        AsyncImage(
                            model = mainImageUri,
                            contentDescription = "Portada",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

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
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Aún no tienes proyectos recientes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onStartNewProject,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(text = "Crear mi primer collage")
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
