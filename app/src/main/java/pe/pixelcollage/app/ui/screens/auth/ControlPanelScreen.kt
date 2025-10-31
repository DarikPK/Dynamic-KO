package pe.pixelcollage.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.pixelcollage.app.viewmodel.ControlPanelViewModel

@Composable
fun ControlPanelScreen(
    controlPanelViewModel: ControlPanelViewModel = viewModel(),
    parentId: String
) {
    val children by controlPanelViewModel.childrenState.collectAsState()
    val isPlayStoreMode by controlPanelViewModel.playStoreModeState.collectAsState()

    LaunchedEffect(parentId) {
        controlPanelViewModel.getChildren(parentId)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        // Opción para el Modo Play Store
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Modo Play Store", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = isPlayStoreMode,
                onCheckedChange = {
                    controlPanelViewModel.setPlayStoreMode(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Gestión de cuentas infantiles (funcionalidad existente)
        Text("Gestión de Cuentas Infantiles", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(children) { child ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(child.email.orEmpty()) // Muestra el email en lugar de UID por claridad
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Auto Login", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = child.allow_auto_login,
                            onCheckedChange = {
                                controlPanelViewModel.updateChild(child.copy(allow_auto_login = it))
                            }
                        )
                    }
                }
                Divider()
            }
        }
    }
}
