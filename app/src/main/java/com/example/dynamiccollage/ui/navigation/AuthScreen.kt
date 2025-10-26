package com.example.dynamiccollage.ui.navigation

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object MainApp : AuthScreen("main_app")
    object Blocked : AuthScreen("blocked")
    object ControlPanel : AuthScreen("control_panel")
}
