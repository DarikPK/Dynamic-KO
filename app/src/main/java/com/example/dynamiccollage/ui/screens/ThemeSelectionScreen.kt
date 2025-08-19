package com.example.dynamiccollage.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dynamiccollage.ui.theme.*
import com.example.dynamiccollage.viewmodel.ProjectViewModel

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
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val currentThemeName by projectViewModel.themeName.collectAsState()
    var selectedThemeName by remember { mutableStateOf(currentThemeName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Tema") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    projectViewModel.updateTheme(selectedThemeName)
                    projectViewModel.saveProject(context)
                    Toast.makeText(context, "Tema guardado", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Guardar")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
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
        Divider()
    }
}

@Composable
fun MinimalistPreview(theme: ThemePreview) {
    Row(
        modifier = Modifier.width(120.dp), // Give it a fixed width
        horizontalArrangement = Arrangement.spacedBy(2.dp) // Tighter spacing
    ) {
        val colors = listOf(theme.primary, theme.secondary, theme.surface, theme.background, theme.onBackground)
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp)) // Softer corners
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            )
        }
    }
}
