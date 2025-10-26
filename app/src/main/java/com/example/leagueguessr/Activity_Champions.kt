package com.example.leagueguessr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Activity_Champions : AppCompatActivity() {

    private val roleTitles = mapOf(
        "1" to "Fighter",
        "2" to "Mage",
        "3" to "Support",
        "4" to "Assassin",
        "5" to "Marksman",
        "6" to "Tank"
    )

    private val roleRecyclerViewMap = mapOf(
        "1" to R.id.container_class1,
        "2" to R.id.container_class2,
        "3" to R.id.container_class3,
        "4" to R.id.container_class4,
        "5" to R.id.container_class5,
        "6" to R.id.container_class6
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragments_champions)

        setupNavigation()
        setupFragments()
    }

    private fun setupFragments() {
        roleTitles.keys.forEach { role ->
            val fragment = Fragment_ChampionList.newInstance(role) { champion ->
                onChampionSelected(champion)
            }

            supportFragmentManager.beginTransaction()
                .replace(getContainerIdForRole(role), fragment, "fragment_$role")
                .commit()
        }
    }

    private fun getContainerIdForRole(role: String): Int {
        return roleRecyclerViewMap[role] ?: throw IllegalArgumentException("Unknown role: $role")
    }

    private fun onChampionSelected(champion: Data_champion) {
        if (GameState.isGameStarted) {
            // Проверяем, не забанен ли чемпион и не выбран ли уже
            if (GameState.isChampionBanned(champion.name)) {
                // Показать сообщение, что чемпион забанен
                return
            }

            if (GameState.isChampionPicked(champion.name)) {
                // Показать сообщение, что чемпион уже выбран
                return
            }

            // ОБНОВЛЯЕМ ДРАФТ ДАННЫЕ
            GameState.draftData?.let { draft ->
                val targetPosition = GameState.targetPickPosition
                if (targetPosition != null) {
                    // Находим пик в списке picks, который соответствует targetPosition
                    val updatedPicks = draft.picks.map { pick ->
                        if (pick.team == targetPosition.team && pick.position == targetPosition.pickIndex) {
                            pick.copy(champion = champion.name)
                        } else {
                            pick
                        }
                    }.toMutableList()

                    GameState.draftData = draft.copy(picks = updatedPicks)
                }
            }

            GameState.selectChampion(champion.id, champion.name)
            GameState.saveState(this)

            // Завершаем активность и возвращаемся в gameplay
            finish()
        }
    }

    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_classes

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_gameplay -> {
                    val intent = Intent(this, Activity_Gameplay::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.navigation_classes -> {
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, Activity_Profile::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                else -> false
            }
        }
    }

}