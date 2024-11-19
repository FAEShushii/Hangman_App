package com.example.btl

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RankActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankingAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)
        initializeViews()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initializeViews() {
        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.rankingRecyclerView)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val rankings = dbHelper.getTopHighScores()

        if (rankings.isNotEmpty()) {
            val top10Rankings = rankings.sortedByDescending { it.second }.take(10)
            adapter = RankingAdapter(top10Rankings)
            recyclerView.adapter = adapter
        } else {
            android.widget.Toast.makeText(
                this,
                "No ranking data yet! Play the game to set your record.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

class RankingAdapter(private val rankings: List<Triple<String, Int, Long>>) :
    RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankTextView: TextView = view.findViewById(R.id.rankTextView)
        val playerNameTextView: TextView = view.findViewById(R.id.playerNameTextView)
        val streakTextView: TextView = view.findViewById(R.id.streakTextView)
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val playerImageView: ImageView = itemView.findViewById(R.id.playerImageView)
    }

    override fun getItemCount() = rankings.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rank_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (playerName, streak, time) = rankings[position]
        holder.rankTextView.text = "#${position + 1}"
        holder.playerNameTextView.text = playerName
        holder.streakTextView.text = "Streak: $streak"
        holder.timeTextView.text = "Time: ${time/1000}s"

        val backgroundColor = when (position) {
            0 -> Color.rgb(255, 215, 0)
            1 -> Color.rgb(192, 192, 192)
            2 -> 0xFFA0522D.toInt()
            else -> Color.WHITE
        }
        holder.itemView.setBackgroundColor(backgroundColor)

        val playerImageResId = when (position) {
            0 -> R.drawable.player1_image
            1 -> R.drawable.player2_image
            2 -> R.drawable.player3_image
            3 -> R.drawable.player4_image
            4 -> R.drawable.player5_image
            5 -> R.drawable.player6_image
            6 -> R.drawable.player7_image
            7 -> R.drawable.player8_image
            8 -> R.drawable.player9_image
            else -> R.drawable.player10_image
        }
        holder.playerImageView.setImageResource(playerImageResId)
    }
}