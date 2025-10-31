package pe.pixelcollage.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pe.pixelcollage.app.viewmodel.AccountManagementViewModel

@Composable
fun AccountManagementScreen(
    navController: NavController,
    viewModel: AccountManagementViewModel = viewModel()
) {
    val isPlayStoreMode by viewModel.playStoreModeState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Selector de Modo de Autenticación
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Modo de Autenticación",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Modo:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            if (isPlayStoreMode) "Acceso libre con límite de 10 PDFs" else "Acceso con credenciales (empresarial)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPlayStoreMode,
                        onCheckedChange = { viewModel.setPlayStoreMode(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(32.dp))

        // Botones de Gestión de Cuentas (Modo Empresarial)
        AnimatedVisibility(visible = !isPlayStoreMode) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { navController.navigate("create_account") }) {
                    Text("Crear Cuenta de Usuario")
                }
                Button(onClick = { navController.navigate("manage_accounts") }) {
                    Text("Administrar Cuentas de Usuario")
                }
            }
        }
    }
}
