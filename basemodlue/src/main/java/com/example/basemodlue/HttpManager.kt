package com.example.basemodlue

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/5/22 09:43
 */
object HttpManager {
    private val retrofitBuilder = Retrofit.Builder()
    private val okHttpClient = OkHttpClient.Builder().build()

    /**
     * 简单get请求
     * @param url 要get的url
     */
    suspend fun get(url: String): String? {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            return@withContext okHttpClient.newCall(request).execute().body()?.string()
        }
    }

    /**
     * 简单的post请求
     * @param url 请求地址
     * @param param 请求参数
     */
    suspend fun post(url: String, param: HashMap<String, String>): String? {
        return withContext(Dispatchers.IO) {
            val bodyBuilder = FormBody.Builder()
            for (key in param.keys) {
                bodyBuilder.add(key, param[key]!!)
            }
            val request = Request.Builder()
                .url(url)
                .post(bodyBuilder.build())
                .header("Accept","application/json")
                .build()
            return@withContext okHttpClient.newCall(request).execute().body()?.string()
        }

    }
}