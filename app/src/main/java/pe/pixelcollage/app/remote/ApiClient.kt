package pe.pixelcollage.app.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://api.apis.net.pe/v1/"

    val instance: SunatApiService by lazy {
        // --- INICIO DE INSTRUMENTACIÓN DE RED ---
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(DiagnosticInterceptor())
            .build()
        // --- FIN DE INSTRUMENTACIÓN DE RED ---

        // --- INICIO DE INSTRUMENTACIÓN DE GSON ---
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(R8TypeInspectorFactory())
            .create()
        // --- FIN DE INSTRUMENTACIÓN DE GSON ---

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Usar Gson instrumentado
            .build()
        retrofit.create(SunatApiService::class.java)
    }
}
