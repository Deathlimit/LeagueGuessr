package com.example.leagueguessr

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class Activity_Gameplay : AppCompatActivity(), Fragment_Gameplay.GameplayListener {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setAppLanguage()
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        if (!isUserLoggedIn()) {
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_gameplay)

        GameState.initialize(this)

        setupNavigation()
        updateNavigationSelection()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Fragment_Gameplay())
                .commit()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    private fun redirectToLogin() {
        val intent = Intent(this, Activity_Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onGameStarted() {
        // TDO: Логика при начале игры
    }

    override fun onGameEnded() {
        // TDO: Логика при окончании игры
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
        bottomNavigation = findViewById(R.id.bottom_navigation)

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