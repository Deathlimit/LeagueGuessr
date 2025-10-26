package com.example.leagueguessr

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

class UserDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "LeagueGuessr.db"

        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_AVATAR = "avatar"
        const val COLUMN_CREATED_AT = "created_at"

        const val TABLE_GAME_HISTORY = "game_history"
        const val COLUMN_HISTORY_ID = "history_id"
        const val COLUMN_CHAMPION_NAME = "champion_name"
        const val COLUMN_POINTS = "points"
        const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Таблица пользователей с аватаркой как BLOB
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_AVATAR BLOB,
                $COLUMN_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // Таблица истории игр
        val createHistoryTable = """
            CREATE TABLE $TABLE_GAME_HISTORY (
                $COLUMN_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_CHAMPION_NAME TEXT NOT NULL,
                $COLUMN_POINTS INTEGER NOT NULL,
                $COLUMN_DATE DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAME_HISTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // Регистрация пользователя
    fun registerUser(username: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, hashPassword(password))
        }

        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    // Авторизация пользователя (без загрузки аватарки для скорости)
    fun loginUser(username: String, password: String): User? {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, hashPassword(password))

        val cursor: Cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_EMAIL), // Не загружаем аватарку при логине
            selection,
            selectionArgs,
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            User(
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            )
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }

    // Обновление аватарки пользователя
    fun updateUserAvatar(userId: Int, avatarBitmap: Bitmap): Boolean {
        val db = writableDatabase

        // Сжимаем Bitmap в байтовый массив
        val outputStream = ByteArrayOutputStream()
        avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val avatarBytes = outputStream.toByteArray()

        val values = ContentValues().apply {
            put(COLUMN_AVATAR, avatarBytes)
        }

        val result = db.update(TABLE_USERS, values,
            "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
        db.close()
        return result > 0
    }

    // Получение аватарки пользователя
// Получение аватарки пользователя
    fun getUserAvatar(userId: Int): Bitmap? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_AVATAR),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        val bitmap = if (cursor.moveToFirst()) {
            val avatarBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_AVATAR))
            // ДОБАВЬТЕ ПРОВЕРКУ НА NULL И ПУСТОЙ МАССИВ
            if (avatarBytes != null && avatarBytes.isNotEmpty()) {
                BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.size)
            } else {
                null
            }
        } else {
            null
        }
        cursor.close()
        db.close()
        return bitmap
    }

    // Проверка существования пользователя
    fun isUsernameTaken(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Проверка существования email
    fun isEmailTaken(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Добавление результата игры
    fun addGameResult(userId: Int, championName: String, points: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_CHAMPION_NAME, championName)
            put(COLUMN_POINTS, points)
        }
        db.insert(TABLE_GAME_HISTORY, null, values)
        db.close()
    }

    // Получение истории игр пользователя
    fun getUserGameHistory(userId: Int): List<GameHistory> {
        val historyList = mutableListOf<GameHistory>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GAME_HISTORY,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_DATE DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                historyList.add(
                    GameHistory(
                        historyId = getInt(getColumnIndexOrThrow(COLUMN_HISTORY_ID)),
                        userId = getInt(getColumnIndexOrThrow(COLUMN_USER_ID)),
                        championName = getString(getColumnIndexOrThrow(COLUMN_CHAMPION_NAME)),
                        points = getInt(getColumnIndexOrThrow(COLUMN_POINTS)),
                        date = getString(getColumnIndexOrThrow(COLUMN_DATE))
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return historyList
    }

    // Получение суммы очков за последние 10 игр
    fun getTotalPointsLast10Games(userId: Int): Int {
        val db = readableDatabase
        val query = """
            SELECT SUM($COLUMN_POINTS) 
            FROM (
                SELECT $COLUMN_POINTS 
                FROM $TABLE_GAME_HISTORY 
                WHERE $COLUMN_USER_ID = ? 
                ORDER BY $COLUMN_DATE DESC 
                LIMIT 10
            )
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        var totalPoints = 0
        if (cursor.moveToFirst()) {
            totalPoints = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return totalPoints
    }

    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password
        }
    }
}

data class User(
    val userId: Int,
    val username: String,
    val email: String
)

data class GameHistory(
    val historyId: Int,
    val userId: Int,
    val championName: String,
    val points: Int,
    val date: String
)