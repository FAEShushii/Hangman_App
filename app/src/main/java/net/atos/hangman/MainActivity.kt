package net.atos.hangman

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class MainActivity : AppCompatActivity() {
    var wordSearch: String? = null
    lateinit var answers: CharArray
    var errors = 0
    var streak = 0
    private val letters = ArrayList<String>()
    private var image: ImageView? = null
    private var wordTV: TextView? = null
    private var searchTV: TextView? = null
    private var streakTV: TextView? = null
    private var themeTV: TextView? = null
    private var lastGameWon = false
    private var gameOver = false  // New variable to track game state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image = findViewById(R.id.img)
        wordTV = findViewById(R.id.wordTV)
        searchTV = findViewById(R.id.searchTV)
        streakTV = findViewById(R.id.streakTV)
        themeTV = findViewById(R.id.themeTV)
        newGame()
    }

    private fun chooseWord(): Pair<String, String> {
        val theme = themes.keys.toList()[random.nextInt(themes.size)]
        val word = themes[theme]!![random.nextInt(3)]
        return Pair(theme, word)
    }

    private fun updateImage(state: Int) {
        val resource = resources.getIdentifier("state$state", "drawable", packageName)
        image!!.setImageResource(resource)
    }

    fun newGame() {
        errors = 0  // Reset errors to 0 instead of -1
        letters.clear()
        val (theme, word) = chooseWord()
        wordSearch = word
        answers = CharArray(wordSearch!!.length) { '_' }
        updateImage(errors)
        wordTV!!.text = stateWord()
        themeTV!!.text = "$theme"
        searchTV!!.text = ""  // Clear the "YOU WIN" message

        if (!lastGameWon) {
            streak = 0
        }
        streakTV!!.text = "Streak: $streak"

        lastGameWon = false
        gameOver = false  // Reset game state
    }

    private fun readLetter(c: String) {
        if (!letters.contains(c)) {
            if (wordSearch!!.contains(c)) {
                var index = wordSearch!!.indexOf(c)
                while (index >= 0) {
                    answers[index] = c[0]
                    index = wordSearch!!.indexOf(c, index + 1)
                }
            } else {
                errors++
            }
            letters.add(c)
        } else {
            Toast.makeText(this, "Letter already entered", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stateWord(): String {
        val builder = StringBuilder()
        for (i in answers.indices) {
            builder.append(answers[i])
            if (i < answers.size - 1) {
                builder.append(" ")
            }
        }
        return builder.toString()
    }

    fun touchLetter(v: View) {
        if (!gameOver) {  // Check if the game is not over
            val letter = (v as Button).text.toString()
            readLetter(letter)
            wordTV!!.text = stateWord()
            updateImage(errors)
            if (wordSearch.contentEquals(String(answers))) {
                Toast.makeText(this, "YOU WIN!", Toast.LENGTH_SHORT).show()
                searchTV!!.text = "YOU WIN! " + searchTV!!.text
                streak++
                streakTV!!.text = "Streak: $streak"
                lastGameWon = true
                gameOver = true  // Set game state to over
            } else if (errors >= 6) {
                updateImage(7)
                Toast.makeText(this, "You lose !!!", Toast.LENGTH_SHORT).show()
                wordTV!!.text = wordSearch
                streak = 0
                streakTV!!.text = "Streak: $streak"
                lastGameWon = false
                gameOver = true  // Set game state to over
            }
        } else {
            Toast.makeText(this, "Game over.", Toast.LENGTH_SHORT).show()
        }
    }

    fun start(view: View?) {
        newGame()
    }

    fun goToIntro(view: android.view.View) {
        val intent = Intent(this, IntroActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        val themes = mapOf(
            "Fruits" to arrayOf("APPLE", "BANANA", "ORANGE"),
            "Animals" to arrayOf("ELEPHANT", "GIRAFFE", "PENGUIN"),
            "Countries" to arrayOf("FRANCE", "BRAZIL", "JAPAN"),
            "Sports" to arrayOf("FOOTBALL", "TENNIS", "SWIMMING"),
            "Colors" to arrayOf("PURPLE", "YELLOW", "INDIGO"),
            "Professions" to arrayOf("DOCTOR", "TEACHER", "ENGINEER"),
            "Vehicles" to arrayOf("CAR", "AIRPLANE", "BICYCLE"),
            "Weather" to arrayOf("RAIN", "SNOW", "THUNDER"),
            "Emotions" to arrayOf("HAPPY", "ANGRY", "EXCITED"),
            "Body" to arrayOf("HAND", "EYE", "HEART"),
            "Music" to arrayOf("GUITAR", "PIANO", "VIOLIN"),
            "Food" to arrayOf("PIZZA", "SUSHI", "BURGER"),
            "Clothing" to arrayOf("SHIRT", "PANTS", "SHOES"),
            "Plants" to arrayOf("ROSE", "CACTUS", "FERN"),
            "Space" to arrayOf("PLANET", "STAR", "COMET"),
            "Technology" to arrayOf("COMPUTER", "SMARTPHONE", "ROBOT"),
            "Literature" to arrayOf("NOVEL", "POETRY", "DRAMA"),
            "Ocean" to arrayOf("WHALE", "CORAL", "WAVE"),
            "Art" to arrayOf("PAINTING", "SCULPTURE", "DANCE"),
            "Time" to arrayOf("MINUTE", "CENTURY", "MOMENT")
        )
        val random = Random()
    }
}