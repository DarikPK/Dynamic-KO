package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetBackgroundScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    val projectConfig by projectViewModel.currentCoverConfig.collectAsState()
    val currentColor = projectConfig.pageBackgroundColor?.let { Color(it) } ?: Color.White

    // Listen for result from ColorPickerScreen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getLiveData<String>("selected_color_background")?.observeForever { colorHex ->
            if (colorHex != null) {
                val color = Color(android.graphics.Color.parseColor("#$colorHex"))
                projectViewModel.updatePageBackgroundColor(context, color)
                // Clean up the state handle so it doesn't trigger again
                savedStateHandle.remove<String>("selected_color_background")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fondo de Página") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Color Actual:", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(currentColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                )
            }

            Button(
                onClick = {
                    val colorHex = String.format("%06X", (0xFFFFFF and currentColor.toArgb()))
                    navController.navigate(
                        Screen.ColorPicker.withArgs("background", "placeholder", colorHex)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Elegir Color Sólido")
            }
        }
    }
}
