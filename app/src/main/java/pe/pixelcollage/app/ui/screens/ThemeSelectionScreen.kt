package pe.pixelcollage.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.WindowSizeClass
import androidx.compose.material3.adaptive.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import pe.pixelcollage.app.ui.theme.*
import pe.pixelcollage.app.viewmodel.ProjectViewModel

// Data class to hold the necessary colors for a preview
data class ThemePreview(
    val name: String,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color
)

// List of all available themes with their preview colors
val themePreviews = listOf(
    ThemePreview("Claro", claro_primary, claro_onPrimary, claro_primaryContainer, claro_onPrimaryContainer, claro_secondary, claro_background, claro_onPrimaryContainer, claro_surface, claro_onPrimaryContainer),
    ThemePreview("Oscuro", oscuro_primary, oscuro_onPrimary, oscuro_primaryContainer, oscuro_onPrimaryContainer, oscuro_secondary, oscuro_background, Color.White, oscuro_surface, Color.White),
    ThemePreview("Descanso", descanso_primary, descanso_onPrimary, descanso_primaryContainer, descanso_onPrimaryContainer, descanso_secondary, descanso_background, descanso_onPrimaryContainer, descanso_surface, descanso_onPrimaryContainer),
    ThemePreview("Bosque", bosque_primary, bosque_onPrimary, bosque_primaryContainer, bosque_onPrimaryContainer, bosque_secondary, bosque_background, bosque_onPrimaryContainer, bosque_surface, bosque_onPrimaryContainer),
    ThemePreview("Océano", oceano_primary, oceano_onPrimary, oceano_primaryContainer, oceano_onPrimaryContainer, oceano_secondary, oceano_background, oceano_onPrimaryContainer, oceano_surface, oceano_onPrimaryContainer),
    ThemePreview("Neón", neon_primary, neon_onPrimary, neon_primaryContainer, neon_onPrimaryContainer, neon_secondary, neon_background, neon_onPrimaryContainer, neon_surface, neon_onPrimaryContainer),
    ThemePreview("Cereza", cereza_primary, cereza_onPrimary, cereza_primaryContainer, cereza_onPrimaryContainer, cereza_secondary, cereza_background, cereza_onPrimaryContainer, cereza_surface, cereza_onPrimaryContainer),
    ThemePreview("Lavanda", lavanda_primary, lavanda_onPrimary, lavanda_primaryContainer, lavanda_onPrimaryContainer, lavanda_secondary, lavanda_background, lavanda_onPrimaryContainer, lavanda_surface, lavanda_onPrimaryContainer),
    ThemePreview("Café", cafe_primary, cafe_onPrimary, cafe_primaryContainer, cafe_onPrimaryContainer, cafe_secondary, cafe_background, cafe_onPrimaryContainer, cafe_surface, cafe_onPrimaryContainer),
    ThemePreview("Noche Estrellada", noche_primary, noche_onPrimary, noche_primaryContainer, noche_onPrimaryContainer, noche_secondary, noche_background, noche_onPrimaryContainer, noche_surface, noche_onPrimaryContainer),
    ThemePreview("Corporativo", corporativo_primary, corporativo_onPrimary, corporativo_primaryContainer, corporativo_onPrimaryContainer, corporativo_secondary, corporativo_background, corporativo_onPrimaryContainer, corporativo_surface, corporativo_onPrimaryContainer),
    ThemePreview("Clásico", clasico_primary, clasico_onPrimary, clasico_primaryContainer, clasico_onPrimaryContainer, clasico_secondary, clasico_background, clasico_onPrimaryContainer, clasico_surface, clasico_onPrimaryContainer),
    ThemePreview("Verde Jade", jade_primary, jade_onPrimary, jade_primaryContainer, jade_onPrimaryContainer, jade_secondary, jade_background, jade_onPrimaryContainer, jade_surface, jade_onPrimaryContainer),
    ThemePreview("Rojo Rubí", rubi_primary, rubi_onPrimary, rubi_primaryContainer, rubi_onPrimaryContainer, rubi_secondary, rubi_background, rubi_onPrimaryContainer, rubi_surface, rubi_onPrimaryContainer),
    ThemePreview("Ámbar", ambar_primary, ambar_onPrimary, ambar_primaryContainer, ambar_onPrimaryContainer, ambar_secondary, ambar_background, ambar_onPrimaryContainer, ambar_surface, ambar_onPrimaryContainer)
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val currentThemeName by projectViewModel.themeName.collectAsState()
    var selectedThemeName by remember { mutableStateOf(currentThemeName) }
    var hasChanges by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedThemeName) {
        hasChanges = selectedThemeName != currentThemeName
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (hasChanges) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    BackHandler(enabled = hasChanges) {
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Salir sin guardar") },
            text = { Text("Has seleccionado el tema '$selectedThemeName' pero no has guardado los cambios. ¿Estás seguro de que quieres salir?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    navController.popBackStack()
                }) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No, quedarse")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Tema") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(CircleShape)
                            .background(if (hasChanges) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .border(
                                width = if (hasChanges) 1.5.dp else 0.dp,
                                color = if (hasChanges) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        IconButton(
                            onClick = {
                                projectViewModel.updateTheme(context, selectedThemeName)
                                hasChanges = false
                                Toast.makeText(context, "Tema guardado", Toast.LENGTH_SHORT).show()
                            },
                            enabled = hasChanges,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar",
                                tint = if (hasChanges) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val widthSizeClass = windowSizeClass.widthSizeClass
        val isCompact = widthSizeClass == WindowWidthSizeClass.Compact
        val contentPadding = if (isCompact) 8.dp else 16.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = contentPadding),
                verticalArrangement = Arrangement.spacedBy(contentPadding),
                contentPadding = PaddingValues(vertical = contentPadding)
            ) {
                items(themePreviews) { themePreview ->
                    ThemePreviewItem(
                        themePreview = themePreview,
                        isSelected = themePreview.name == selectedThemeName,
                        onThemeSelected = { selectedThemeName = it }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemePreviewItem(
    themePreview: ThemePreview,
    isSelected: Boolean,
    onThemeSelected: (String) -> Unit
) {
    Column(modifier = Modifier.clickable { onThemeSelected(themePreview.name) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Add a spacer to keep alignment consistent when icon is not present
                Spacer(modifier = Modifier.width(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = themePreview.name,
                fontSize = 20.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            MinimalistPreview(theme = themePreview)
        }
        HorizontalDivider()
    }
}

@Composable
fun MinimalistPreview(theme: ThemePreview) {
    // The container now uses the currently active theme's surface color
    Card(
        modifier = Modifier.width(120.dp).height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val colors = listOf(theme.primary, theme.secondary, theme.onBackground, theme.onSurface).take(4)
            val circleSize = 18.dp
            val overlap = 12.dp

            Box {
                colors.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .padding(start = (overlap * index))
                            .size(circleSize)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape) // Border now also uses the active theme's surface color
                    )
                }
            }
        }
    }
}
