package pe.pixelcollage.app.data.model

import androidx.compose.ui.graphics.Color

data class ColorTheme(
    val name: String,
    val textColor: Color,
    val rucBackgroundColor: Color,
    val borderColor: Color
)

object ColorThemes {
    val themes = listOf(
        ColorTheme(
            name = "SkyBlue",
            textColor = Color(0xFF2C74B5),
            rucBackgroundColor = Color(0xFFDBE5F1),
            borderColor = Color(0xFF73A1D3)
        ),
        ColorTheme(
            name = "Ruby Red",
            textColor = Color(0xFF9B1B30),
            rucBackgroundColor = Color(0xFFF5D0D7),
            borderColor = Color(0xFFD46A7E)
        ),
        ColorTheme(
            name = "Emerald Green",
            textColor = Color(0xFF009B77),
            rucBackgroundColor = Color(0xFFD4EEE6),
            borderColor = Color(0xFF50C878)
        ),
        ColorTheme(
            name = "Golden Sun",
            textColor = Color(0xFFE49B0F),
            rucBackgroundColor = Color(0xFFFDF0D5),
            borderColor = Color(0xFFFBC02D)
        ),
        ColorTheme(
            name = "Amethyst Purple",
            textColor = Color(0xFF6A0DAD),
            rucBackgroundColor = Color(0xFFE9D6F5),
            borderColor = Color(0xFF9966CC)
        ),
        ColorTheme(
            name = "Obsidian Black",
            textColor = Color.Black,
            rucBackgroundColor = Color(0xFFE0E0E0),
            borderColor = Color.DarkGray
        ),
        ColorTheme(
            name = "Graphite Gray",
            textColor = Color(0xFF4C4C4C),
            rucBackgroundColor = Color(0xFFF0F0F0),
            borderColor = Color(0xFFA0A0A0)
        ),
        ColorTheme(
            name = "Mocha Brown",
            textColor = Color(0xFF6F4E37),
            rucBackgroundColor = Color(0xFFEAE0D9),
            borderColor = Color(0xFFB59477)
        ),
         ColorTheme(
            name = "Ocean Teal",
            textColor = Color(0xFF008080),
            rucBackgroundColor = Color(0xFFD4EBEB),
            borderColor = Color(0xFF48D1CC)
        ),
        ColorTheme(
            name = "Crimson Velvet",
            textColor = Color(0xFF8B0000),
            rucBackgroundColor = Color(0xFFF5D0D0),
            borderColor = Color(0xFFDC143C)
        )
    )
}
