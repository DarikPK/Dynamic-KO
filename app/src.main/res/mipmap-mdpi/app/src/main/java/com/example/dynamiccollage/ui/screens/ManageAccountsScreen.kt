package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamiccollage.viewmodel.ManageAccountsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ManageAccountsScreen(
    navController: NavController,
    manageAccountsViewModel: ManageAccountsViewModel = viewModel()
) {
    val users by manageAccountsViewModel.users.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Administrar Cuentas") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Nick: ${user.nick}", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Creación: ${dateFormat.format(user.createdAt.toDate())}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { manageAccountsViewModel.toggleUserLock(user) }) {
                            Icon(
                                imageVector = if (user.locked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (user.locked) "Desbloquear" else "Bloquear"
                            )
                        }
                        IconButton(onClick = { manageAccountsViewModel.deleteUser(user) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar"
                            )
                        }
                    }
                }
            }
        }
    }
}
