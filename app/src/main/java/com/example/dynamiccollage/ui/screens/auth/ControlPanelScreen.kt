package com.example.dynamiccollage.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dynamiccollage.data.model.User
import com.example.dynamiccollage.viewmodel.ControlPanelViewModel

@Composable
fun ControlPanelScreen(
    controlPanelViewModel: ControlPanelViewModel = viewModel(),
    parentId: String
) {
    val children by controlPanelViewModel.childrenState.collectAsState()

    LaunchedEffect(parentId) {
        controlPanelViewModel.getChildren(parentId)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(children) { child ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(child.uid)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = child.allow_auto_login,
                    onCheckedChange = {
                        controlPanelViewModel.updateChild(child.copy(allow_auto_login = it))
                    }
                )
            }
        }
    }
}
