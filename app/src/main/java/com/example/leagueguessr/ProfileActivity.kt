// ProfileActivity.kt
package com.example.leagueguessr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_classes -> {
                    val intent = Intent(this, ChampionsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.navigation_gameplay -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.navigation_profile -> {
                    true
                }
                else -> false
            }
        }

        setupHistoryRecyclerView()
    }

     fun setupHistoryRecyclerView() {
        val historyRecyclerView: RecyclerView = findViewById(R.id.historyRecyclerView)
        val historyItems = generateTestHistoryData()

        val adapter = HistoryAdapter(historyItems)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = adapter
    }

     fun generateTestHistoryData(): List<Data_history> {
        val testData = mutableListOf<Data_history>()
        val results = listOf("Correct", "Incorrect", "Correct", "Incorrect", "Correct")
        val dates = listOf(
            "2024-01-15 14:30",
            "2024-01-14 16:45",
            "2024-01-13 11:20",
            "2024-01-12 19:15",
            "2024-01-11 09:30"
        )

        for (i in results.indices) {
            testData.add(
                Data_history(
                    championImageResId = R.drawable._unknown_pick_ban,
                    result = results[i],
                    date = dates[i]
                )
            )
        }

        return testData
    }
}