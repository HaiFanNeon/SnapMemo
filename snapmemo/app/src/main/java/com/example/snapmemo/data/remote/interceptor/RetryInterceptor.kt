package com.example.snapmemo.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class RetryInterceptor @Inject constructor() : Interceptor {

    private val maxRetries = 3

    override fun intercept(chain: Interceptor.Chain): Response {
        var lastException: IOException? = null
        repeat(maxRetries) { attempt ->
            try {
                val response = chain.proceed(chain.request())
                if (response.isSuccessful || response.code != 503) {
                    return response
                }
                response.close()
            } catch (e: IOException) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    Thread.sleep(1000L * (attempt + 1))
                }
            }
        }
        throw lastException ?: IOException("Request failed after $maxRetries retries")
    }
}
