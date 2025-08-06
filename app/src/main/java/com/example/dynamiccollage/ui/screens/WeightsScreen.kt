package com.example.dynamiccollage.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightsScreen(
    navController: NavController,
    coverSetupViewModel: CoverSetupViewModel,
    projectViewModel: ProjectViewModel
) {
    val coverConfig by coverSetupViewModel.coverConfig.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Peso de Elementos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        projectViewModel.updateCoverConfig(coverConfig)
                        Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Guardar Cambios")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LayoutWeightsCustomizationSection(
                clientWeight = coverConfig.clientWeight,
                rucWeight = coverConfig.rucWeight,
                addressWeight = coverConfig.addressWeight,
                separationWeight = coverConfig.separationWeight,
                photoWeight = coverConfig.photoWeight,
                onWeightChange = { client, ruc, address, separation, photo ->
                    coverSetupViewModel.onWeightChange(client, ruc, address, separation, photo)
                }
            )
        }
    }
}

@Composable
private fun LayoutWeightsCustomizationSection(
    clientWeight: Float,
    rucWeight: Float,
    addressWeight: Float,
    separationWeight: Float,
    photoWeight: Float,
    onWeightChange: (client: String?, ruc: String?, address: String?, separation: String?, photo: String?) -> Unit
) {
    var clientInput by remember { mutableStateOf(clientWeight.toString()) }
    var rucInput by remember { mutableStateOf(rucWeight.toString()) }
    var addressInput by remember { mutableStateOf(addressWeight.toString()) }
    var separationInput by remember { mutableStateOf(separationWeight.toString()) }
    var photoInput by remember { mutableStateOf(photoWeight.toString()) }

    // NOTA: Se eliminan los LaunchedEffect para evitar que el campo se reinicie al borrar.

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Pesos de Diseño de Portada",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = "Peso Cliente",
                value = clientInput,
                onValueChange = {
                    clientInput = it
                    onWeightChange(it, null, null, null, null)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = "Peso RUC",
                value = rucInput,
                onValueChange = {
                    rucInput = it
                    onWeightChange(null, it, null, null, null)
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = "Peso Dirección",
                value = addressInput,
                onValueChange = {
                    addressInput = it
                    onWeightChange(null, null, it, null, null)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = "Peso Separación",
                value = separationInput,
                onValueChange = {
                    separationInput = it
                    onWeightChange(null, null, null, it, null)
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = "Peso Foto",
                value = photoInput,
                onValueChange = {
                    photoInput = it
                    onWeightChange(null, null, null, null, it)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MarginTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}
