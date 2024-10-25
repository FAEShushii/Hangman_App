package com.example.hmg

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GuessWordActivity : AppCompatActivity() {
    private var wordToGuess: String? = null
    private lateinit var answers: CharArray
    private var errors = 0
    private var letters = ArrayList<String>()
    private var image: ImageView? = null
    private var wordTV: TextView? = null
    private lateinit var winMessage: TextView
    private var gameOver = false
    private lateinit var homeButton: Button
    private lateinit var playAgainButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess_word)

        image = findViewById(R.id.img)
        wordTV = findViewById(R.id.wordToGuess)
        winMessage = findViewById(R.id.winMessage)

        // Nhận từ từ Intent
        wordToGuess = intent.getStringExtra("WORD_TO_GUESS")
        answers = CharArray(wordToGuess!!.length) { '_' }
        updateImage(errors)
        wordTV!!.text = stateWord()

        // Khởi tạo các nút
        homeButton = findViewById(R.id.homeButton)
        playAgainButton = findViewById(R.id.playAgainButton)
        playAgainButton.visibility = View.GONE

        // Thiết lập sự kiện cho nút Home
        homeButton.setOnClickListener {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }

        // Thiết lập sự kiện cho nút Play Again
        playAgainButton.setOnClickListener {
            startActivity(Intent(this, SetWordActivity::class.java))
            finish()
        }
    }

    private fun updateImage(state: Int) {
        val resource = resources.getIdentifier("state$state", "drawable", packageName)
        image!!.setImageResource(resource)
    }

    private fun readLetter(c: String) {
        if (!letters.contains(c)) {
            if (wordToGuess!!.contains(c)) {
                var index = wordToGuess!!.indexOf(c)
                while (index >= 0) {
                    answers[index] = c[0]
                    index = wordToGuess!!.indexOf(c, index + 1)
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
        return answers.joinToString(" ")
    }

    fun touchLetter(v: View) {
        if (!gameOver) {
            val letter = (v as Button).text.toString()
            readLetter(letter)
            wordTV!!.text = stateWord()
            updateImage(errors)
            v.isEnabled = false
            v.alpha = 0.5f

            if (wordToGuess.contentEquals(String(answers))) {
                gameOver = true
                winMessage.text = "YOU WIN!"
                wordTV!!.text = String(answers)
                playAgainButton.visibility = View.VISIBLE
            } else if (errors >= 6) {
                updateImage(7)
                Toast.makeText(this, "You lose !!!", Toast.LENGTH_SHORT).show()
                wordTV!!.text = wordToGuess
                gameOver = true
                playAgainButton.visibility = View.VISIBLE
            }
        } else {
            Toast.makeText(this, "Game over.", Toast.LENGTH_SHORT).show()
        }
    }
}