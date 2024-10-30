package com.example.hmg

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
import java.text.SimpleDateFormat
import java.util.*

class RankActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankingAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)

        // Khởi tạo các view
        initializeViews()

        // Set up RecyclerView
        setupRecyclerView()

        // Set up button click listener
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
            // Lấy top 5 người chơi có streak cao nhất
            val top5Rankings = rankings.sortedByDescending { it.second }.take(5)
            adapter = RankingAdapter(top5Rankings)
            recyclerView.adapter = adapter
        } else {
            // Có thể thêm xử lý khi không có dữ liệu
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

class RankingAdapter(private val rankings: List<Triple<String, Int, String>>) :
    RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankTextView: TextView = view.findViewById(R.id.rankTextView)
        val playerNameTextView: TextView = view.findViewById(R.id.playerNameTextView)
        val streakTextView: TextView = view.findViewById(R.id.streakTextView)
        val playerImageView: ImageView = itemView.findViewById(R.id.playerImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rank_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (playerName, streak, date) = rankings[position]
        holder.rankTextView.text = "#${position + 1}"
        holder.playerNameTextView.text = playerName
        holder.streakTextView.text = "Streak: $streak"

        // Đặt màu nền của toàn bộ item dựa trên vị trí
        val backgroundColor = when (position) {
            0 -> Color.rgb(255, 215, 0) // Màu vàng cho hạng #1
            1 -> Color.rgb(192, 192, 192)// Màu bạc cho hạng #2
            2 -> 0xFFA0522D.toInt() // Màu nâu đồng cho hạng #3
            else -> Color.WHITE // Không đổi màu cho hạng #4 trở đi
        }
        holder.itemView.setBackgroundColor(backgroundColor)

        val playerImageResId = when (position) {
            0 -> R.drawable.player1_image
            1 -> R.drawable.player2_image
            2 -> R.drawable.player3_image
            3 -> R.drawable.player4_image
            else -> R.drawable.player5_image
        }
        holder.playerImageView.setImageResource(playerImageResId)
    }

    override fun getItemCount() = rankings.size
}
