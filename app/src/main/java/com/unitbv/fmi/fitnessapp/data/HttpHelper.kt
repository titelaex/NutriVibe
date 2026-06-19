package com.unitbv.fmi.fitnessapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object HttpHelper {

    suspend fun fetchUrl(urlString: String): String? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "NutriVibeApp/1.0 (Android; Contact: titelaex@gmail.com)")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return@withContext connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e("HttpHelper", "HTTP Error: Response code $responseCode for URL: $urlString")
            }
        } catch (e: Exception) {
            Log.e("HttpHelper", "Error requesting URL: $urlString", e)
        } finally {
            connection?.disconnect()
        }
        return@withContext null
    }
}
