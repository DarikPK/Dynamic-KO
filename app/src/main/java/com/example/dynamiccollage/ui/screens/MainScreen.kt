package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import android.content.Intent // Importar Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.dynamiccollage.viewmodel.PdfGenerationState
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showGeneratePdfDialog by remember { mutableStateOf(false) }
    var pdfFileName by remember { mutableStateOf("MiCollage") }

    val pdfGenerationState by projectViewModel.pdfGenerationState.collectAsState()
    val shareablePdfUri by projectViewModel.shareablePdfUri.collectAsState()

    // Manejar el lanzamiento del Intent para compartir
    LaunchedEffect(shareablePdfUri) {
        shareablePdfUri?.let { uri ->
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
            projectViewModel.resetShareableUri() // Resetear para no volver a lanzar
        }
    }

    // Manejar los estados de la generación de PDF
    LaunchedEffect(pdfGenerationState) {
        when (val state = pdfGenerationState) {
            is PdfGenerationState.Loading -> {
                Toast.makeText(context, context.getString(R.string.generating_pdf_toast), Toast.LENGTH_SHORT).show()
            }
            is PdfGenerationState.Success -> {
                Toast.makeText(context, context.getString(R.string.pdf_success_toast, state.file.name), Toast.LENGTH_LONG).show()
                projectViewModel.resetPdfGenerationState() // Resetear para futuros usos
            }
            is PdfGenerationState.Error -> {
                Toast.makeText(context, context.getString(R.string.pdf_error_toast, state.message), Toast.LENGTH_LONG).show()
                projectViewModel.resetPdfGenerationState()
            }
            is PdfGenerationState.Idle -> { /* No hacer nada */ }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(id = R.string.delete_project_dialog_title)) },
            text = { Text(stringResource(id = R.string.delete_project_dialog_message)) },
            confirmButton = {
                Button(onClick = {
                    projectViewModel.resetProject()
                    showDeleteConfirmDialog = false
                    Toast.makeText(context, context.getString(R.string.project_deleted_toast), Toast.LENGTH_SHORT).show()
                }) { Text(stringResource(id = R.string.delete_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(id = R.string.cancel_button))
                }
            }
        )
    }

    if (showGeneratePdfDialog) {
        AlertDialog(
            onDismissRequest = { showGeneratePdfDialog = false },
            title = { Text(stringResource(R.string.generate_pdf_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = pdfFileName,
                    onValueChange = { pdfFileName = it },
                    label = { Text(stringResource(R.string.pdf_filename_label)) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    projectViewModel.generatePdf(context, pdfFileName)
                    showGeneratePdfDialog = false
                }) { Text(stringResource(R.string.generate_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showGeneratePdfDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
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
                    val lastGeneratedFile = (pdfGenerationState as? PdfGenerationState.Success)?.file
                    if (lastGeneratedFile != null) {
                        projectViewModel.createShareableUriForFile(context, lastGeneratedFile)
                    } else {
                        Toast.makeText(context, "Primero genera un PDF para poder compartirlo", Toast.LENGTH_SHORT).show()
                    }
                },
                // Deshabilitar si no se ha generado un PDF con éxito en esta sesión
                buttonColor = if (pdfGenerationState !is PdfGenerationState.Success) MaterialTheme.colorScheme.surfaceVariant else null,
                textColor = if (pdfGenerationState !is PdfGenerationState.Success) MaterialTheme.colorScheme.onSurfaceVariant else null
            )
            MainButton(
                text = stringResource(R.string.main_btn_templates),
                onClick = { /* TODO */ }
            )
            Spacer(modifier = Modifier.weight(1f))

            if (pdfGenerationState is PdfGenerationState.Loading) {
                CircularProgressIndicator()
            } else {
                MainButton(
                    text = stringResource(R.string.main_btn_generate_pdf),
                    onClick = { showGeneratePdfDialog = true }
                )
            }

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
        val fakeProjectViewModel = ProjectViewModel()
        MainScreen(
            navController = rememberNavController(),
            projectViewModel = fakeProjectViewModel
        )
    }
}
