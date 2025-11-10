package com.example.leagueguessr

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Api_Draft {

    private const val SERVER_URL = "https://loldraftserver.onrender.com/draft"

    suspend fun fetchDraftFromServer(): DraftData? {
        return withContext(Dispatchers.IO) {
            val url = URL(SERVER_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseDraftResponse(response)
            } else {
                null
            }
        }
    }

    private fun parseDraftResponse(jsonString: String): DraftData {
        val jsonObject = JSONObject(jsonString)

        val bans = mutableListOf<DraftAction>()
        val bansArray = jsonObject.getJSONArray("bans")
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

        val picks = mutableListOf<DraftAction>()
        val picksArray = jsonObject.getJSONArray("picks")
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

        val points = mutableMapOf<String, Int>()
        val pointsObject = jsonObject.getJSONObject("Points")
        for (key in pointsObject.keys()) {
            points[key] = pointsObject.getInt(key)
        }


        return DraftData(bans, picks, points)
    }
}