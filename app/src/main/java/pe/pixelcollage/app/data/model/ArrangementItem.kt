package pe.pixelcollage.app.data.model

enum class SheetType {
    SINGLE, DOUBLE
}

data class PhotoArrangementItem(
    val uri: String,
    var order: Int,
    var sheetType: SheetType
)