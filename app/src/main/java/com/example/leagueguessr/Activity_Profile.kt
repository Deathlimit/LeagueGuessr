package com.example.leagueguessr

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.InputStream

class Activity_Profile : AppCompatActivity() {

    private lateinit var rankTextView: TextView
    private lateinit var usernameTextView: TextView
    private lateinit var descriptionTextView: TextView

    private lateinit var btnEditDescription: Button
    private lateinit var btnLogout: Button
    private lateinit var avatarImageView: ImageView
    private lateinit var btnChangeAvatar: Button
    private lateinit var dbHelper: UserDbHelper
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_profile)

            sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)


            if (!isUserLoggedIn()) {
                redirectToLogin()
                return
            }

            dbHelper = UserDbHelper(this)

            initViews()
            setupNavigation()
            setupUserInfo()
            setupHistoryRecyclerView()
            updateRank()
            setupLogoutButton()
            loadUserDescription()
            loadAvatar()
            setupAvatarButton()
            checkDatabaseHealth()
            setupDescriptionButton()
    }

    private fun initViews() {
        rankTextView = findViewById(R.id.rankText)
        usernameTextView = findViewById(R.id.usernameText)
        btnLogout = findViewById(R.id.btnLogout)
        avatarImageView = findViewById(R.id.avatarImageView)
        descriptionTextView = findViewById(R.id.descriptionText)
        btnEditDescription = findViewById(R.id.btnEditDescription)
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar)
    }

    private fun loadAvatar() {
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId != -1) {

                val avatarBitmap = dbHelper.getUserAvatar(userId)
                if (avatarBitmap != null) {
                    avatarImageView.setImageBitmap(avatarBitmap)
                } else {
                    avatarImageView.setImageResource(R.drawable.default_avatar)
                }
        } else {
            avatarImageView.setImageResource(R.drawable.default_avatar)
        }
    }

    private fun setupAvatarButton() {
        btnChangeAvatar.setOnClickListener {
            openImageChooser()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(intent, "Выберите аватар"),
            PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            try {
                val inputStream: InputStream? = uri?.let { contentResolver.openInputStream(it) }
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // Сжимаем изображение
                val compressedBitmap = compressBitmap(originalBitmap, 400, 400)

                val userId = sharedPreferences.getInt("user_id", -1)
                if (userId != -1 && compressedBitmap != null) {
                    val success = dbHelper.updateUserAvatar(userId, compressedBitmap)
                    if (success) {
                        avatarImageView.setImageBitmap(compressedBitmap)
                        Toast.makeText(this, "Аватар обновлен", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Ошибка обновления аватара", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compressBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap? {
        return try {
            var width = bitmap.width
            var height = bitmap.height

            if (width > maxWidth || height > maxHeight) {
                val ratio = width.toFloat() / height.toFloat()
                if (ratio > 1) {
                    width = maxWidth
                    height = (maxWidth / ratio).toInt()
                } else {
                    height = maxHeight
                    width = (maxHeight * ratio).toInt()
                }
            }

            Bitmap.createScaledBitmap(bitmap, width, height, true)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupLogoutButton() {
        btnLogout.setOnClickListener {
            logoutUser()
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

    private fun logoutUser() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        redirectToLogin()
    }

    private fun setupUserInfo() {
        val username = sharedPreferences.getString("username", "")
        usernameTextView.text = "Пользователь: $username"
    }

    private fun loadUserDescription() {
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
            val description = dbHelper.getUserDescription(userId)
            descriptionTextView.text = description ?: "Описание не добавлено"
        }
    }

    private fun setupDescriptionButton() {
        btnEditDescription.setOnClickListener {
            showEditDescriptionDialog()
        }
    }

    private fun showEditDescriptionDialog() {
        val userId = sharedPreferences.getInt("user_id", -1)
        val currentDescription = dbHelper.getUserDescription(userId)

        val editText = EditText(this)
        editText.setText(currentDescription)
        editText.hint = "Введите описание профиля"

        AlertDialog.Builder(this)
            .setTitle("Редактировать описание")
            .setView(editText)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val newDescription = editText.text.toString().trim()
                if (newDescription.isNotEmpty()) {
                    updateUserDescription(newDescription)
                } else {

                    updateUserDescription("")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Очистить") { dialog, _ ->
                updateUserDescription("")
                dialog.dismiss()
            }
            .show()
    }

    private fun updateUserDescription(description: String) {
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
            val success = dbHelper.updateUserDescription(userId, description)
            if (success) {
                loadUserDescription()
                Toast.makeText(this, "Описание обновлено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка обновления описания", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRank() {
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
            val totalPoints = dbHelper.getTotalPointsLast10Games(userId)
            val rank = determineRank(totalPoints)
            rankTextView.text = "Ранг: $rank ($totalPoints очков)"
        }
    }

    private fun determineRank(totalPoints: Int): String {
        return when {
            totalPoints >= 8000 -> "Властелин"
            totalPoints >= 5000 -> "Легенда"
            totalPoints >= 1000 -> "Рекрут"
            else -> "Новичок"
        }
    }

    private fun setupHistoryRecyclerView() {
        val historyRecyclerView: RecyclerView = findViewById(R.id.historyRecyclerView)
        val userId = sharedPreferences.getInt("user_id", -1)
        val historyItems = if (userId != -1) dbHelper.getUserGameHistory(userId) else emptyList()

        val adapter = Adapter_History(historyItems)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = adapter
    }




    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_classes -> {
                    val intent = Intent(this, Activity_Champions::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.navigation_gameplay -> {
                    val intent = Intent(this, Activity_Gameplay::class.java)
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
    }
    private fun checkDatabaseHealth() {
        try {
            val userId = sharedPreferences.getInt("user_id", -1)
            if (userId != -1) {
                val history = dbHelper.getUserGameHistory(userId)
                Toast.makeText(this, "\"Database health check: User history count = ${history.size}\"", Toast.LENGTH_LONG).show()

                val avatar = dbHelper.getUserAvatar(userId)
                Toast.makeText(this, "Database health check: Avatar exists = ${avatar != null}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Database health check failed: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        updateRank()
    }
}