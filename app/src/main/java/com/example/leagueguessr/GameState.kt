package com.example.leagueguessr

import android.content.Context
import android.content.SharedPreferences

object GameState {
     const val PREFS_NAME = "GameStatePrefs"


     const val KEY_SELECTED_PICK_INDEX = "selected_pick_index"
     const val KEY_GAME_STARTED = "game_started"
     const val KEY_SELECTED_CHAMPION_ID = "selected_champion_id"
     const val KEY_IS_CHAMPION_SELECTED = "is_champion_selected"


    var selectedPickIndex: Int = -1
    var isGameStarted: Boolean = false
    var selectedChampionId: Int = -1
    var isChampionSelected: Boolean = false


    fun initialize(context: Context) {
        loadState(context)
    }


    fun startGame(totalPicks: Int): Int {
        selectedPickIndex = (0 until totalPicks).random()
        isGameStarted = true
        isChampionSelected = false
        selectedChampionId = -1
        return selectedPickIndex
    }


    fun endGame() {
        selectedPickIndex = -1
        isGameStarted = false
        isChampionSelected = false
        selectedChampionId = -1
    }


    fun selectChampion(championId: Int) {
        selectedChampionId = championId
        isChampionSelected = true
    }


    fun saveState(context: Context) {
        with(getPrefs(context).edit()) {
            putInt(KEY_SELECTED_PICK_INDEX, selectedPickIndex)
            putBoolean(KEY_GAME_STARTED, isGameStarted)
            putInt(KEY_SELECTED_CHAMPION_ID, selectedChampionId)
            putBoolean(KEY_IS_CHAMPION_SELECTED, isChampionSelected)
            apply()
        }
    }


     fun loadState(context: Context) {
        with(getPrefs(context)) {
            selectedPickIndex = getInt(KEY_SELECTED_PICK_INDEX, -1)
            isGameStarted = getBoolean(KEY_GAME_STARTED, false)
            selectedChampionId = getInt(KEY_SELECTED_CHAMPION_ID, -1)
            isChampionSelected = getBoolean(KEY_IS_CHAMPION_SELECTED, false)
        }
    }


     fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun hasSelectedPick(): Boolean = selectedPickIndex != -1
    fun hasSelectedChampion(): Boolean = isChampionSelected && selectedChampionId != -1
}

data class GameStateData(
    val selectedPickIndex: Int,
    val isGameStarted: Boolean,
    val selectedChampionId: Int,
    val isChampionSelected: Boolean
)