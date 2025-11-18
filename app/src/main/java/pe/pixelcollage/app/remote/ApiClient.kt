package pe.pixelcollage.app.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://api.apis.net.pe/v1/"

    val instance: SunatApiService by lazy {
        // --- INICIO DE INSTRUMENTACIÓN TEMPORAL ---
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(DiagnosticInterceptor())
            .build()
        // --- FIN DE INSTRUMENTACIÓN TEMPORAL ---

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Adjuntar el cliente instrumentado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(SunatApiService::class.java)
    }
}
