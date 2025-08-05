package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.viewmodel.ProjectViewModel
import com.example.dynamiccollage.viewmodel.SunatDataState
import com.example.dynamiccollage.viewmodel.SunatDataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunatDataScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    sunatDataViewModel: SunatDataViewModel = viewModel()
) {
    var documentType by remember { mutableStateOf("DNI") }
    var documentNumber by remember { mutableStateOf("") }
    val sunatDataState by sunatDataViewModel.sunatDataState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(sunatDataState) {
        when (val state = sunatDataState) {
            is SunatDataState.Success -> {
                projectViewModel.updateSunatData(state.data)
                sunatDataViewModel.resetState()
                navController.navigate(Screen.CoverSetup.route) {
                    popUpTo(Screen.Main.route) // Go back to main, not just one screen
                }
            }
            is SunatDataState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                sunatDataViewModel.resetState()
            }
            else -> {
                // Idle or Loading, do nothing here
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Obtener Datos de SUNAT") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = documentType == "DNI",
                    onClick = {
                        documentType = "DNI"
                        documentNumber = ""
                    }
                )
                Text(
                    text = "DNI",
                    modifier = Modifier.selectable(
                        selected = documentType == "DNI",
                        onClick = {
                            documentType = "DNI"
                            documentNumber = ""
                        }
                    ).padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = documentType == "RUC",
                    onClick = {
                        documentType = "RUC"
                        documentNumber = "20"
                    }
                )
                Text(
                    text = "RUC",
                    modifier = Modifier.selectable(
                        selected = documentType == "RUC",
                        onClick = {
                            documentType = "RUC"
                            documentNumber = "20"
                        }
                    ).padding(start = 4.dp)
                )
            }

            OutlinedTextField(
                value = documentNumber,
                onValueChange = { newValue ->
                    documentNumber = newValue.filter { it.isDigit() }
                },
                label = { Text("Número de ${documentType}") },
                modifier = Modifier.fillMaxWidth(0.8f),
                enabled = sunatDataState !is SunatDataState.Loading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Box(modifier = Modifier.fillMaxWidth(0.8f), contentAlignment = Alignment.Center) {
                if (sunatDataState is SunatDataState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (documentNumber.isNotBlank()) {
                                sunatDataViewModel.getSunatData(documentType, documentNumber)
                            } else {
                                Toast.makeText(context, "Por favor, ingrese un número", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Buscar")
                    }
                }
            }
        }
    }
}
