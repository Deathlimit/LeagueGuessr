package com.example.leagueguessr

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.graphics.drawable.GradientDrawable
import android.graphics.Color
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {
    val roleContainerMap = mapOf(
        "1" to R.id.container_class1,  //fighter
        "2" to R.id.container_class2,  //mage
        "3" to R.id.container_class3,  //support
        "4" to R.id.container_class4,  //assassin
        "5" to R.id.container_class5,  //marksman
        "6" to R.id.container_class6   //tank
    )

    var isBorderActive = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //системные отступы
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container))
        {
        view, insets -> val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var containerMain: FrameLayout
        var layoutClasses: ScrollView
        var bottomNavigation: BottomNavigationView

        containerMain = findViewById(R.id.container_main)
        layoutClasses = findViewById(R.id.layout_classes)
        bottomNavigation = findViewById(R.id.bottom_navigation)


        val gameplayLayout = layoutInflater.inflate(R.layout.gameplay_layout, containerMain, false)
        containerMain.addView(gameplayLayout)
        gameplayLayout.visibility = View.GONE


        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_classes -> {
                    layoutClasses.visibility = View.VISIBLE
                    gameplayLayout.visibility = View.GONE
                    true
                }
                R.id.navigation_gameplay -> {
                    layoutClasses.visibility = View.GONE
                    gameplayLayout.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }


        createImageGrid()

        var startButton: Button

        startButton = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            toggleImageBorder()
        }

    }

    private fun createImageGrid() {
        //ищем чемпионов
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
        //_champion_52_corki.png

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
        val screenHeight = resources.displayMetrics.heightPixels
        val bottomBarHeight = 300
        val availableHeight = screenHeight - bottomBarHeight
        val rowsOnScreen = 7
        val rowHeight = availableHeight / rowsOnScreen
        val imagesPerRow = 4

        imagesByRole.forEach { (role, imageResources) ->
            val containerId = roleContainerMap[role]
            if (containerId != null && imageResources.isNotEmpty()) {
                val container = findViewById<LinearLayout>(containerId)
                createImagesForContainer(container, imageResources, rowHeight, imagesPerRow)
            }
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
                    //пустые
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
            setPadding(0, 0, 0, 0)
        }
    }

    private fun createImageView(imageResource: Int, rowHeight: Int): ImageView {
        return ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                rowHeight,
                1f
            ).apply {
                setMargins(5, 5, 5, 5)
            }
            setImageResource(imageResource)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            setPadding(0, 0, 0, 0)

            setOnClickListener {
                //попозже
            }
        }
    }

    private fun toggleImageBorder() {
        isBorderActive = !isBorderActive
        val linearLayout = findViewById<LinearLayout>(R.id.container_class1)
        val firstChild = (linearLayout.getChildAt(0) as ViewGroup).getChildAt(0)

        if (isBorderActive) {
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.setColor(Color.TRANSPARENT)
            drawable.setStroke(50, Color.RED)
            drawable.cornerRadius = 1f

            firstChild.setPadding(5, 10, 5, 10)
            firstChild.background = drawable
        } else {
            firstChild.setPadding(0, 0, 0, 0)
            firstChild.background = null
        }
    }

}