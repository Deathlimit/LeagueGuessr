package com.example.leagueguessr

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Activity_Login : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var dbHelper: UserDbHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = UserDbHelper(this)
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        if (isUserLoggedIn()) {
            startMainActivity()
            return
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInput(username, password)) {
                loginUser(username, password)
            }
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, Activity_Register::class.java)
            startActivity(intent)
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            etUsername.error = "Введите имя пользователя"
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Введите пароль"
            return false
        }

        if (password.length < 6) {
            etPassword.error = "Пароль должен содержать минимум 6 символов"
            return false
        }

        return true
    }

    private fun loginUser(username: String, password: String) {
        val user = dbHelper.loginUser(username, password)

        if (user != null) {
            saveUserToPrefs(user)
            Toast.makeText(this, "Добро пожаловать, ${user.username}!", Toast.LENGTH_SHORT).show()
            startMainActivity()
        } else {
            Toast.makeText(this, "Неверное имя пользователя или пароль", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserToPrefs(user: User) {
        val editor = sharedPreferences.edit()
        editor.putInt("user_id", user.userId)
        editor.putString("username", user.username)
        editor.putString("email", user.email)
        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    private fun startMainActivity() {
        val intent = Intent(this, Activity_Gameplay::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}