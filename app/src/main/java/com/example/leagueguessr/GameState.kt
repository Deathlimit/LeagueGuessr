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

    var selectedPickIndex: Int = -1
    var isGameStarted: Boolean = false
    var selectedChampionId: Int = -1
    var isChampionSelected: Boolean = false
    var draftData: DraftData? = null
    var targetPickPosition: PickPosition? = null

    fun initialize(context: Context) {
        loadState(context)
    }

    fun startGameWithDraft(draft: DraftData, targetPosition: PickPosition, context: Context) {
        draftData = draft
        targetPickPosition = targetPosition
        isGameStarted = true
        isChampionSelected = false
        selectedChampionId = -1

        saveState(context)
    }

    fun endGame() {
        isGameStarted = false
        isChampionSelected = false
        selectedChampionId = -1
        // НЕ сбрасываем draftData и targetPickPosition, чтобы сохранить состояние драфта
    }

    fun selectChampion(championId: Int, championName: String) {
        selectedChampionId = championId
        isChampionSelected = true
        // Здесь можно добавить логику проверки правильности выбора
    }

    fun saveState(context: Context) {
        with(getPrefs(context).edit()) {
            putInt(KEY_SELECTED_PICK_INDEX, selectedPickIndex)
            putBoolean(KEY_GAME_STARTED, isGameStarted)
            putInt(KEY_SELECTED_CHAMPION_ID, selectedChampionId)
            putBoolean(KEY_IS_CHAMPION_SELECTED, isChampionSelected)

            targetPickPosition?.let { position ->
                putString(KEY_TARGET_PICK_POSITION, "${position.team},${position.pickIndex}")
            }

            // Сохраняем draftData как JSON
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

            // Загружаем targetPickPosition
            getString(KEY_TARGET_PICK_POSITION, null)?.let { positionStr ->
                val parts = positionStr.split(",")
                if (parts.size == 2) {
                    targetPickPosition = PickPosition(parts[0].toInt(), parts[1].toInt())
                }
            }

            // Загружаем draftData из JSON
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

    fun hasSelectedPick(): Boolean = selectedPickIndex != -1
    fun hasSelectedChampion(): Boolean = isChampionSelected && selectedChampionId != -1

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

    fun getCurrentDraftState(): List<DraftAction> {
        val allActions = mutableListOf<DraftAction>()

        draftData?.let { draft ->
            // Добавляем банны
            allActions.addAll(draft.bans)
            // Добавляем пики
            allActions.addAll(draft.picks)
        }

        return allActions.sortedBy { it.order }
    }

    fun getTargetPickAction(): DraftAction? {
        return targetPickPosition?.let { position ->
            DraftAction(
                type = "pick",
                team = position.team,
                order = calculateOrderForPosition(position),
                champion = null, // Это то, что нужно угадать
                position = position.pickIndex
            )
        }
    }

    private fun calculateOrderForPosition(position: PickPosition): Int {
        // Логика расчета порядка действия в драфте
        // Обычно драфт идет: бан1-бан2-бан1-бан2... пик1-пик2-пик2-пик1...
        // Упрощенная реализация
        return draftData?.bans?.size ?: 0 + position.team * 5 + position.pickIndex
    }
}

data class DraftData(
    val bans: List<DraftAction>,
    val picks: List<DraftAction>
)

data class DraftAction(
    val type: String, // "ban" или "pick"
    val team: Int, // 1 или 2
    val order: Int, // порядок действия в драфте
    val champion: String?, // имя чемпиона (null для целевого пика)
    val position: Int? = null // позиция в команде (0-4)
)

data class PickPosition(
    val team: Int, // 1 или 2
    val pickIndex: Int // 0-4
)