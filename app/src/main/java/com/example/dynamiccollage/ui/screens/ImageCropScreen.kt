package com.example.dynamiccollage.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.canhub.cropper.CropImageView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropScreen(navController: NavController, imageUri: String?) {
    val cropImageView = remember {
        CropImageView(navController.context).apply {
            setImageUriAsync(Uri.parse(imageUri))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recortar Imagen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val cropped = cropImageView.getCroppedImage()
                        // TODO: Save the cropped image and update the URI
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Hecho")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = { cropImageView.rotateImage(90) }) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Girar")
                        Text("Girar")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            AndroidView({ cropImageView }, modifier = Modifier.fillMaxSize())
        }
    }
}
