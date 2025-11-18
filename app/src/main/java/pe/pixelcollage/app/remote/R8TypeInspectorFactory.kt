package pe.pixelcollage.app.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Modifier

class R8TypeInspectorFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType

        // Lista de clases que nos interesa inspeccionar
        val typesToInspect = listOf(
            RucData::class.java,
            DniData::class.java,
            SunatData::class.java
        )

        if (typesToInspect.any { it == rawType }) {
            logClassAnatomy(rawType)
        }

        // IMPORTANTE: Devolvemos null para que Gson use su lógica por defecto.
        // Esto asegura que nuestro inspector es NO-INTRUSIVO.
        return null
    }

    private fun logClassAnatomy(clazz: Class<*>) {
        val logTag = "R8Inspector"

        Log.d(logTag, "=================== R8 TYPE INSPECTION START ===================")
        Log.d(logTag, "Inspecting Class: ${clazz.name}")

        // 1. Interfaces implementadas (Evidencia Clave)
        val interfaces = clazz.interfaces.joinToString { it.name }
        Log.d(logTag, "  -> Implemented Interfaces: [${if (interfaces.isEmpty()) "NONE" else interfaces}]")

        // 2. Superclase
        Log.d(logTag, "  -> Superclass: ${clazz.superclass?.name ?: "NONE"}")

        // 3. Constructores y sus parámetros
        try {
            clazz.constructors.forEachIndexed { index, constructor ->
                val params = constructor.parameters.joinToString { "${it.name ?: "[param name unavailable]"}: ${it.type.simpleName}" }
                Log.d(logTag, "  -> Constructor[$index]: (${params})")
            }
        } catch (e: Exception) {
            Log.e(logTag, "  -> Error inspecting constructors: ${e.message}")
        }


        // 4. Campos declarados
        Log.d(logTag, "  -> Declared Fields:")
        try {
            clazz.declaredFields.forEach { field ->
                // Omitir campos sintéticos generados por el compilador (como $jacocoData)
                if (!field.isSynthetic) {
                     val modifiers = Modifier.toString(field.modifiers)
                     Log.d(logTag, "    - ${modifiers} ${field.name} (Type: ${field.type.simpleName})")
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "  -> Error inspecting fields: ${e.message}")
        }

        Log.d(logTag, "==================== R8 TYPE INSPECTION END ====================")
    }
}
