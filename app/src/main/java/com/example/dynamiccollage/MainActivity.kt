package com.example.dynamiccollage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dynamiccollage.ui.navigation.AppNavigation
import com.example.dynamiccollage.ui.theme.DynamicCollageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DynamicCollageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// El Greeting Composable y su Preview pueden eliminarse o mantenerse para pruebas rápidas
// si se desea, pero no son parte de la estructura de navegación principal.
// Por ahora los comentaré para mantener el enfoque en la navegación.

/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "¡Hola $name!",
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DynamicCollageTheme {
        Greeting("Dynamic Collage Preview")
    }
}
*/
