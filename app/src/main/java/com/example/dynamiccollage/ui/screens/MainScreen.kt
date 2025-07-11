package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import androidx.activity.ComponentActivity // Para el ViewModelStoreOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(id = R.string.delete_project_dialog_title)) },
            text = { Text(stringResource(id = R.string.delete_project_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        projectViewModel.resetProject()
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, context.getString(R.string.project_deleted_toast), Toast.LENGTH_SHORT).show()
                    }
                ) { Text(stringResource(id = R.string.delete_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(id = R.string.cancel_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MainButton(
                text = stringResource(R.string.main_btn_cover_setup),
                onClick = { navController.navigate(Screen.CoverSetup.route) }
            )
            MainButton(
                text = stringResource(R.string.main_btn_inner_pages),
                onClick = { navController.navigate(Screen.InnerPages.route) }
            )
            MainButton(
                text = stringResource(R.string.main_btn_preview_pdf),
                onClick = { navController.navigate(Screen.PdfPreview.route) }
            )
            MainButton(
                text = stringResource(R.string.main_btn_share_pdf),
                onClick = {
                    // Lógica para compartir PDF (se implementará más adelante)
                    Toast.makeText(context, "Compartir PDF: Próximamente", Toast.LENGTH_SHORT).show()
                }
            )
            MainButton(
                text = stringResource(R.string.main_btn_templates),
                onClick = {
                    // Lógica para guardar/cargar plantillas (se implementará más adelante)
                    Toast.makeText(context, "Plantillas: Próximamente", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.weight(1f)) // Empuja el último botón hacia abajo
            MainButton(
                text = stringResource(R.string.main_btn_generate_pdf),
                onClick = {
                    // Lógica para generar PDF (se implementará más adelante)
                    Toast.makeText(context, "Generar PDF: Próximamente", Toast.LENGTH_SHORT).show()
                }
            )

            // Nuevo Botón para Eliminar Proyecto
            Spacer(modifier = Modifier.height(16.dp)) // Un poco de espacio antes del botón de eliminar
            MainButton(
                text = stringResource(R.string.main_btn_delete_project),
                onClick = { showDeleteConfirmDialog = true },
                buttonColor = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun MainButton(
    text: String,
    onClick: () -> Unit,
    buttonColor: androidx.compose.ui.graphics.Color? = null, // Parámetro opcional para color
    textColor: androidx.compose.ui.graphics.Color? = null
) {
    val colors = if (buttonColor != null) {
        ButtonDefaults.buttonColors(containerColor = buttonColor)
    } else {
        ButtonDefaults.buttonColors()
    }
    val textFinalColor = textColor ?: MaterialTheme.colorScheme.onPrimary

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(48.dp),
        colors = colors
    ) {
        Text(text.uppercase(), color = textFinalColor)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DynamicCollageTheme {
        // Para la preview, el ProjectViewModel no se inyectará automáticamente así.
        // Se necesitaría un mock o una forma de proveerlo para previews si se quiere probar la lógica de eliminación.
        // Por ahora, la preview mostrará la UI sin la interacción del ViewModel.
        MainScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenDarkPreview() {
    DynamicCollageTheme(darkTheme = true) {
        MainScreen(navController = rememberNavController())
    }
}
