package pe.pixelcollage.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.pixelcollage.app.R
import pe.pixelcollage.app.ui.util.scaledSp
import pe.pixelcollage.app.viewmodel.InnerPagesViewModel

@Composable
fun SettingsDialog(
    viewModel: InnerPagesViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Ajustes de Página", fontSize = scaledSp(20.sp)) },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.smart_layout_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = scaledSp(16.sp)
                        )
                        Text(
                            text = stringResource(id = R.string.smart_layout_description),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = scaledSp(14.sp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = viewModel.newGroupIsSmart,
                        onCheckedChange = { isChecked ->
                            viewModel.newGroupIsSmart = isChecked
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar", fontSize = scaledSp(14.sp))
            }
        }
    )
}
