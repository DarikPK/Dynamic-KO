package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.activity.ComponentActivity // Necesario para el viewModelStoreOwner en previews si no se pasa el VM
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
    projectViewModel: ProjectViewModel // Se recibe como parámetro
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
                onClick = {
                    val pdfFile = com.example.dynamiccollage.utils.PdfGenerator.generate(
                        context = context,
                        coverConfig = projectViewModel.currentCoverConfig.value,
                        pageGroups = projectViewModel.currentPageGroups.value,
                        fileName = "collage_report"
                    )
                    if (pdfFile != null) {
                        val encodedPath = java.net.URLEncoder.encode(pdfFile.absolutePath, "UTF-8")
                        navController.navigate(Screen.PdfPreview.withArgs(encodedPath))
                    } else {
                        Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            MainButton(
                text = stringResource(R.string.main_btn_share_pdf),
                onClick = {
                    Toast.makeText(context, "Compartir PDF: Próximamente", Toast.LENGTH_SHORT).show()
                }
            )
            MainButton(
                text = stringResource(R.string.main_btn_templates),
                onClick = {
                    Toast.makeText(context, "Plantillas: Próximamente", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            MainButton(
                text = stringResource(R.string.main_btn_generate_pdf),
                onClick = {
                    val pdfFile = com.example.dynamiccollage.utils.PdfGenerator.generate(
                        context = context,
                        coverConfig = projectViewModel.currentCoverConfig.value,
                        pageGroups = projectViewModel.currentPageGroups.value,
                        fileName = "collage_report"
                    )
                    if (pdfFile != null) {
                        val encodedPath = java.net.URLEncoder.encode(pdfFile.absolutePath, "UTF-8")
                        navController.navigate(Screen.PdfPreview.withArgs(encodedPath))
                    } else {
                        Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_SHORT).show()
                    }
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
            projectViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity) // Esto podría fallar en preview si el contexto no es una Activity
            // O pasar un mock/stub ProjectViewModel
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
            projectViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)
        )
    }
}
