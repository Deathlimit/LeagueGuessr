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
        draftInfoText = view.findViewById(R.id.draftInfoText)
        initGameplayViews()
        updateUI()

        startButton.setOnClickListener {
            if (GameState.isGameStarted) {
                // Конец игры - сбрасываем всё
                GameState.endGame()
                resetAllImages() // Сбрасываем все изображения
                draftInfoText.text = "Game ended. Press Start to begin."
                gameplayListener?.onGameEnded()
            } else {
                // Начало игры - загружаем драфт
                if (loadDraftFromJson()) {
                    GameState.startGameWithDraft(
                        GameState.draftData!!,
                        GameState.targetPickPosition!!,
                        requireContext()
                    )
                    draftInfoText.text = "Game started! Select your champion."
                    gameplayListener?.onGameStarted()
                } else {
                    draftInfoText.text = "Failed to load draft. Please try again."
                }
            }
            GameState.saveState(requireContext())
            updateUI()
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
        if (GameState.isGameStarted) {
            GameState.targetPickPosition?.let { targetPosition ->
                val teamText = if (targetPosition.team == 1) "Blue" else "Red"
                draftInfoText.text = "Pick for $teamText team, position ${targetPosition.pickIndex + 1}"
            } ?: run {
                draftInfoText.text = "Game started - select your champion"
            }
        } else {
            draftInfoText.text = "Press Start to begin"
        }
    }

    private fun updateDraftDisplay() {
        GameState.draftData?.let { draft ->
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
                        val imageView = banImageViews[viewIndex]
                        imageView.setImageResource(resourceId)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        imageView.adjustViewBounds = true
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
                    val imageView = pickImageViews[viewIndex]

                    if (pick.champion != null) {
                        // Если чемпион выбран - показываем его изображение
                        val resourceId = getChampionResourceId(pick.champion)
                        imageView.setImageResource(resourceId)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        imageView.adjustViewBounds = true
                    } else {
                        // Если чемпион не выбран - показываем вопрос
                        imageView.setImageResource(R.drawable._unknown_pick_ban)
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
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

    // Новый метод для сброса всех изображений
    private fun resetAllImages() {
        pickImageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable._unknown_pick_ban)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
        banImageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable._unknown_pick_ban)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }

    private fun getChampionResourceId(championName: String): Int {
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

    private fun loadDraftFromJson(): Boolean {
        return try {
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
                true
            } else {
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun updateButtonText() {
        startButton.text = if (GameState.isGameStarted) "End" else "Start"
    }

    private fun addRedBorder(imageView: ImageView) {
        val currentDrawable = imageView.drawable
        val border = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setStroke(8, Color.RED)
            cornerRadius = 0f
        }

        val currentScaleType = imageView.scaleType
        val layerDrawable = LayerDrawable(arrayOf(currentDrawable, border))
        imageView.setImageDrawable(layerDrawable)
        imageView.scaleType = currentScaleType
    }

    override fun onDetach() {
        super.onDetach()
        gameplayListener = null
    }
}