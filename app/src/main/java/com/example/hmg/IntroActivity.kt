package com.example.hmg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        /*val twoPlayersButton: Button = findViewById(R.id.twoPlayersButton)
        twoPlayersButton.setOnClickListener {
            val intent = Intent(this, SetWordActivity::class.java)
            startActivity(intent)
            finish() // Để không quay lại màn hình giới thiệu khi nhấn nút back
        }*/
        val rankButton : Button = findViewById(R.id.rankButton)
        rankButton.setOnClickListener{
            val intent = Intent(this, RankActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun startGame(view: android.view.View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}