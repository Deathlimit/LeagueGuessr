package com.example.leagueguessr

import android.content.Context

object ChampionUtils {

    fun getDrawableChampions(): Map<String, Int> {
        val resources = mutableMapOf<String, Int>()
        val fields = R.drawable::class.java.fields
        for (field in fields) {
            val resourceName = field.name
            if (resourceName.startsWith("_champion_")) {
                val resourceId = field.getInt(null)
                resources[resourceName] = resourceId
            }
        }
        return resources
    }

    fun extractChampionName(fileName: String): String {
        return fileName.removePrefix("_champion_")
            .substringAfter("_")
            .replace("_", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    fun getChampionResourceId(championName: String): Int {
        val drawableResources = getDrawableChampions()

        drawableResources.forEach { (resourceName, resourceId) ->
            val nameFromResource = extractChampionName(resourceName)
            if (nameFromResource.equals(championName, ignoreCase = true)) {
                return resourceId
            }
        }
        return R.drawable._unknown_pick_ban
    }
}