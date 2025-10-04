package com.example.leagueguessr

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChampionsActivity : AppCompatActivity() {

    private val roleContainerMap = mapOf(
        "1" to R.id.container_class1,
        "2" to R.id.container_class2,
        "3" to R.id.container_class3,
        "4" to R.id.container_class4,
        "5" to R.id.container_class5,
        "6" to R.id.container_class6
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_champions)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Устанавливаем выбранный пункт для classes
        bottomNavigation.selectedItemId = R.id.navigation_classes

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_gameplay -> {

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
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

        createImageGrid()
    }

    private fun createImageGrid() {
        try {
            val drawableResources = getDrawableChampions()
            val imagesByRole = mutableMapOf<String, MutableList<Int>>()

            roleContainerMap.keys.forEach { role -> imagesByRole[role] = mutableListOf() }

            drawableResources.forEach { (resourceName, resourceId) ->
                val roles = extractRolesFromFileName(resourceName)
                roles.forEach { role ->
                    if (imagesByRole.containsKey(role)) {
                        imagesByRole[role]?.add(resourceId)
                    }
                }
            }

            createImagesInContainers(imagesByRole)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDrawableChampions(): Map<String, Int> {
        val resources = mutableMapOf<String, Int>()
        try {
            val fields = R.drawable::class.java.fields
            for (field in fields) {
                val resourceName = field.name
                if (resourceName.startsWith("_champion_")) {
                    val resourceId = field.getInt(null)
                    resources[resourceName] = resourceId
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    private fun createImagesInContainers(imagesByRole: Map<String, List<Int>>) {
        try {
            val screenHeight = resources.displayMetrics.heightPixels
            val availableHeight = screenHeight - 300 // минус высота навигации
            val rowHeight = availableHeight / 7
            val imagesPerRow = 4

            imagesByRole.forEach { (role, imageResources) ->
                val containerId = roleContainerMap[role]
                if (containerId != null && imageResources.isNotEmpty()) {
                    val container = findViewById<LinearLayout>(containerId)
                    createImagesForContainer(container, imageResources, rowHeight, imagesPerRow)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createImagesForContainer(
        container: LinearLayout,
        imageResources: List<Int>,
        rowHeight: Int,
        imagesPerRow: Int
    ) {
        for (i in imageResources.indices step imagesPerRow) {
            val rowLayout = createRowLayout(rowHeight)

            for (j in 0 until imagesPerRow) {
                val index = i + j
                if (index < imageResources.size) {
                    val imageView = createImageView(imageResources[index], rowHeight)
                    rowLayout.addView(imageView)
                } else {
                    val emptyView = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            rowHeight,
                            1f
                        )
                    }
                    rowLayout.addView(emptyView)
                }
            }

            container.addView(rowLayout)
        }
    }

    private fun createRowLayout(rowHeight: Int): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                rowHeight
            )
            orientation = LinearLayout.HORIZONTAL
        }
    }

    private fun createImageView(imageResource: Int, rowHeight: Int): ImageView {
        return ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                rowHeight,
                1f
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            setImageResource(imageResource)
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
        }
    }
}