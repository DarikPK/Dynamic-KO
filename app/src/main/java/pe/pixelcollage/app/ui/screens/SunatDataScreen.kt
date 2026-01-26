package pe.pixelcollage.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pe.pixelcollage.app.data.model.SelectedSunatData
import pe.pixelcollage.app.remote.DniData
import pe.pixelcollage.app.remote.RucData
import pe.pixelcollage.app.ui.navigation.Screen
import pe.pixelcollage.app.ui.util.RucVisualTransformation
import pe.pixelcollage.app.viewmodel.ProjectViewModel
import pe.pixelcollage.app.viewmodel.SunatDataState
import pe.pixelcollage.app.viewmodel.SunatDataViewModel

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunatDataScreen(
    windowSizeClass: WindowSizeClass,
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
                val errorMessage = if (state.message.contains("Error de red")) {
                    when (documentType) {
                        "DNI" -> "DNI no válido"
                        "RUC10", "RUC20" -> "RUC no válido"
                        else -> "Documento no válido"
                    }
                } else {
                    state.message
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
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
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        val widthSizeClass = windowSizeClass.widthSizeClass
        val isCompact = widthSizeClass == WindowWidthSizeClass.Compact
        val contentPadding = if (isCompact) 12.dp else 16.dp
        val verticalSpacing = if (isCompact) 10.dp else 12.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp) // Contenedor máximo
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // DNI Option
                    Box(modifier = Modifier.weight(0.5f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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
                        }
                    }

                    // RUC Options
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    }
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
                        val prefix = when (documentType) {
                            "RUC10" -> "10"
                            "RUC20" -> "20"
                            else -> null
                        }

                        // Lógica de pegado inteligente
                        if (prefix != null && filtered.length == 11 && filtered.startsWith(prefix)) {
                            documentNumber = filtered.substring(2)
                            return@OutlinedTextField
                        }

                        // Lógica original
                        val maxLength = if (documentType == "DNI") 8 else 9
                        if (filtered.length <= maxLength) {
                            documentNumber = filtered
                        }
                    },
                    label = { Text("Número de ${documentType.replace("RUC20", "RUC").replace("RUC10", "RUC")}") },
                    modifier = Modifier
                        .fillMaxWidth(if (isCompact) 0.9f else 0.8f)
                        .heightIn(min = 48.dp),
                    enabled = sunatDataState !is SunatDataState.Loading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = visualTransformation
                )

                Box(
                    modifier = Modifier.fillMaxWidth(if (isCompact) 0.9f else 0.8f),
                    contentAlignment = Alignment.Center
                ) {
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
                            modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp, max = 52.dp)
                        ) {
                            Text("Buscar")
                        }
                    }
                }

                if (sunatDataState is SunatDataState.Success) {
                    val data = (sunatDataState as SunatDataState.Success).data

                    // Determinar el nombre a mostrar y el número de documento
                    val (displayName, docNum) = when (data) {
                        is DniData -> "${data.nombres} ${data.apellidoPaterno} ${data.apellidoMaterno}" to data.numeroDocumento
                        is RucData -> data.nombre to data.numeroDocumento
                        else -> "" to ""
                    }

                    var useName by remember { mutableStateOf(true) }
                    var manualAddress by remember { mutableStateOf("") }
                    var manualDistrict by remember { mutableStateOf("") }

                    // Lógica específica para RUC de Empresa (RUC20)
                    if (data is RucData && data.numeroDocumento.startsWith("20")) {
                        var useApiAddress by remember { mutableStateOf(true) }
                        var useManualAddress by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.Start) {
                            Text("Datos encontrados:", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = useName, onCheckedChange = { useName = it })
                                Text("Nombre: $displayName")
                            }

                            // Checkbox para la dirección de la API
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = useApiAddress,
                                    onCheckedChange = {
                                        useApiAddress = it
                                        if (it) useManualAddress = false
                                    }
                                )
                                Text("Dirección: ${data.direccion} - ${data.distrito}")
                            }

                            // Checkbox para otra dirección (mutuamente excluyente)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = useManualAddress,
                                    onCheckedChange = {
                                        useManualAddress = it
                                        if (it) useApiAddress = false
                                    }
                                )
                                Text("Otra dirección")
                            }

                            if (useManualAddress) {
                                AddressInputFields(
                                    manualAddress = manualAddress,
                                    onAddressChange = { manualAddress = it },
                                    manualDistrict = manualDistrict,
                                    onDistrictChange = { manualDistrict = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                val finalAddress = when {
                                    useApiAddress -> "${data.direccion.uppercase()} - ${data.distrito.uppercase()}"
                                    useManualAddress -> if (manualDistrict.isNotBlank()) "${manualAddress.uppercase()} - ${manualDistrict.uppercase()}" else manualAddress.uppercase()
                                    else -> null
                                }
                                val selectedData = SelectedSunatData(
                                    nombre = if (useName) displayName else null,
                                    numeroDocumento = docNum,
                                    direccion = finalAddress
                                )
                                projectViewModel.updateSunatData(context, selectedData)
                                sunatDataViewModel.resetState()
                                navController.navigate(Screen.CoverSetup.route) { popUpTo(Screen.Main.route) }
                            }) {
                                Text("Usar estos datos")
                            }
                        }
                    } else {
                        // Lógica para DNI y RUC de Persona
                        var useAddress by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.Start) {
                            Text("Datos encontrados:", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = useName, onCheckedChange = { useName = it })
                                Text("Nombre: $displayName")
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = useAddress, onCheckedChange = { useAddress = it })
                                Text("Añadir dirección")
                            }

                            if (useAddress) {
                                AddressInputFields(
                                    manualAddress = manualAddress,
                                    onAddressChange = { manualAddress = it },
                                    manualDistrict = manualDistrict,
                                    onDistrictChange = { manualDistrict = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                val finalAddress = if (useAddress) {
                                    if (manualDistrict.isNotBlank()) "${manualAddress.uppercase()} - ${manualDistrict.uppercase()}" else manualAddress.uppercase()
                                } else null

                                val selectedData = SelectedSunatData(
                                    nombre = if (useName) displayName else null,
                                    numeroDocumento = docNum,
                                    direccion = finalAddress
                                )
                                projectViewModel.updateSunatData(context, selectedData)
                                sunatDataViewModel.resetState()
                                navController.navigate(Screen.CoverSetup.route) { popUpTo(Screen.Main.route) }
                            }) {
                                Text("Usar estos datos")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressInputFields(
    manualAddress: String,
    onAddressChange: (String) -> Unit,
    manualDistrict: String,
    onDistrictChange: (String) -> Unit
) {
    OutlinedTextField(
        value = manualAddress,
        onValueChange = { onAddressChange(it.replace("\n", "").uppercase()) },
        label = { Text("Dirección") },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        singleLine = true
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
            onValueChange = { onDistrictChange(it.replace("\n", "").uppercase()) },
            label = { Text("Distrito (Opcional)") },
            modifier = Modifier.menuAnchor().fillMaxWidth().heightIn(min = 48.dp),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filteredDistricts.forEach { district ->
                DropdownMenuItem(
                    text = { Text(district) },
                    onClick = {
                        onDistrictChange(district.uppercase())
                        expanded = false
                    }
                )
            }
        }
    }
}
