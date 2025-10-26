package com.example.leagueguessr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern

class Activity_Register : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var dbHelper: UserDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = UserDbHelper(this)
        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(username, email, password, confirmPassword)) {
                registerUser(username, email, password)
            }
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, Activity_Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInput(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            etUsername.error = "Введите имя пользователя"
            return false
        }

        if (username.length < 3) {
            etUsername.error = "Имя пользователя должно содержать минимум 3 символа"
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = "Введите email"
            return false
        }

        if (!isValidEmail(email)) {
            etEmail.error = "Введите корректный email"
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

        if (confirmPassword != password) {
            etConfirmPassword.error = "Пароли не совпадают"
            return false
        }

        if (dbHelper.isUsernameTaken(username)) {
            etUsername.error = "Имя пользователя уже занято"
            return false
        }

        if (dbHelper.isEmailTaken(email)) {
            etEmail.error = "Email уже используется"
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        return pattern.matcher(email).matches()
    }

    private fun registerUser(username: String, email: String, password: String) {
        val success = dbHelper.registerUser(username, email, password)

        if (success) {
            Toast.makeText(this, "Регистрация успешна! Теперь войдите в систему.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Activity_Login::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Ошибка регистрации. Попробуйте позже.", Toast.LENGTH_SHORT).show()
        }
    }
}