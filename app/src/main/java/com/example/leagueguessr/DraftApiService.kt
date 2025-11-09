package com.example.leagueguessr

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object DraftApiService {

    private const val SERVER_URL = "http://192.168.254.19:5000/draft"

    suspend fun fetchDraftFromServer(): DraftData? {
        return withContext(Dispatchers.IO) {
            try {
                println("🔄 Attempting to connect to: $SERVER_URL")
                val url = URL(SERVER_URL)
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                println("📡 Response Code: ${connection.responseCode}")

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    println("✅ Server response: $response")
                    parseDraftResponse(response)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    println("❌ Server error: $errorResponse")
                    null
                }
            } catch (e: Exception) {
                println("❌ Network exception: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }


    private fun parseDraftResponse(jsonString: String): DraftData {
        val jsonObject = JSONObject(jsonString)

        // Банны
        val bansArray = jsonObject.getJSONArray("bans")
        val bans = mutableListOf<DraftAction>()
        for (i in 0 until bansArray.length()) {
            val banObj = bansArray.getJSONObject(i)
            bans.add(DraftAction(
                type = "ban",
                team = banObj.getInt("team"),
                order = banObj.getInt("order"),
                champion = banObj.getString("champion"),
                position = banObj.getInt("position")
            ))
        }

        // Пики
        val picksArray = jsonObject.getJSONArray("picks")
        val picks = mutableListOf<DraftAction>()
        for (i in 0 until picksArray.length()) {
            val pickObj = picksArray.getJSONObject(i)
            picks.add(DraftAction(
                type = "pick",
                team = pickObj.getInt("team"),
                order = pickObj.getInt("order"),
                champion = if (pickObj.has("champion") && !pickObj.isNull("champion")) {
                    pickObj.getString("champion")
                } else {
                    null
                },
                position = pickObj.getInt("position")
            ))
        }

        // Очки (если есть в ответе сервера)
        val points = mutableMapOf<String, Int>()
        if (jsonObject.has("Points")) {
            val pointsObject = jsonObject.getJSONObject("Points")
            for (key in pointsObject.keys()) {
                points[key] = pointsObject.getInt(key)
            }
        }

        return DraftData(bans, picks, points)
    }
}