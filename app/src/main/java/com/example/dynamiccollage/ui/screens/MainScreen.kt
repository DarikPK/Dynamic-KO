package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current

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
        }
    }
}

@Composable
fun MainButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f) // Botones un poco menos anchos que la pantalla
            .height(48.dp)
    ) {
        Text(text.uppercase()) // Texto en mayúsculas como convención para botones
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DynamicCollageTheme {
        MainScreen(rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenDarkPreview() {
    DynamicCollageTheme(darkTheme = true) {
        MainScreen(rememberNavController())
    }
}
