package com.example.dynamiccollage.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object CoverSetup : Screen("cover_setup_screen")
    object InnerPages : Screen("inner_pages_screen")
    object ImageUpload : Screen("image_upload_screen/{groupId}") {
        fun createRoute(groupId: String) = "image_upload_screen/$groupId"
    }
    object PdfPreview : Screen("pdf_preview_screen")

}
