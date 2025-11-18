package pe.pixelcollage.app.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class DiagnosticInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val responseCode = response.code
        val contentType = response.header("Content-Type")

        // Usamos peekBody para no consumir el cuerpo de la respuesta
        val bodyString = response.peekBody(1024).string()

        val logTag = "NetworkDiag"

        Log.d(logTag, "=================== NETWORK DIAGNOSTIC START ===================")
        Log.d(logTag, "Request URL: ${request.url}")
        Log.d(logTag, "Response Code: $responseCode")
        Log.d(logTag, "Content-Type: $contentType")
        Log.d(logTag, "Response Body Preview: \n$bodyString")
        Log.d(logTag, "==================== NETWORK DIAGNOSTIC END ====================")

        return response
    }
}
