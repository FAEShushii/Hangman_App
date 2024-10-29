package com.example.hmg

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        backButton = findViewById(R.id.backButton) // Đảm bảo ID này tồn tại trong activity_rank.xml
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val rankings = dbHelper.getTopHighScores()

        if (rankings.isNotEmpty()) {
            adapter = RankingAdapter(rankings)
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

        // Đặt màu nền dựa trên vị trí
        when (position) {
            0 -> holder.rankTextView.setBackgroundColor(Color.YELLOW) // Màu vàng cho hạng #1
            1 -> holder.rankTextView.setBackgroundColor(Color.LTGRAY) // Màu bạc cho hạng #2
            2 -> holder.rankTextView.setBackgroundColor(0xFFA0522D.toInt()) // Màu nâu đồng cho hạng #3
            else -> holder.rankTextView.setBackgroundColor(Color.TRANSPARENT) // Không đổi màu cho hạng #4 trở đi
        }
    }

    override fun getItemCount() = rankings.size
}