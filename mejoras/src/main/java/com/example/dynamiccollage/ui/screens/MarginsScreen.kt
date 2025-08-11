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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamiccollage.R
import com.example.dynamiccollage.viewmodel.CoverSetupViewModel
import com.example.dynamiccollage.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarginsScreen(
    navController: NavController,
    coverSetupViewModel: CoverSetupViewModel,
    projectViewModel: ProjectViewModel
) {
    val coverConfig by coverSetupViewModel.coverConfig.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Márgenes de Hoja") },
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
            MarginCustomizationSection(
                marginTop = coverConfig.marginTop,
                marginBottom = coverConfig.marginBottom,
                marginLeft = coverConfig.marginLeft,
                marginRight = coverConfig.marginRight,
                onMarginChange = { top, bottom, left, right ->
                    coverSetupViewModel.onMarginChange(top, bottom, left, right)
                }
            )
        }
    }
}

@Composable
private fun MarginCustomizationSection(
    marginTop: Float,
    marginBottom: Float,
    marginLeft: Float,
    marginRight: Float,
    onMarginChange: (top: String?, bottom: String?, left: String?, right: String?) -> Unit
) {
    var topInput by remember { mutableStateOf(marginTop.toString()) }
    var bottomInput by remember { mutableStateOf(marginBottom.toString()) }
    var leftInput by remember { mutableStateOf(marginLeft.toString()) }
    var rightInput by remember { mutableStateOf(marginRight.toString()) }

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
            stringResource(id = R.string.cover_setup_margins_customization_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_top),
                value = topInput,
                onValueChange = {
                    topInput = it
                    onMarginChange(it, null, null, null)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_bottom),
                value = bottomInput,
                onValueChange = {
                    bottomInput = it
                    onMarginChange(null, it, null, null)
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_left),
                value = leftInput,
                onValueChange = {
                    leftInput = it
                    onMarginChange(null, null, it, null)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MarginTextField(
                label = stringResource(id = R.string.cover_setup_margin_right),
                value = rightInput,
                onValueChange = {
                    rightInput = it
                    onMarginChange(null, null, null, it)
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
