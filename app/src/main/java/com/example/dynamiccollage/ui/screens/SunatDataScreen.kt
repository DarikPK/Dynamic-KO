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
                    selected = documentType == "RUC20",
                    onClick = {
                        documentType = "RUC20"
                        documentNumber = ""
                        sunatDataViewModel.resetState()
                    }
                )
                Text(
                    text = "RUC (Empresa)",
                    modifier = Modifier.selectable(
                        selected = documentType == "RUC20",
                        onClick = {
                            documentType = "RUC20"
                            documentNumber = ""
                            sunatDataViewModel.resetState()
                        }
                    ).padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = documentType == "RUC10",
                    onClick = {
                        documentType = "RUC10"
                        documentNumber = ""
                        sunatDataViewModel.resetState()
                    }
                )
                Text(
                    text = "RUC (Persona)",
                    modifier = Modifier.selectable(
                        selected = documentType == "RUC10",
                        onClick = {
                            documentType = "RUC10"
                            documentNumber = ""
                            sunatDataViewModel.resetState()
                        }
                    ).padding(start = 4.dp)
                )
            }

            val visualTransformation = when (documentType) {
                "RUC10" -> RucVisualTransformation("10")
                "RUC20" -> RucVisualTransformation("20")
                else -> VisualTransformation.None
            }
            OutlinedTextField(
                value = documentNumber,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    val maxLength = if (documentType == "DNI") 8 else 9
                    if (filtered.length <= maxLength) {
                        documentNumber = filtered
                    }
                },
                label = { Text("Número de ${documentType.replace("RUC20", "RUC").replace("RUC10", "RUC")}") },
                modifier = Modifier.fillMaxWidth(0.8f),
                enabled = sunatDataState !is SunatDataState.Loading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = visualTransformation
            )

            Box(modifier = Modifier.fillMaxWidth(0.8f), contentAlignment = Alignment.Center) {
                if (sunatDataState is SunatDataState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            val prefix = when (documentType) {
                                "RUC10" -> "10"
                                "RUC20" -> "20"
                                else -> ""
                            }
                            val numberToValidate = prefix + documentNumber
                            val isValid = when (documentType) {
                                "DNI" -> numberToValidate.length == 8
                                "RUC10", "RUC20" -> numberToValidate.length == 11
                                else -> false
                            }

                            if (isValid) {
                                sunatDataViewModel.getSunatData(documentType, numberToValidate)
                            } else {
                                val docName = documentType.replace("RUC20", "RUC").replace("RUC10", "RUC")
                                val requiredLength = if (documentType == "DNI") 8 else 11
                                Toast.makeText(context, "El $docName debe tener $requiredLength dígitos", Toast.LENGTH_SHORT).show()
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
                var manualAddress by remember { mutableStateOf("") }
                var manualDistrict by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Datos encontrados:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val displayName = if (data is com.example.dynamiccollage.remote.DniData) {
                        "${data.nombres} ${data.apellidoPaterno} ${data.apellidoMaterno}"
                    } else {
                        data.nombre
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = useName, onCheckedChange = { useName = it })
                        Text("Nombre: $displayName")
                    }
                    if (data is com.example.dynamiccollage.remote.RucData && data.numeroDocumento.startsWith("20")) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = useAddress, onCheckedChange = { useAddress = it })
                            Text("Dirección: ${data.direccion} - ${data.distrito}")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = useAddress, onCheckedChange = { useAddress = it })
                            Text("Añadir dirección")
                        }
                        if (useAddress) {
                            OutlinedTextField(
                                value = manualAddress,
                                onValueChange = { manualAddress = it.uppercase() },
                                label = { Text("Dirección") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            val districts = listOf(
                                "Ancón", "Ate", "Barranco", "Breña", "Callao", "Carabayllo", "Cercado de Lima",
                                "Chaclacayo", "Chorrillos", "Cieneguilla", "Comas", "El agustino", "Independencia",
                                "Jesús maría", "La molina", "La victoria", "Lince", "Los olivos", "Lurigancho",
                                "Lurín", "Magdalena del mar", "Miraflores", "Pachacámac", "Pucusana", "Pueblo libre",
                                "Puente piedra", "Punta hermosa", "Punta negra", "Rímac", "San bartolo", "San borja",
                                "San isidro", "San Juan de Lurigancho", "San Juan de Miraflores", "San Luis",
                                "San Martin de Porres", "San Miguel", "Santa Anita", "Santa María del Mar",
                                "Santa Rosa", "Santiago de Surco", "Surquillo", "Villa el Salvador",
                                "Villa Maria del Triunfo"
                            )
                            var expanded by remember { mutableStateOf(false) }
                            val filteredDistricts = districts.filter { it.contains(manualDistrict, ignoreCase = true) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = manualDistrict,
                                    onValueChange = { manualDistrict = it.uppercase() },
                                    label = { Text("Distrito (Opcional)") },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    filteredDistricts.forEach { district ->
                                        DropdownMenuItem(
                                            text = { Text(district) },
                                            onClick = {
                                                manualDistrict = district
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val finalAddress = when {
                                useAddress && data is com.example.dynamiccollage.remote.RucData && data.numeroDocumento.startsWith("20") -> "${data.direccion.uppercase()} - ${data.distrito.uppercase()}"
                                useAddress && (data is com.example.dynamiccollage.remote.DniData || (data is com.example.dynamiccollage.remote.RucData && data.numeroDocumento.startsWith("10"))) -> {
                                    if (manualDistrict.isNotBlank()) {
                                        "${manualAddress.uppercase()} - ${manualDistrict.uppercase()}"
                                    } else {
                                        manualAddress.uppercase()
                                    }
                                }
                                else -> null
                            }
                            val selectedData = SelectedSunatData(
                                nombre = if (useName) displayName else null,
                                numeroDocumento = data.numeroDocumento,
                                direccion = finalAddress
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
