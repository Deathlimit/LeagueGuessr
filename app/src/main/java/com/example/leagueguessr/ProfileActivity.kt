package com.example.leagueguessr

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)


        // Устанавливаем выбранный пункт для gameplay
        bottomNavigation.selectedItemId = R.id.navigation_gameplay

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_classes -> {
                    // Переход к активности Champions
                    val intent = Intent(this, ChampionsActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_gameplay -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_profile -> {
                    true
                }
                else -> false
            }
        }

    }

}