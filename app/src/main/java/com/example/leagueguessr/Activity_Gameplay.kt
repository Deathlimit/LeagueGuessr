package com.example.leagueguessr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class Activity_Gameplay : AppCompatActivity(), Fragment_Gameplay.GameplayListener {

    private lateinit var bottomNavigation: BottomNavigationView // ПЕРЕМЕСТИЛИ ВНУТРЬ КЛАССА

    override fun onCreate(savedInstanceState: Bundle?) {
        setAppLanguage()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragments_gameplay)

        GameState.initialize(this)

        setupNavigation()
        updateNavigationSelection()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Fragment_Gameplay())
                .commit()
        }
    }

    override fun onGameStarted() {
        // Логика при начале игры
    }

    override fun onGameEnded() {
        // Логика при окончании игры
    }

    override fun onLanguageChanged(languageCode: String) {
        changeLanguage(languageCode)
    }

    private fun changeLanguage(languageCode: String) {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        prefs.edit().putString("language", languageCode).apply()
        recreate()
    }

    private fun setAppLanguage() {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val languageCode = prefs.getString("language", "ru") ?: "ru"

        val locale = when (languageCode) {
            "en" -> Locale.ENGLISH
            "ru" -> Locale("ru", "RU")
            else -> Locale.getDefault()
        }
        Locale.setDefault(locale)

        resources.configuration.setLocale(locale)
        resources.updateConfiguration(resources.configuration, resources.displayMetrics)
    }

    private fun setupNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation) // ИНИЦИАЛИЗИРУЕМ ЗДЕСЬ

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_classes -> {
                    val intent = Intent(this, Activity_Champions::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.navigation_gameplay -> {
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, Activity_Profile::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                else -> false
            }
        }
    }

    private fun updateNavigationSelection() {
        bottomNavigation.selectedItemId = R.id.navigation_gameplay
    }

    override fun onResume() {
        super.onResume()
        updateNavigationSelection()

        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? Fragment_Gameplay
        fragment?.updateUI()
    }

    override fun onPause() {
        super.onPause()
        GameState.saveState(this)
    }
}