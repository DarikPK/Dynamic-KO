package pe.pixelcollage.app.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import pe.pixelcollage.app.data.model.ImageEffectSettings
import pe.pixelcollage.app.ui.components.ResponsiveMainButton
import pe.pixelcollage.app.ui.components.ResponsiveTopAppBar
import pe.pixelcollage.app.ui.util.Responsive
import pe.pixelcollage.app.ui.util.scaledSp
import pe.pixelcollage.app.utils.ImageEffects
import pe.pixelcollage.app.viewmodel.ProjectViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEffectsScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    imageUri: String
) {
    val uri = remember { Uri.parse(imageUri) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val draftEffectSettings by projectViewModel.draftImageEffectSettings.collectAsState()
    val currentSettings = draftEffectSettings[imageUri] ?: ImageEffectSettings()

    var localSettings by remember { mutableStateOf(currentSettings) }

    LaunchedEffect(currentSettings) {
        localSettings = currentSettings
    }

    var previewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var originalBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    fun updatePreview(settings: ImageEffectSettings) {
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            originalBitmap?.let { ob ->
                val scaleFactor = 400.0 / ob.width.coerceAtLeast(ob.height)
                val thumbnail = if (scaleFactor < 1.0) {
                    android.graphics.Bitmap.createScaledBitmap(ob, (ob.width * scaleFactor).toInt(), (ob.height * scaleFactor).toInt(), true)
                } else ob
                val processedBitmap = ImageEffects.applyEffects(
                    thumbnail,
                    settings.brightness,
                    1.0f + settings.contrast / 100.0f,
                    1.0f + settings.saturation / 100.0f
                ).run {
                    when {
                        settings.sharpness > 0 -> ImageEffects.applySharpen(this, settings.sharpness / 100.0f)
                        settings.sharpness < 0 -> ImageEffects.applyBlur(this, -settings.sharpness / 100.0f)
                        else -> this
                    }
                }
                previewBitmap = processedBitmap
            }
        }
    }

    LaunchedEffect(uri, localSettings) {
        if (originalBitmap == null) {
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                originalBitmap = context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
                updatePreview(localSettings)
            }
        } else {
            updatePreview(localSettings)
        }
    }

    val dimensions = Responsive.dimensions

    Scaffold(
        topBar = {
            ResponsiveTopAppBar(
                title = { Text("Ajustar Efectos de Imagen", fontSize = scaledSp(22)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás (Cancelar)", modifier = Modifier.size(dimensions.topBarIconSize))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(dimensions.externalMargin)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = "Image Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Brillo: ${"%.0f".format(localSettings.brightness)}", fontSize = scaledSp(16))
                Slider(
                    value = localSettings.brightness,
                    onValueChange = { localSettings = localSettings.copy(brightness = it) },
                    valueRange = -100f..100f
                )
                Text("Contraste: ${"%.0f".format(localSettings.contrast)}", fontSize = scaledSp(16))
                Slider(
                    value = localSettings.contrast,
                    onValueChange = { localSettings = localSettings.copy(contrast = it) },
                    valueRange = -100f..100f
                )
                Text("Saturación: ${"%.0f".format(localSettings.saturation)}", fontSize = scaledSp(16))
                Slider(
                    value = localSettings.saturation,
                    onValueChange = { localSettings = localSettings.copy(saturation = it) },
                    valueRange = -100f..100f
                )
                Text("Nitidez: ${"%.0f".format(localSettings.sharpness)}", fontSize = scaledSp(16))
                Slider(
                    value = localSettings.sharpness,
                    onValueChange = { localSettings = localSettings.copy(sharpness = it) },
                    valueRange = -100f..100f
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ResponsiveMainButton(onClick = {
                        Log.d("ImageEffectsDebug", "Aplicando cambios para $imageUri: $localSettings")
                        projectViewModel.updateImageEffectSettings(imageUri, localSettings)
                        navController.popBackStack()
                    }, text = "Aplicar")
                    ResponsiveMainButton(onClick = {
                        localSettings = localSettings.copy(
                            brightness = 0f,
                            contrast = 0f,
                            saturation = 0f,
                            sharpness = 0f
                        )
                    }, text = "Restablecer")
                }
            }
        }
    }
}
