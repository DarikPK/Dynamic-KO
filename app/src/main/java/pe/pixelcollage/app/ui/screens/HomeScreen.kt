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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import pe.pixelcollage.app.ui.navigation.Screen
import pe.pixelcollage.app.viewmodel.MainViewModel
import pe.pixelcollage.app.viewmodel.ProjectViewModel
import pe.pixelcollage.app.viewmodel.UserState
import pe.pixelcollage.app.ui.components.Sparkle
import pe.pixelcollage.app.ui.components.multicolorShimmer
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

    var preselectedToolForPicker by remember { mutableStateOf("none") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val type = context.contentResolver.getType(uri)
            val isValid = type?.contains("image/jpeg") == true ||
                          type?.contains("image/png") == true ||
                          type?.contains("image/webp") == true ||
                          uri.path?.endsWith(".jpg", ignoreCase = true) == true ||
                          uri.path?.endsWith(".jpeg", ignoreCase = true) == true ||
                          uri.path?.endsWith(".png", ignoreCase = true) == true ||
                          uri.path?.endsWith(".webp", ignoreCase = true) == true

            if (isValid) {
                val encodedUri = java.net.URLEncoder.encode(uri.toString(), "UTF-8")
                navController.navigate(Screen.PhotoEditor.route + "/$encodedUri?tool=$preselectedToolForPicker")
            } else {
                Toast.makeText(context, "Este formato todavía no es compatible.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Efecto de efectos secundarios para pintar de negro la barra de estado y la barra de navegación del sistema de manera limpia y sin cortes
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        val window = (context as? android.app.Activity)?.window
        if (window != null) {
            val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
            SideEffect {
                window.statusBarColor = android.graphics.Color.BLACK
                window.navigationBarColor = android.graphics.Color.BLACK
                windowInsetsController.isAppearanceLightStatusBars = false // Iconos claros para fondo de barra de estado negro
                windowInsetsController.isAppearanceLightNavigationBars = false // Iconos claros para fondo de barra de navegación negro
            }
        }
    }

    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    // Fondo principal en negro puro #000000
    val backgroundColor = Color.Black

    Scaffold(
        bottomBar = {
            HomeNavigationBar(currentRoute = Screen.Home.route, navController = navController)
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()), // Solo padding inferior para la barra de navegación
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera con Logotipo, Botón ESTÁNDAR y Menú sobre fondo negro puro de extremo a extremo, extendiéndose tras la StatusBar
            HeaderSection(
                navController = navController
            )

            // Contenedor con scroll para las tarjetas y contenido de la pantalla principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tarjeta principal de Crear Collage (Morado/Violeta) - Rediseño Premium con previsualización refinada
                CreateCollageCardRefined(
                    onNavigateToTemplates = { navController.navigate(Screen.Templates.route) },
                    onNavigateToClassic = { navController.navigate(Screen.Main.route) }
                )

                // Tarjeta secundaria de Editar Foto (Azul) - Refinada
                EditPhotoCardRefined(
                    onStartEditing = { tool ->
                        preselectedToolForPicker = tool
                        photoPickerLauncher.launch("image/*")
                    }
                )

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
}

// Función helper ultra-limpia para remover el fondo negro de una imagen y dejarlo transparente de manera quirúrgica
private fun getTransparentLogo(context: Context, resId: Int): Bitmap {
    val src = BitmapFactory.decodeResource(context.resources, resId) ?:
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    val width = src.width
    val height = src.height
    val pixels = IntArray(width * height)
    src.getPixels(pixels, 0, width, 0, 0, width, height)

    var hasBlackBackground = false
    for (i in pixels.indices) {
        val color = pixels[i]
        val a = (color shr 24) and 0xFF
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        // Si el píxel es opaco pero es negro o casi negro, hacerlo transparente
        if (a > 200 && r < 20 && g < 20 && b < 20) {
            pixels[i] = 0x00000000
            hasBlackBackground = true
        }
    }

    if (!hasBlackBackground) {
        return src
    }

    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    result.setPixels(pixels, 0, width, 0, 0, width, height)
    return result
}

@Composable
fun HeaderSection(navController: NavController) {
    val context = LocalContext.current

    // Contenedor con fondo negro puro (#000000) de extremo a extremo que abarca la barra de estado (statusBarsPadding)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .statusBarsPadding() // Hace que el fondo negro se extienda por debajo de la StatusBar sin cortes ni líneas
            .padding(horizontal = 14.dp, vertical = 14.dp) // Espaciado refinado y respirable
    ) {
        // 1. Botón hamburguesa (Menú) alineado perfectamente a la izquierda
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Acción futura de menú de navegación */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú de navegación",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 2. Columna central con el Logotipo Oficial y Subtítulo centrado de forma absoluta
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.52f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cargar el logotipo con remoción quirúrgica de fondo negro en un remember para evitar sobrecarga de performance
            val logoBitmap = remember(context) {
                getTransparentLogo(context, pe.pixelcollage.app.R.drawable.ic_official_logo)
            }
            Image(
                bitmap = logoBitmap.asImageBitmap(),
                contentDescription = "Logo Oficial Pixel Collage",
                modifier = Modifier
                    .height(84.dp) // Aumentado en un 40%-60% su tamaño anterior (de 58dp a 84dp de altura) para ser el protagonista
                    .fillMaxWidth() // Adaptativo
                    .multicolorShimmer(),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(4.dp)) // Espaciado refinado entre logo y subtítulo
            Text(
                text = "Todo para tus fotos, en un solo lugar",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f), // Color blanco con opacidad reducida para jerarquía visual premium
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }

        // 3. Botón "ESTÁNDAR" alineado perfectamente a la derecha (engranaje eliminado completamente)
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón ESTÁNDAR con un diseño premium y pulido que destaca sobre fondo negro, ahora completamente funcional
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF7E57C2), Color(0xFF5E35B1))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        navController.navigate(Screen.Subscription.route)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ESTÁNDAR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
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
fun EditPhotoCardRefined(
    onStartEditing: (preselectedTool: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF142C44), // Azul profundo
                            Color(0xFF1B4965)  // Azul pizarra premium más iluminado
                        )
                    )
                )
        ) {
            // Fina línea de iluminación superior para un acabado extremadamente pro
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.22f))
                    .align(Alignment.TopCenter)
            )

            // Resplandor azul en la parte superior derecha
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.25f), // Azul celeste brillante
                                Color.Transparent
                            ),
                            radius = 450f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna Izquierda: Título, Descripción, Botón Premium CTA, Acciones con Iconos (55% del ancho)
                Column(
                    modifier = Modifier.weight(0.55f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Editar foto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        // Destellos discretos decorativos
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Sparkle(modifier = Modifier.size(8.dp), color = Color(0xFF38BDF8), alpha = 0.7f)
                            Sparkle(modifier = Modifier.size(5.dp), color = Color(0xFF38BDF8), alpha = 0.5f)
                        }
                    }

                    Text(
                        text = "Mejora, transforma y personaliza tus fotos",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    // Botón Principal Premium "Comenzar a editar" con excelente CTA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                            .clickable(enabled = true) { onStartEditing("none") }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Comenzar a editar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF142C44),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF142C44),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Botones de acciones inferiores con iconos y degradados
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EditActionItem(
                                icon = Icons.Default.Delete,
                                label = "Eliminar objeto",
                                modifier = Modifier.weight(1f),
                                onClick = { onStartEditing("ai_remove") }
                            )
                            EditActionItem(
                                icon = Icons.Default.Brush,
                                label = "Quitar fondo",
                                modifier = Modifier.weight(1f),
                                onClick = { onStartEditing("background_removal") }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EditActionItem(
                                icon = Icons.Default.Star,
                                label = "Mejorar calidad",
                                modifier = Modifier.weight(1f),
                                onClick = { onStartEditing("ai_enhance") }
                            )
                            EditActionItem(
                                icon = Icons.Default.GridView,
                                label = "Ver todas",
                                modifier = Modifier.weight(1f),
                                onClick = { onStartEditing("none") }
                            )
                        }
                    }
                }

                // Columna Derecha: Protagonista visual con imágenes grandes y superposición
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .height(160.dp)
                        .padding(start = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    EditPhotoOverlappingImages(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun EditActionItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.03f))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = true, onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 9.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EditPhotoOverlappingImages(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clipToBounds(),
        contentAlignment = Alignment.CenterStart
    ) {
        // Resplandor azul de fondo pro
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(110.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.35f), // Resplandor azul
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 1. Imagen 1 (Antes / Después) -> Grande, ocupa ~45% de la escala de tamaño
        Box(
            modifier = Modifier
                .offset(x = (-4).dp, y = 14.dp)
                .size(width = 68.dp, height = 96.dp)
                .rotate(-5f)
                .shadow(3.dp, shape = RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .border(0.8.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
        ) {
            Image(
                painter = painterResource(id = pe.pixelcollage.app.R.drawable.edit_before_after),
                contentDescription = "Antes / Después",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Imagen 2 (Retrato) -> Ocupa ~30% de la escala con patrón Checkerboard de transparencia pro
        Box(
            modifier = Modifier
                .offset(x = 48.dp, y = (-8).dp)
                .size(width = 62.dp, height = 88.dp)
                .rotate(3f)
                .shadow(5.dp, shape = RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .border(0.8.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(10.dp))
                .drawBehind {
                    // Recrear un patrón checkerboard impecable de transparencia (Photoshop/CapCut style)
                    val sizePx = 6.dp.toPx()
                    val width = size.width
                    val height = size.height
                    var y = 0f
                    var rowIdx = 0
                    while (y < height) {
                        var x = 0f
                        var colIdx = 0
                        while (x < width) {
                            val color = if ((rowIdx + colIdx) % 2 == 0) Color(0xFFFFFFFF) else Color(0xFFD4D4D8)
                            drawRect(
                                color = color,
                                topLeft = Offset(x, y),
                                size = Size(
                                    if (x + sizePx > width) width - x else sizePx,
                                    if (y + sizePx > height) height - y else sizePx
                                )
                            )
                            x += sizePx
                            colIdx++
                        }
                        y += sizePx
                        rowIdx++
                    }
                }
        ) {
            Image(
                painter = painterResource(id = pe.pixelcollage.app.R.drawable.edit_portrait),
                contentDescription = "Retrato",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Imagen 3 (Recorte con tijeras) -> Ocupa ~25% de la escala de tamaño y sobresale un 35% del contenedor
        Box(
            modifier = Modifier
                .offset(x = 94.dp, y = 16.dp)
                .size(width = 56.dp, height = 78.dp)
                .rotate(-3f)
                .shadow(3.dp, shape = RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .border(0.8.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
        ) {
            Image(
                painter = painterResource(id = pe.pixelcollage.app.R.drawable.edit_scissor),
                contentDescription = "Recorte",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
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
            color = Color.White
        )

        if (hasContent) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenProject),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1E)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
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
                                .background(Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
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
                            color = Color.White
                        )
                        Text(
                            text = "Collage Clásico • $pageGroupsCount grupos de páginas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(onClick = onOpenProject) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Continuar editando",
                            tint = Color(0xFFB39DDB),
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
                    containerColor = Color(0xFF1A1A1E)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
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
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Aún no tienes proyectos recientes",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tus collages y ediciones aparecerán aquí",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = onStartNewProject,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7E57C2),
                            contentColor = Color.White
                        )
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
        containerColor = Color.Black,
        tonalElevation = 0.dp
    ) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFFB39DDB), // Morado lavanda brillante
            selectedTextColor = Color(0xFFB39DDB),
            unselectedIconColor = Color.White.copy(alpha = 0.5f),
            unselectedTextColor = Color.White.copy(alpha = 0.5f),
            indicatorColor = Color(0xFF7E57C2).copy(alpha = 0.25f) // Sutil resplandor morado
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { /* Ya estamos aquí */ },
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.ImageManager.route) },
            icon = { Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Fotos") },
            label = { Text("Fotos") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Templates.route) },
            icon = { Icon(imageVector = Icons.Default.GridView, contentDescription = "Modelos") },
            label = { Text("Modelos") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("account_management") },
            icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            colors = navItemColors
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
        color = Color.White.copy(alpha = 0.4f),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}