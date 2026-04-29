package com.zovdeneg.app.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.min

/**
 * Логирует начало тела ответа через [okio.BufferedSource.peek], не потребляя основной поток
 * (иначе Ktor/OkHttp даёт «unexpected end of stream» при последующем парсинге).
 */
internal object ZovOkHttpResponseBodyLoggingInterceptor : Interceptor {
    private const val TAG = "ZovHttp"
    private const val MAX_BYTES = 512L * 1024L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val body = response.body ?: return response
        val text =
            try {
                val source = body.source()
                val peek = source.peek()
                peek.request(MAX_BYTES)
                val toRead = min(MAX_BYTES, peek.buffer.size)
                if (toRead == 0L) {
                    ""
                } else {
                    peek.buffer.clone().readUtf8(toRead)
                }
            } catch (e: IOException) {
                "<peek read failed: ${e.message}>"
            } catch (e: IllegalStateException) {
                "<peek read failed: ${e.message}>"
            }
        Log.d(TAG, "<-- ${response.code} ${request.method} ${request.url} body:\n$text")
        return response
    }
}
