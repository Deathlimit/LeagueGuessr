package com.example.leagueguessr

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.json.JSONObject

class Fragment_Gameplay : Fragment() {

    private lateinit var startButton: Button
    private lateinit var loadDraftButton: Button
    private lateinit var draftInfoText: TextView
    private val pickImageViews = mutableListOf<ImageView>()
    private val banImageViews = mutableListOf<ImageView>()

    interface GameplayListener {
        fun onGameStarted()
        fun onGameEnded()
        fun onLanguageChanged(languageCode: String)
    }

    private var gameplayListener: GameplayListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GameplayListener) {
            gameplayListener = context
        } else {
            throw RuntimeException("$context must implement GameplayListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.gameplay_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton = view.findViewById(R.id.startButton)
        loadDraftButton = view.findViewById(R.id.loadDraftButton)
        draftInfoText = view.findViewById(R.id.draftInfoText)
        initGameplayViews()
        updateUI()

        startButton.setOnClickListener {
            if (GameState.isGameStarted) {
                GameState.endGame()
                gameplayListener?.onGameEnded()
            } else {
                // Игра теперь начинается только после загрузки драфта
                if (GameState.draftData != null && GameState.targetPickPosition != null) {
                    GameState.startGameWithDraft(
                        GameState.draftData!!,
                        GameState.targetPickPosition!!,
                        requireContext()
                    )
                    gameplayListener?.onGameStarted()
                } else {
                    // Показать сообщение, что нужно сначала загрузить драфт
                    draftInfoText.text = "Please load draft first"
                }
            }
            GameState.saveState(requireContext())
            updateUI()
        }

        loadDraftButton.setOnClickListener {
            loadDraftFromJson()
        }

        view.findViewById<Button>(R.id.button_russian).setOnClickListener {
            gameplayListener?.onLanguageChanged("ru")
        }

        view.findViewById<Button>(R.id.button_english).setOnClickListener {
            gameplayListener?.onLanguageChanged("en")
        }
    }

    private fun initGameplayViews() {
        // Инициализация пиков
        val pickIds = listOf(
            R.id.PickLeft1, R.id.PickLeft2, R.id.PickLeft3, R.id.PickLeft4, R.id.PickLeft5,
            R.id.PickRight1, R.id.PickRight2, R.id.PickRight3, R.id.PickRight4, R.id.PickRight5
        )

        pickIds.forEach { id ->
            requireView().findViewById<ImageView>(id).let { imageView ->
                pickImageViews.add(imageView)
            }
        }

        // Инициализация баннов
        val banIds = listOf(
            R.id.BanLeft1, R.id.BanLeft2, R.id.BanLeft3, R.id.BanLeft4, R.id.BanLeft5,
            R.id.BanRight1, R.id.BanRight2, R.id.BanRight3, R.id.BanRight4, R.id.BanRight5
        )

        banIds.forEach { id ->
            requireView().findViewById<ImageView>(id).let { imageView ->
                banImageViews.add(imageView)
            }
        }
    }

    fun updateUI() {
        clearAllBorders()
        updateButtonText()
        updateDraftInfo()

        if (GameState.draftData != null) {
            updateDraftDisplay()

            // Подсвечиваем целевой пик только если игра начата
            if (GameState.isGameStarted) {
                GameState.targetPickPosition?.let { targetPosition ->
                    val viewIndex = if (targetPosition.team == 1) {
                        targetPosition.pickIndex
                    } else {
                        targetPosition.pickIndex + 5
                    }

                    if (viewIndex in pickImageViews.indices) {
                        addRedBorder(pickImageViews[viewIndex])
                    }
                }
            }
        }
    }

    private fun updateDraftInfo() {
        GameState.targetPickPosition?.let { targetPosition ->
            val teamText = if (targetPosition.team == 1) "Blue" else "Red"
            draftInfoText.text = "Pick for $teamText team, position ${targetPosition.pickIndex + 1}"
        } ?: run {
            draftInfoText.text = "No target position set"
        }
    }

    private fun updateDraftDisplay() {
        GameState.draftData?.let { draft ->
            // Очищаем все изображения
            clearAllBorders()

            // Отображаем банны
            draft.bans.forEach { ban ->
                if (ban.champion != null) {
                    val viewIndex = if (ban.team == 1) {
                        ban.position ?: 0
                    } else {
                        (ban.position ?: 0) + 5
                    }

                    if (viewIndex in banImageViews.indices) {
                        val resourceId = getChampionResourceId(ban.champion)
                        banImageViews[viewIndex].setImageResource(resourceId)
                    }
                }
            }

            // Отображаем пики
            draft.picks.forEach { pick ->
                val viewIndex = if (pick.team == 1) {
                    pick.position ?: 0
                } else {
                    (pick.position ?: 0) + 5
                }

                if (viewIndex in pickImageViews.indices) {
                    if (pick.champion != null) {
                        // Если чемпион выбран - показываем его изображение
                        val resourceId = getChampionResourceId(pick.champion)
                        pickImageViews[viewIndex].setImageResource(resourceId)
                    } else {
                        // Если чемпион не выбран - показываем вопрос
                        pickImageViews[viewIndex].setImageResource(R.drawable._unknown_pick_ban)
                    }
                }
            }

            // Подсвечиваем целевой пик красной рамкой, если игра активна
            if (GameState.isGameStarted) {
                GameState.targetPickPosition?.let { targetPosition ->
                    val viewIndex = if (targetPosition.team == 1) {
                        targetPosition.pickIndex
                    } else {
                        targetPosition.pickIndex + 5
                    }

                    if (viewIndex in pickImageViews.indices) {
                        addRedBorder(pickImageViews[viewIndex])
                    }
                }
            }
        }
    }

    private fun getChampionResourceId(championName: String): Int {
        // Используем тот же подход, что и в Fragment_ChampionList
        val drawableResources = getDrawableChampions()
        val normalizedChampionName = championName.lowercase().replace(" ", "_")

        drawableResources.forEach { (resourceName, resourceId) ->
            val nameFromResource = extractChampionName(resourceName)
            if (nameFromResource.equals(championName, ignoreCase = true)) {
                return resourceId
            }
        }
        return R.drawable._unknown_pick_ban
    }

    private fun getDrawableChampions(): Map<String, Int> {
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

    private fun extractChampionName(fileName: String): String {
        return fileName.removePrefix("_champion_")
            .substringAfter("_")
            .replace("_", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun loadDraftFromJson() {
        try {
            val jsonString = requireContext().assets.open("draft.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            // Загружаем банны
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

            // Загружаем пики
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

            val draftData = DraftData(bans, picks)
            GameState.draftData = draftData

            // Находим целевой пик (первый пик с champion = null)
            val targetPick = picks.firstOrNull { it.champion == null }
            if (targetPick != null) {
                GameState.targetPickPosition = PickPosition(targetPick.team, targetPick.position ?: 0)
                draftInfoText.text = "Draft loaded! Target position found."
            } else {
                draftInfoText.text = "Draft loaded but no target position found"
            }

            updateUI()

        } catch (e: Exception) {
            draftInfoText.text = "Error loading draft: ${e.message}"
        }
    }

    private fun updateButtonText() {
        startButton.text = if (GameState.isGameStarted) "End" else "Start"
    }

    private fun clearAllBorders() {
        pickImageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable._unknown_pick_ban)
        }
        banImageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable._unknown_pick_ban)
        }
    }

    private fun addRedBorder(imageView: ImageView) {
        val currentDrawable = imageView.drawable
        val border = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setStroke(18, Color.RED)
            cornerRadius = 0f
        }
        imageView.setImageDrawable(LayerDrawable(arrayOf(currentDrawable, border)))
    }

    override fun onDetach() {
        super.onDetach()
        gameplayListener = null
    }
}