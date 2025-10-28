package com.example.dynamiccollage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamiccollage.data.model.User
import com.example.dynamiccollage.data.repository.AuthRepository
import com.example.dynamiccollage.data.repository.UserRepository
import com.example.dynamiccollage.viewmodel.ManageAccountsViewModel
import com.example.dynamiccollage.viewmodel.ManageAccountsViewModelFactory
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAccountsScreen(
    navController: NavController,
    viewModel: ManageAccountsViewModel = viewModel(
        factory = ManageAccountsViewModelFactory(
            AuthRepository(),
            UserRepository()
        )
    )
) {
    val users by viewModel.usersState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Cuentas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                UserItem(
                    user = user,
                    onBlockToggle = { isEnabled -> viewModel.toggleUserBlock(user, isEnabled) },
                    onDelete = { viewModel.deleteUser(user) }
                )
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onBlockToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val daysSinceCreation = TimeUnit.MILLISECONDS.toDays(Timestamp.now().toDate().time - user.createdAt.toDate().time)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.nick, style = MaterialTheme.typography.titleMedium)
                Text(text = "Días desde creación: $daysSinceCreation", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = user.allow_auto_login,
                onCheckedChange = onBlockToggle,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar Usuario", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
