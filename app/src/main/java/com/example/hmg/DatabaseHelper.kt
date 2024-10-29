package com.example.hmg

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "hg1.db"
        private const val DATABASE_VERSION = 1

        // Table names
        private const val TABLE_THEMES = "themes"
        private const val TABLE_WORDS = "words"
        private const val TABLE_HIGH_SCORES = "high_scores"

        // Common column names
        private const val KEY_ID = "id"
        private const val KEY_THEME_NAME = "theme_name"
        private const val KEY_WORD = "word"
        private const val KEY_PLAYER_NAME = "player_name" // Thêm cột mới
        private const val KEY_STREAK = "streak" // Thêm cột mới
        private const val KEY_DATE = "date" // Thêm cột mới
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create themes table
        val createThemesTable = """
            CREATE TABLE $TABLE_THEMES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_THEME_NAME TEXT UNIQUE
            )
        """.trimIndent()

        // Create words table
        val createWordsTable = """
            CREATE TABLE $TABLE_WORDS (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_THEME_NAME TEXT,
                $KEY_WORD TEXT,
                FOREIGN KEY($KEY_THEME_NAME) REFERENCES $TABLE_THEMES($KEY_THEME_NAME)
            )
        """.trimIndent()

        val createHighScoresTable = """
            CREATE TABLE $TABLE_HIGH_SCORES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_PLAYER_NAME TEXT,
                $KEY_STREAK INTEGER,
                $KEY_DATE TEXT
            )
        """.trimIndent()

        db.execSQL(createThemesTable)
        db.execSQL(createWordsTable)
        db.execSQL(createHighScoresTable)

        // Insert initial data
        /*insertInitialData(db)*/
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_THEMES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HIGH_SCORES")

        // Create tables again
        onCreate(db)
    }

    /*private fun insertInitialData(db: SQLiteDatabase) {
        val themes = mapOf(
            "Fruits" to arrayOf("APPLE", "MANGO", "PEAR", "GRAPE", "KIWI"),
            "Animals" to arrayOf("DOG", "CAT", "BIRD", "FISH", "LION"),
            "Countries" to arrayOf("INDIA", "CHILE", "PERU", "ITALY", "SPAIN"),
            "Sports" to arrayOf("SOCCER", "RUGBY", "SKI", "GOLF", "SURF"),
            "Colors" to arrayOf("RED", "BLUE", "GREEN", "BLACK", "WHITE"),
            "Professions" to arrayOf("CHEF", "NURSE", "LAWYER", "GUARD", "PILOT"),
            "Vehicles" to arrayOf("BUS", "BOAT", "TRUCK", "TRAIN", "CYCLE"),
            "Weather" to arrayOf("SUN", "COLD", "WIND", "FOG", "HAIL"),
            "Emotions" to arrayOf("SAD", "CALM", "FEAR", "HOPE", "LOVE"),
            "Body" to arrayOf("ARM", "LEG", "LUNG", "FOOT", "KNEE"),
            "Music" to arrayOf("DRUM", "HARP", "FLUTE", "HORN", "LYRE"),
            "Food" to arrayOf("CAKE", "RICE", "BEEF", "BEAN", "EGGS"),
            "Clothing" to arrayOf("HAT", "BELT", "SKIRT", "COAT", "SOCK"),
            "Plants" to arrayOf("TREE", "MOSS", "BUSH", "WEED", "VINE"),
            "Space" to arrayOf("MOON", "SUN", "MARS", "ORBIT", "STAR"),
            "Technology" to arrayOf("PHONE", "LAPTOP", "TABLET", "DRONE", "RADIO"),
            "Literature" to arrayOf("BOOK", "POEM", "PLAY", "ESSAY", "TALE"),
            "Ocean" to arrayOf("REEF", "TIDE", "KELP", "FISH", "SHARK"),
            "Art" to arrayOf("INK", "CLAY", "CRAFT", "SKETCH", "COLOR"),
            "Time" to arrayOf("HOUR", "DAY", "WEEK", "MONTH", "YEAR")
        )


        themes.forEach { (theme, words) ->
            // Insert theme
            val themeValues = ContentValues().apply {
                put(KEY_THEME_NAME, theme)
            }
            db.insert(TABLE_THEMES, null, themeValues)

            // Insert words for this theme
            words.forEach { word ->
                val wordValues = ContentValues().apply {
                    put(KEY_THEME_NAME, theme)
                    put(KEY_WORD, word)
                }
                db.insert(TABLE_WORDS, null, wordValues)
            }
        }
    }*/

    fun getRandomThemeAndWord(): Pair<String, String> {
        val db = this.readableDatabase

        // Get random theme
        val themeQuery = "SELECT $KEY_THEME_NAME FROM $TABLE_THEMES ORDER BY RANDOM() LIMIT 1"
        val themeCursor = db.rawQuery(themeQuery, null)

        themeCursor.moveToFirst()
        val theme = themeCursor.getString(themeCursor.getColumnIndexOrThrow(KEY_THEME_NAME))

        // Get random word for this theme
        val wordQuery = "SELECT $KEY_WORD FROM $TABLE_WORDS WHERE $KEY_THEME_NAME = ? ORDER BY RANDOM() LIMIT 1"
        val wordCursor = db.rawQuery(wordQuery, arrayOf(theme))

        wordCursor.moveToFirst()
        val word = wordCursor.getString(wordCursor.getColumnIndexOrThrow(KEY_WORD))

        themeCursor.close()
        wordCursor.close()

        return Pair(theme, word)
    }

    // Thêm function để lưu high score
    fun saveHighScore(playerName: String, streak: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_PLAYER_NAME, playerName)
            put(KEY_STREAK, streak)
            put(KEY_DATE, System.currentTimeMillis().toString())
        }
        db.insert(TABLE_HIGH_SCORES, null, values)
        db.close()
    }

    // Thêm function để lấy top high scores
    fun getTopHighScores(limit: Int = 10): List<Triple<String, Int, String>> {
        val scores = mutableListOf<Triple<String, Int, String>>()
        val db = this.readableDatabase

        try {
            val cursor = db.query(
                TABLE_HIGH_SCORES,
                arrayOf(KEY_PLAYER_NAME, KEY_STREAK, KEY_DATE),
                null,
                null,
                null,
                null,
                "$KEY_STREAK DESC",
                limit.toString()
            )

            if (cursor?.count ?: 0 > 0) {
                cursor.moveToFirst()
                do {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PLAYER_NAME))
                    val streak = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STREAK))
                    val date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                    scores.add(Triple(name, streak, date))
                } while (cursor.moveToNext())
            }
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return scores
    }
}