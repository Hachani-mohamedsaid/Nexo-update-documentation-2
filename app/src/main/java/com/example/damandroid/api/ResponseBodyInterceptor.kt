package com.example.damandroid.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException

class ResponseBodyInterceptor : Interceptor {
    companion object {
        private val rawResponseBody = ThreadLocal<String>()
        
        fun getRawResponseBody(): String? {
            return rawResponseBody.get()
        }
        
        fun clearRawResponseBody() {
            rawResponseBody.remove()
        }
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body
            val buffer = source.buffer
            
            // Clone the buffer to read without consuming it
            val clonedBuffer = buffer.clone()
            val byteString = clonedBuffer.readByteString()
            val bodyString = byteString.utf8()
            
            // Store it in ThreadLocal for later retrieval
            rawResponseBody.set(bodyString)
            
            Log.d("ResponseBodyInterceptor", "Raw response body captured: ${bodyString.take(200)}...")
            
            // Return a new response with a new ResponseBody created from the byte string
            val contentType = responseBody.contentType()
            val newResponseBody = okhttp3.ResponseBody.create(contentType, byteString)
            
            return response.newBuilder()
                .body(newResponseBody)
                .build()
        }
        
        return response
    }
}

