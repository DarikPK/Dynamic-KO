package com.example.dynamiccollage.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.dynamiccollage.data.model.PageOrientation

object ImageUtils {

    fun getImageOrientation(context: Context, uriString: String): PageOrientation {
        try {
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)

                val width = options.outWidth
                val height = options.outHeight

                return if (height > width) {
                    PageOrientation.Vertical
                } else {
                    PageOrientation.Horizontal
                }
            }
        } catch (e: Exception) {
            // En caso de error al leer la imagen, se devuelve una orientación por defecto.
            e.printStackTrace()
        }
        // Devuelve Vertical como un valor seguro por defecto si el stream es nulo o hay una excepción.
        return PageOrientation.Vertical
    }
}
