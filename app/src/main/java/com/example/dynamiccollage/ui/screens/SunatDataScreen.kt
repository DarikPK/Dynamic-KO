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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.data.model.SelectedSunatData
import com.example.dynamiccollage.ui.navigation.Screen
import com.example.dynamiccollage.ui.util.RucVisualTransformation
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
                        sunatDataViewModel.resetState()
                    }
                )
                Text(
                    text = "DNI",
                    modifier = Modifier.selectable(
                        selected = documentType == "DNI",
                        onClick = {
                            documentType = "DNI"
                            documentNumber = ""
                            sunatDataViewModel.resetState()
                        }
                    ).padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = documentType == "RUC",
                    onClick = {
                        documentType = "RUC"
                        documentNumber = ""
                        sunatDataViewModel.resetState()
                    }
                )
                Text(
                    text = "RUC",
                    modifier = Modifier.selectable(
                        selected = documentType == "RUC",
                        onClick = {
                            documentType = "RUC"
                            documentNumber = ""
                            sunatDataViewModel.resetState()
                        }
                    ).padding(start = 4.dp)
                )
            }

            val rucVisualTransformation = remember { RucVisualTransformation() }
            OutlinedTextField(
                value = documentNumber,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    val maxLength = if (documentType == "DNI") 8 else 9
                    if (filtered.length <= maxLength) {
                        documentNumber = filtered
                    }
                },
                label = { Text("Número de ${documentType}") },
                modifier = Modifier.fillMaxWidth(0.8f),
                enabled = sunatDataState !is SunatDataState.Loading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = if (documentType == "RUC") rucVisualTransformation else VisualTransformation.None
            )

            Box(modifier = Modifier.fillMaxWidth(0.8f), contentAlignment = Alignment.Center) {
                if (sunatDataState is SunatDataState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            val numberToValidate = if (documentType == "RUC") "20$documentNumber" else documentNumber
                            val isValid = when (documentType) {
                                "DNI" -> numberToValidate.length == 8
                                "RUC" -> numberToValidate.length == 11
                                else -> false
                            }

                            if (isValid) {
                                sunatDataViewModel.getSunatData(documentType, numberToValidate)
                            } else {
                                val requiredLength = if (documentType == "DNI") 8 else 11
                                Toast.makeText(context, "El $documentType debe tener $requiredLength dígitos", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Buscar")
                    }
                }
            }

            if (sunatDataState is SunatDataState.Success) {
                val data = (sunatDataState as SunatDataState.Success).data
                var useName by remember { mutableStateOf(true) }
                var useAddress by remember { mutableStateOf(data is com.example.dynamiccollage.remote.RucData) }

                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Datos encontrados:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = useName, onCheckedChange = { useName = it })
                        Text("Nombre: ${data.nombre}")
                    }
                    if (data is com.example.dynamiccollage.remote.RucData) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = useAddress, onCheckedChange = { useAddress = it })
                            Text("Dirección: ${data.direccion} - ${data.distrito}")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val selectedData = SelectedSunatData(
                                nombre = if (useName) data.nombre else null,
                                numeroDocumento = data.numeroDocumento,
                                direccion = if (useAddress && data is com.example.dynamiccollage.remote.RucData) "${data.direccion} - ${data.distrito}" else null
                            )
                            projectViewModel.updateSunatData(selectedData)
                            sunatDataViewModel.resetState()
                            navController.navigate(Screen.CoverSetup.route) {
                                popUpTo(Screen.Main.route)
                            }
                        }
                    ) {
                        Text("Usar estos datos")
                    }
                }
            }
        }
    }
}
