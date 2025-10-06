package com.example.leagueguessr

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import java.util.Locale
import android.view.View

class MainActivity : AppCompatActivity() {

     lateinit var startButton: Button
     val pickImageViews = mutableListOf<ImageView>()


    override fun onCreate(savedInstanceState: Bundle?) {
        setAppLanguage()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GameState.initialize(this)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        startButton = findViewById(R.id.startButton)

        bottomNavigation.selectedItemId = R.id.navigation_gameplay

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_classes -> {
                    val intent = Intent(this, ChampionsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_gameplay -> {
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

        initGameplayViews()
        updateUI()

        startButton.setOnClickListener {
            if (GameState.isGameStarted) {
                GameState.endGame()
            } else {
                GameState.startGame(pickImageViews.size)
            }
            GameState.saveState(this)
            updateUI()
        }
    }

     fun initGameplayViews() {
        val pickIds = listOf(
            R.id.PickLeft1, R.id.PickLeft2, R.id.PickLeft3, R.id.PickLeft4, R.id.PickLeft5,
            R.id.PickRight1, R.id.PickRight2, R.id.PickRight3, R.id.PickRight4, R.id.PickRight5
        )

        pickIds.forEach { id -> findViewById<ImageView>(id).let {
            imageView -> pickImageViews.add(imageView)
            }
        }
    }


     fun updateUI() {
        clearAllBorders()
        updateButtonText()

        if (GameState.hasSelectedPick()) {
            val selectedImageView = pickImageViews[GameState.selectedPickIndex]

            if (GameState.hasSelectedChampion()) {
                setImageWithXmlAnimation(selectedImageView, GameState.selectedChampionId)
            } else {
                setImageWithXmlAnimation(selectedImageView, R.drawable._unknown_pick_ban)
            }
        }
    }

     fun updateButtonText() {
        startButton.text = if (GameState.isGameStarted) "End" else "Start"
    }

     fun clearAllBorders() {
        pickImageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable._unknown_pick_ban)
            }
        }

    fun setImageWithXmlAnimation(imageView: ImageView, imageResId: Int) {
        imageView.setImageResource(imageResId)

        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.select_champion_anim)
        imageView.startAnimation(animation)

        addRedBorder(imageView)
    }
     fun addRedBorder(imageView: ImageView) {

        val currentDrawable = imageView.drawable

        val border = GradientDrawable()
        border.setColor(Color.TRANSPARENT)
        border.setStroke(18, Color.RED)
        border.cornerRadius = 0f

        val layers = arrayOf<Drawable?>(currentDrawable, border)

        imageView.setImageDrawable(LayerDrawable(layers))
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        GameState.saveState(this)
    }

   fun changeLanguage(languageCode: String) {
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        prefs.edit().putString("language", languageCode).apply()

        recreate()
    }

    fun setAppLanguage() {
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

    fun setRussian(view: View) {
        changeLanguage("ru")
    }

    fun setEnglish(view: View) {
        changeLanguage("en")
    }

}