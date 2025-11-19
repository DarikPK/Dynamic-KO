package pe.pixelcollage.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import pe.pixelcollage.app.ui.util.scaledSp

@Composable
fun ConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = "Confirmar",
    dismissButtonText: String = "Cancelar"
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title, fontSize = scaledSp(20.sp)) },
            text = { Text(text = message, fontSize = scaledSp(16.sp)) },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss() // Automatically dismiss after confirming
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (confirmButtonText == "Confirmar") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(confirmButtonText, fontSize = scaledSp(14.sp))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText, fontSize = scaledSp(14.sp))
                }
            }
        )
    }
}
