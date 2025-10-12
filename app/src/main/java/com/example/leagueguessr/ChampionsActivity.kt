package com.example.leagueguessr

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChampionsActivity : AppCompatActivity() {

    // Маппинг ролей на RecyclerView
    private val roleRecyclerViewMap = mapOf(
        "1" to R.id.rv_class1, // Fighter
        "2" to R.id.rv_class2, // Mage
        "3" to R.id.rv_class3, // Support
        "4" to R.id.rv_class4, // Assassin
        "5" to R.id.rv_class5, // Marksman
        "6" to R.id.rv_class6  // Tank
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_champions)

        setupNavigation()
        setupChampionsRecyclerViews()
    }

    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_classes

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_gameplay -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_classes -> {
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupChampionsRecyclerViews() {
        // Получаем чемпионов сгруппированных по ролям
        val championsByRole = getChampionsByRole()

        // Настраиваем RecyclerView для каждой роли
        championsByRole.forEach { (role, champions) ->
            val recyclerViewId = roleRecyclerViewMap[role]
            if (recyclerViewId != null && champions.isNotEmpty()) {
                setupRecyclerView(recyclerViewId, champions)
            }
        }
    }

    private fun setupRecyclerView(recyclerViewId: Int, champions: List<Champion>) {
        val recyclerView: RecyclerView = findViewById(recyclerViewId)

        val adapter = ChampionsAdapter(champions) { champion ->
            if (GameState.isGameStarted) {
                GameState.selectChampion(champion.imageResId)
                GameState.saveState(this@ChampionsActivity)

                val intent = Intent(this@ChampionsActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }

        recyclerView.layoutManager = GridLayoutManager(this, 4, GridLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
    }

    private fun getChampionsByRole(): Map<String, List<Champion>> {
        val championsByRole = mutableMapOf<String, MutableList<Champion>>()
        roleRecyclerViewMap.keys.forEach { role ->
            championsByRole[role] = mutableListOf()
        }

        val drawableResources = getDrawableChampions()
        drawableResources.forEach { (resourceName, resourceId) ->
            val roles = extractRolesFromFileName(resourceName)
            val name = extractChampionName(resourceName)

            roles.forEach { role ->
                if (championsByRole.containsKey(role)) {
                    val champion = Champion(
                        id = resourceId,
                        name = name,
                        imageResId = resourceId,
                        roles = roles
                    )
                    championsByRole[role]?.add(champion)
                }
            }
        }

        return championsByRole
    }

    private fun extractChampionName(fileName: String): String {
        return fileName.removePrefix("_champion_")
            .substringAfter("_")
            .replace("_", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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

    private fun extractRolesFromFileName(fileName: String): List<String> {
        val pattern = "_champion_(\\d+)_.+".toRegex()
        val result = pattern.find(fileName)

        val roles = mutableListOf<String>()
        result?.groupValues?.get(1)?.let { roleDigits ->
            roleDigits.forEach { digit ->
                roles.add(digit.toString())
            }
        }
        return roles
    }
}