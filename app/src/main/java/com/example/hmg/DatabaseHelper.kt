package com.example.hmg

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "hangman1.db"
        private const val DATABASE_VERSION = 1

        // Table names
        private const val TABLE_THEMES = "themes"
        private const val TABLE_WORDS = "words"

        // Common column names
        private const val KEY_ID = "id"
        private const val KEY_THEME_NAME = "theme_name"
        private const val KEY_WORD = "word"
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

        db.execSQL(createThemesTable)
        db.execSQL(createWordsTable)

        // Insert initial data
        /*insertInitialData(db)*/
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_THEMES")

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
}