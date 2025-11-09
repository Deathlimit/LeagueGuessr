package com.example.leagueguessr

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GameState {
    const val PREFS_NAME = "GameStatePrefs"
    const val KEY_SELECTED_PICK_INDEX = "selected_pick_index"
    const val KEY_GAME_STARTED = "game_started"
    const val KEY_SELECTED_CHAMPION_ID = "selected_champion_id"
    const val KEY_IS_CHAMPION_SELECTED = "is_champion_selected"
    const val KEY_DRAFT_DATA = "draft_data"
    const val KEY_TARGET_PICK_POSITION = "target_pick_position"
    const val KEY_SELECTED_CHAMPION_NAME = "selected_champion_name"

    var selectedPickIndex: Int = -1
    var isGameStarted: Boolean = false
    var selectedChampionId: Int = -1
    var isChampionSelected: Boolean = false
    var draftData: DraftData? = null
    var targetPickPosition: PickPosition? = null
    var selectedChampionName: String = ""
    var isDraftFromServer: Boolean = false


    fun initialize(context: Context) {
        loadState(context)
    }

    fun startGameWithDraft(draft: DraftData, targetPosition: PickPosition, context: Context) {
        draftData = draft
        targetPickPosition = targetPosition
        isGameStarted = true
        isChampionSelected = false
        selectedChampionId = -1
        isDraftFromServer = true

        saveState(context)
    }

    fun endGame() {
        selectedPickIndex = -1
        isGameStarted = false
        isChampionSelected = false
        selectedChampionId = -1
        draftData = null
        targetPickPosition = null
        selectedChampionName = ""

    }


    fun selectChampion(championId: Int, championName: String) {
        selectedChampionId = championId
        selectedChampionName = championName
        isChampionSelected = true
    }

    fun saveState(context: Context) {
        with(getPrefs(context).edit()) {
            putInt(KEY_SELECTED_PICK_INDEX, selectedPickIndex)
            putBoolean(KEY_GAME_STARTED, isGameStarted)
            putInt(KEY_SELECTED_CHAMPION_ID, selectedChampionId)
            putBoolean(KEY_IS_CHAMPION_SELECTED, isChampionSelected)
            putString(KEY_SELECTED_CHAMPION_NAME, selectedChampionName)


            targetPickPosition?.let { position ->
                putString(KEY_TARGET_PICK_POSITION, "${position.team},${position.pickIndex}")
            }

            draftData?.let { draft ->
                val gson = Gson()
                val json = gson.toJson(draft)
                putString(KEY_DRAFT_DATA, json)
            }

            apply()
        }
    }

    fun loadState(context: Context) {
        with(getPrefs(context)) {
            selectedPickIndex = getInt(KEY_SELECTED_PICK_INDEX, -1)
            isGameStarted = getBoolean(KEY_GAME_STARTED, false)
            selectedChampionId = getInt(KEY_SELECTED_CHAMPION_ID, -1)
            isChampionSelected = getBoolean(KEY_IS_CHAMPION_SELECTED, false)
            selectedChampionName = getString(KEY_SELECTED_CHAMPION_NAME, "").toString()

            getString(KEY_TARGET_PICK_POSITION, null)?.let { positionStr ->
                val parts = positionStr.split(",")
                if (parts.size == 2) {
                    targetPickPosition = PickPosition(parts[0].toInt(), parts[1].toInt())
                }
            }

            val json = getString(KEY_DRAFT_DATA, null)
            json?.let {
                val gson = Gson()
                val type = object : TypeToken<DraftData>() {}.type
                draftData = gson.fromJson(it, type)
            }
        }
    }

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }


    fun isChampionBanned(championName: String): Boolean {
        return draftData?.let { draft ->
            draft.bans.any { it.champion == championName }
        } ?: false
    }

    fun isChampionPicked(championName: String): Boolean {
        return draftData?.let { draft ->
            draft.picks.any { it.champion == championName }
        } ?: false
    }
}

data class DraftData(
    val bans: List<DraftAction>,
    val picks: List<DraftAction>,
    val points: Map<String, Int> = emptyMap()
)

data class DraftAction(
    val type: String,
    val team: Int,
    val order: Int,
    val champion: String?,
    val position: Int? = null
)

data class PickPosition(
    val team: Int,
    val pickIndex: Int
)