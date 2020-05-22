package com.example.basemodlue

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/5/22 09:43
 */
object HttpManager {
    private val retrofitBuilder = Retrofit.Builder()
    private val okHttpClient = OkHttpClient.Builder().build()

    suspend fun get(url:String):String?{
        return withContext(Dispatchers.IO){
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            return@withContext okHttpClient.newCall(request).execute().body()?.string()
        }
    }
}