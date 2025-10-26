package com.example.leagueguessr

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GameHistoryDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "GameHistory.db"

        const val TABLE_HISTORY = "game_history"
        const val COLUMN_ID = "_id"
        const val COLUMN_CHAMPION_NAME = "champion_name"
        const val COLUMN_POINTS = "points"
        const val COLUMN_DATE = "date"
        const val COLUMN_CORRECT_CHAMPION = "correct_champion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_HISTORY (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CHAMPION_NAME TEXT NOT NULL,
                $COLUMN_POINTS INTEGER NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_CORRECT_CHAMPION TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    fun addGameResult(championName: String, points: Int, correctChampion: String = "") {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHAMPION_NAME, championName)
            put(COLUMN_POINTS, points)
            put(COLUMN_DATE, java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
            put(COLUMN_CORRECT_CHAMPION, correctChampion)
        }
        db.insert(TABLE_HISTORY, null, values)
        db.close()
    }

    fun getLast10Games(): List<Data_history> {
        val historyList = mutableListOf<Data_history>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_HISTORY,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE DESC",
            "10"
        )

        with(cursor) {
            while (moveToNext()) {
                val championName = getString(getColumnIndexOrThrow(COLUMN_CHAMPION_NAME))
                val points = getInt(getColumnIndexOrThrow(COLUMN_POINTS))
                val date = getString(getColumnIndexOrThrow(COLUMN_DATE))

                historyList.add(
                    Data_history(
                        championImageResId = 0, // Будет установлено в Activity_Profile
                        result = "Points: $points",
                        date = date,
                        points = points,
                        championName = championName // Сохраняем имя чемпиона
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return historyList
    }

    fun getAllGames(): List<Data_history> {
        val historyList = mutableListOf<Data_history>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_HISTORY,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val championName = getString(getColumnIndexOrThrow(COLUMN_CHAMPION_NAME))
                val points = getInt(getColumnIndexOrThrow(COLUMN_POINTS))
                val date = getString(getColumnIndexOrThrow(COLUMN_DATE))

                historyList.add(
                    Data_history(
                        championImageResId = 0, // Будет установлено в Activity_Profile
                        result = "Points: $points",
                        date = date,
                        points = points,
                        championName = championName // Сохраняем имя чемпиона
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return historyList
    }

    fun getTotalPointsLast10Games(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_POINTS) FROM (SELECT $COLUMN_POINTS FROM $TABLE_HISTORY ORDER BY $COLUMN_DATE DESC LIMIT 10)",
            null
        )

        var totalPoints = 0
        if (cursor.moveToFirst()) {
            totalPoints = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return totalPoints
    }
}