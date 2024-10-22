package com.example.hmg

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SetWordActivity : AppCompatActivity() {
    private lateinit var titleSetWord: TextView
    private lateinit var wordToGuess: EditText
    private lateinit var deleteButton: Button
    private lateinit var playButton: Button
    private lateinit var homeButton: Button

    private var word: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_word)

        // Khởi tạo các thành phần giao diện
        titleSetWord = findViewById(R.id.titleSetWord)
        wordToGuess = findViewById(R.id.wordToGuess)
        deleteButton = findViewById(R.id.deleteButton)
        playButton = findViewById(R.id.playButton)
        homeButton = findViewById(R.id.homeButton)

        // Thiết lập nút Play
        playButton.setOnClickListener {
            word = wordToGuess.text.toString().uppercase()
            if (word.isNullOrEmpty()) {
                // Hiển thị thông báo nếu không có từ nhập vào
                wordToGuess.setError("Please enter a word!")
            } else {
                // Chuyển sang GuessWordActivity với từ đã nhập
                val intent = Intent(this, GuessWordActivity::class.java).apply {
                    putExtra("WORD_TO_GUESS", word)
                }
                startActivity(intent)
                finish()
            }
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Thiết lập nút Delete
        deleteButton.setOnClickListener {
            val currentText = wordToGuess.text.toString()
            if (currentText.isNotEmpty()) {
                // Xóa ký tự cuối cùng
                wordToGuess.setText(currentText.substring(0, currentText.length - 1))
            }
        }
    }

    // Hàm để thêm chữ cái vào EditText
    fun touchLetter(view: View) {
        val button = view as Button
        val letter = button.text.toString()
        val currentText = wordToGuess.text.toString()

        // Cập nhật nội dung trong EditText
        wordToGuess.setText(currentText + letter)
    }
}