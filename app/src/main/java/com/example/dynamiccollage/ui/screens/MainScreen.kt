package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.activity.ComponentActivity // Necesario para el viewModelStoreOwner en previews si no se pasa el VM
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Para la preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    onThemeChange: (String) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showContentEntryDialog by remember { mutableStateOf(false) }
    val pdfGenerationState by projectViewModel.pdfGenerationState.collectAsState()
    val shareablePdfUri by projectViewModel.shareablePdfUri.collectAsState()

    // Efecto para navegar cuando el PDF está listo para previsualizar
    LaunchedEffect(pdfGenerationState) {
        if (pdfGenerationState is com.example.dynamiccollage.viewmodel.PdfGenerationState.Success) {
            val file = (pdfGenerationState as com.example.dynamiccollage.viewmodel.PdfGenerationState.Success).file
            val encodedPath = java.net.URLEncoder.encode(file.absolutePath, "UTF-8")
            navController.navigate(Screen.PdfPreview.withArgs(encodedPath))
            projectViewModel.resetPdfGenerationState() // Resetea el estado para no volver a navegar
        } else if (pdfGenerationState is com.example.dynamiccollage.viewmodel.PdfGenerationState.Error) {
            val message = (pdfGenerationState as com.example.dynamiccollage.viewmodel.PdfGenerationState.Error).message
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            projectViewModel.resetPdfGenerationState()
        }
    }

    // Efecto para lanzar el selector de compartir cuando el URI está listo
    LaunchedEffect(shareablePdfUri) {
        shareablePdfUri?.let { uri ->
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Share PDF"))
            projectViewModel.resetShareableUri() // Limpia el URI después de usarlo
        }
    }

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

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Seleccionar Tema") },
            text = {
                Column {
                    val themes = listOf("Claro", "Oscuro", "Descanso")
                    themes.forEach { themeName ->
                        TextButton(onClick = {
                            onThemeChange(themeName)
                            showThemeDialog = false
                        }) {
                            Text(themeName, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showContentEntryDialog) {
        AlertDialog(
            onDismissRequest = { showContentEntryDialog = false },
            title = { Text("Gestionar Contenido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showContentEntryDialog = false
                            navController.navigate(Screen.InnerPages.route)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ingreso Manual")
                    }
                    TextButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ingreso Inteligente (Próximamente)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContentEntryDialog = false }) {
                    Text("Cancelar")
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
                text = "Obtener Datos Sunat",
                onClick = { navController.navigate(Screen.SunatData.route) }
            )
            MainButton(
                text = stringResource(R.string.main_btn_cover_setup),
                onClick = { navController.navigate(Screen.CoverSetup.route) }
            )
            MainButton(
                text = "Gestionar Contenido",
                onClick = { showContentEntryDialog = true }
            )
            MainButton(
                text = stringResource(R.string.main_btn_preview_pdf),
                onClick = {
                    projectViewModel.generatePdf(context, "collage_report")
                }
            )
            MainButton(
                text = stringResource(R.string.main_btn_templates),
                onClick = {
                    Toast.makeText(context, "Plantillas: Próximamente", Toast.LENGTH_SHORT).show()
                }
            )
            MainButton(
                text = "Tema",
                onClick = { showThemeDialog = true }
            )
            Spacer(modifier = Modifier.weight(1f))
            MainButton(
                text = "Gestionar Imágenes",
                onClick = {
                    navController.navigate(Screen.ImageManager.route)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
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
    buttonColor: Color? = null,
    textColor: Color? = null
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
        // Para que la preview funcione sin crash, el ProjectViewModel necesita ser proveído.
        // Una forma es usar un viewModel() dummy o un mock.
        // Esto es solo un ejemplo de cómo podrías necesitar ajustar previews.
        val context = LocalContext.current
        MainScreen(
            navController = rememberNavController(),
            projectViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity), // Esto podría fallar en preview si el contexto no es una Activity
            // O pasar un mock/stub ProjectViewModel
            onThemeChange = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenDarkPreview() {
    DynamicCollageTheme(darkTheme = true) {
        val context = LocalContext.current
        MainScreen(
            navController = rememberNavController(),
            projectViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity),
            onThemeChange = {}
        )
    }
}
