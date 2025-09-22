package com.example.leagueguessr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_classes -> {
                    startActivity(Intent(this, ClassActivity::class.java))
                    true
                }
                R.id.navigation_gameplay -> {
                    startActivity(Intent(this, GameplayActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}