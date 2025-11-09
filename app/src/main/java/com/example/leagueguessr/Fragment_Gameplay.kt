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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Fragment_Gameplay : Fragment() {

    private lateinit var startButton: Button
    private lateinit var draftInfoText: TextView
    private val pickImageViews = mutableListOf<ImageView>()
    private val banImageViews = mutableListOf<ImageView>()

    private lateinit var loadingText: TextView

    private var loadDraftJob: Job? = null

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
        loadingText = view.findViewById(R.id.loadingText)
        initGameplayViews()
        updateUI()

        startButton.setOnClickListener {
            if (GameState.isGameStarted) {
                if (GameState.isChampionSelected) {
                    checkPointsAndEndGame()
                } else {
                    GameState.endGame()
                    resetAllImages()
                    draftInfoText.text = "Game ended. No champion selected."
                    gameplayListener?.onGameEnded()
                    GameState.saveState(requireContext())
                    updateUI()
                }
            } else {
                // Начало игры
                loadDraftFromServer()
            }
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
                        val resourceId = ChampionUtils.getChampionResourceId(ban.champion)
                        val imageView = banImageViews[viewIndex]
                        imageView.setImageResource(resourceId)
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                        imageView.adjustViewBounds = false
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
                        val resourceId = ChampionUtils.getChampionResourceId(pick.champion)
                        imageView.setImageResource(resourceId)
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                        imageView.adjustViewBounds = false
                    } else {
                        // Если чемпион не выбран - показываем вопрос
                        imageView.setImageResource(R.drawable._unknown_pick_ban)
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                        imageView.adjustViewBounds = false
                    }
                }
            }

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

    private fun resetAllImages() {
        pickImageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable._unknown_pick_ban)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.adjustViewBounds = false
        }
        banImageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable._unknown_pick_ban)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.adjustViewBounds = false
        }
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

            // Загружаем очки
            val points = mutableMapOf<String, Int>()
            if (jsonObject.has("Points")) {
                val pointsObject = jsonObject.getJSONObject("Points")
                for (key in pointsObject.keys()) {
                    points[key] = pointsObject.getInt(key)
                }
            }

            val draftData = DraftData(bans, picks, points)
            GameState.draftData = draftData

            // Пустой пик
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

    private fun checkPointsAndEndGame() {
        if (GameState.isGameStarted && GameState.isChampionSelected) {
            val selectedChampionName = GameState.selectedChampionName
            val points = getPointsForChampion(selectedChampionName)

            saveGameResult(selectedChampionName, points)

            showPointsResult(selectedChampionName, points)
        }

        GameState.endGame()
        resetAllImages()
        draftInfoText.text = "Game ended. Press Start to begin."
        gameplayListener?.onGameEnded()
        GameState.saveState(requireContext())
        updateUI()
    }

    private fun getPointsForChampion(championName: String): Int {
        // Сначала проверяем очки в загруженном драфте
        GameState.draftData?.points?.let { pointsMap ->
            // Ищем точное совпадение
            if (pointsMap.containsKey(championName)) {
                return pointsMap[championName] ?: 0
            }
            // Ищем без учета регистра
            for ((key, value) in pointsMap) {
                if (key.equals(championName, ignoreCase = true)) {
                    return value
                }
            }
        }

        // Если в драфте нет очков, пробуем загрузить из локального файла
        return try {
            val inputStream = requireContext().assets.open("draft.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            if (jsonObject.has("Points")) {
                val pointsObject = jsonObject.getJSONObject("Points")
                if (pointsObject.has(championName)) {
                    pointsObject.getInt(championName)
                } else {

                    for (key in pointsObject.keys()) {
                        if (key.equals(championName, ignoreCase = true)) {
                            return pointsObject.getInt(key)
                        }
                    }
                    0
                }
            } else {
                0
            }
        } catch (e: Exception) {
            println("❌ Error loading points from local file: ${e.message}")
            0
        }
    }

    private fun saveGameResult(championName: String, points: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId != -1) {
            val dbHelper = UserDbHelper(requireContext())
            dbHelper.addGameResult(userId, championName, points)
        }

    }




    private fun showPointsResult(championName: String, points: Int) {
        val message = "You selected: $championName\nPoints: $points"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Game Result")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadDraftFromServer() {
        loadingText.visibility = View.VISIBLE
        draftInfoText.text = "Loading from server..."
        startButton.isEnabled = false

        loadDraftJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val serverDraft = DraftApiService.fetchDraftFromServer()

                if (serverDraft != null) {
                    // Успех - используем серверный драфт
                    setupDraftFromData(serverDraft, "Server")
                } else {
                    // Fallback на локальный файл
                    loadingText.text = "Server unavailable, using local draft"
                    if (loadDraftFromJson()) {
                        setupDraftFromData(GameState.draftData!!, "Local")
                    } else {
                        loadingText.text = "Failed to load any draft"
                    }
                }
            } catch (e: Exception) {
                // Ошибка - пробуем локальный файл
                draftInfoText.text = "Error: ${e.message}"
                if (loadDraftFromJson()) {
                    setupDraftFromData(GameState.draftData!!, "Local")
                }
            } finally {
                startButton.isEnabled = true
                updateUI()
            }
        }
    }

    // Вспомогательный метод для настройки драфта
    private fun setupDraftFromData(draftData: DraftData, source: String) {
        GameState.draftData = draftData

        // Логирование при установке драфта
        println("\n🎮 SETTING UP DRAFT FROM: $source")
        println("📊 Total bans: ${draftData.bans.size}, picks: ${draftData.picks.size}")

        draftData.bans.forEach { ban ->
            println("   🚫 Ban: Team ${ban.team}, Pos ${ban.position}, Champion: ${ban.champion}")
        }

        draftData.picks.forEach { pick ->
            val status = if (pick.champion == null) "EMPTY" else "FILLED"
            println("   ✅ Pick: Team ${pick.team}, Pos ${pick.position}, Champion: ${pick.champion ?: "???"} ($status)")
        }

        val targetPick = draftData.picks.firstOrNull { it.champion == null }

        if (targetPick != null) {
            GameState.targetPickPosition = PickPosition(targetPick.team, targetPick.position ?: 0)
            GameState.startGameWithDraft(draftData, GameState.targetPickPosition!!, requireContext())
            draftInfoText.text = "Game started! ($source)"
            gameplayListener?.onGameStarted()

            // Логирование целевого пика
            println("🎯 TARGET PICK: Team ${targetPick.team}, Position ${targetPick.position}")
        } else {
            draftInfoText.text = "No empty picks found"
            println("⚠️ No empty picks found in draft")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadDraftJob?.cancel()
    }


}