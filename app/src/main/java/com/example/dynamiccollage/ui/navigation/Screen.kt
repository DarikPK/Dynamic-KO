package com.example.dynamiccollage.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object CoverSetup : Screen("cover_setup_screen")
    object InnerPages : Screen("inner_pages_screen")
    object PdfPreview : Screen("pdf_preview_screen")
    object RowStyleEditor : Screen("row_style_editor_screen")
    object SunatData : Screen("sunat_data_screen")
    object ImageManager : Screen("image_manager_screen")
    object AdvancedCoverOptions : Screen("advanced_cover_options_screen")
    object TextStyle : Screen("text_style_screen")
    object Margins : Screen("margins_screen")
    object Weights : Screen("weights_screen")
    // object Settings : Screen("settings_screen") // Ejemplo si se necesitara

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
