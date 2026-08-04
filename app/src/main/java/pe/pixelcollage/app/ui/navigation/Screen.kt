package pe.pixelcollage.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object Templates : Screen("templates_screen")
    object Subscription : Screen("subscription_screen")
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
    object ColorPicker : Screen("color_picker_screen")
    object GroupHeaderStyle : Screen("group_header_style_screen")
    object SizeManager : Screen("size_manager_screen")
    object ImageEffects : Screen("image_effects_screen")
    object AdvancedDesign : Screen("advanced_design_screen")
    object ImageBorders : Screen("image_borders_screen")
    object ThemeSelection : Screen("theme_selection_screen")
    object PhotoSwap : Screen("photo_swap_screen")
    object RecycleBin : Screen("recycle_bin_screen")
    object ColorThemeSelection : Screen("color_theme_selection_screen")
    object HybridQuality : Screen("hybrid_quality_screen")
    object GeneratedBackground : Screen("generated_background_screen")
    object ImageUpload : Screen("image_upload_screen") {
        fun createRoute(groupId: String) = "image_upload_screen/$groupId"
    }

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
