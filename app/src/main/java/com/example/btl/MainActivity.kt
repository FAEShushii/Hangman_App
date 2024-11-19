package com.example.btl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer

class MainActivity : AppCompatActivity() {
    var wordSearch: String? = null
    lateinit var answers: CharArray
    var errors = 0
    var streak = 0
    private lateinit var dbHelper: DatabaseHelper
    private val letters = ArrayList<String>()
    private var image: ImageView? = null
    private var wordTV: TextView? = null
    private var searchTV: TextView? = null
    private var streakTV: TextView? = null
    private var themeTV: TextView? = null
    private var lastGameWon = false
    private val usedWords = mutableSetOf<String>()
    private val recentThemes = ArrayDeque<String>()
    private lateinit var playAgainButton: Button
    private lateinit var backButton: Button
    private var currentStreak = 0
    private var gameOver = false
    private var startTime: Long = 0
    private var totalPlayTime: Long = 0
    private lateinit var hintButton1: Button
    private lateinit var hintButton2: Button
    private lateinit var hintButton3: Button
    private var consecutiveCorrectAnswers = 0
    private var roundsSinceLastHintUse = 0
    private var hasUsedAnyHint = false
    private var lastSelectedHint: Int = 0
    private var allHintsEnabled = false
    private var isHint1Available = false
    private var isHint2Available = false
    private var isHint3Available = false
    private lateinit var backgroundMusic: MediaPlayer
    private lateinit var correctSound: MediaPlayer
    private lateinit var wrongSound: MediaPlayer
    private lateinit var musicToggleButton: ImageButton
    private var isMusicPlaying = false
    private var isSoundEnabled = true

    private fun initializeAudio() {
        backgroundMusic = MediaPlayer.create(this, R.raw.background_music)
        backgroundMusic.isLooping = true
        backgroundMusic.setVolume(0.5f, 0.5f)
        correctSound = MediaPlayer.create(this, R.raw.correct_sound)
        wrongSound = MediaPlayer.create(this, R.raw.wrong_sound)
    }

    private fun startBackgroundMusic() {
        if (!isMusicPlaying) {
            backgroundMusic.start()
            isMusicPlaying = true
            isSoundEnabled = true
            musicToggleButton.setImageResource(R.drawable.ic_music_on)
        }
    }

    private fun stopBackgroundMusic() {
        if (isMusicPlaying) {
            backgroundMusic.pause()
            backgroundMusic.seekTo(0)
            isMusicPlaying = false
            isSoundEnabled = false
            musicToggleButton.setImageResource(R.drawable.ic_music_off)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = DatabaseHelper(this)
        image = findViewById(R.id.img)
        wordTV = findViewById(R.id.wordTV)
        searchTV = findViewById(R.id.searchTV)
        streakTV = findViewById(R.id.streakTV)
        themeTV = findViewById(R.id.themeTV)
        playAgainButton = findViewById(R.id.playAgainButton)
        backButton = findViewById(R.id.backButton)
        hintButton1 = findViewById(R.id.hintButton1)
        hintButton2 = findViewById(R.id.hintButton2)
        hintButton3 = findViewById(R.id.hintButton3)
        musicToggleButton = findViewById(R.id.musicToggleButton)
        setupMusicToggleButton()
        newGame()
        setupHintButtons()

        backButton.setOnClickListener {
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            finish()
        }

        initializeAudio()
        startBackgroundMusic()
    }

    private fun setupMusicToggleButton() {
        musicToggleButton.setImageResource(if (isMusicPlaying) R.drawable.ic_music_on else R.drawable.ic_music_off)

        musicToggleButton.setOnClickListener {
            if (isMusicPlaying) {
                stopBackgroundMusic()
            } else {
                startBackgroundMusic()
            }
        }
    }

    private fun playCorrectSound() {
        if (isSoundEnabled) {
            correctSound.seekTo(0)
            correctSound.start()
        }
    }

    private fun playWrongSound() {
        if (isSoundEnabled) {
            wrongSound.seekTo(0)
            wrongSound.start()
        }
    }

    private fun showHintSelectionDialog() {
        if (isHint1Available && isHint2Available && isHint3Available) {
            allHintsEnabled = true
            return
        }

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_hint_selection, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.hintRadioGroup)
        val timerText = dialogView.findViewById<TextView>(R.id.timerText)

        dialogView.findViewById<RadioButton>(R.id.radioHint1).isEnabled =
            !isHint1Available && lastSelectedHint != 1
        dialogView.findViewById<RadioButton>(R.id.radioHint2).isEnabled =
            !isHint2Available && lastSelectedHint != 2
        dialogView.findViewById<RadioButton>(R.id.radioHint3).isEnabled =
            !isHint3Available && lastSelectedHint != 3

        val alertDialog = dialogBuilder.setView(dialogView)
            .setTitle("Reward Selection")
            .setCancelable(false)
            .setPositiveButton("Select", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                randomlyEnableHint()
            }
            .create()

        var secondsLeft = 10
        val timer = object : CountDownTimer(10000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft = (millisUntilFinished / 1000).toInt()
                timerText.text = "Time remaining: ${secondsLeft}s"
            }

            override fun onFinish() {
                alertDialog.dismiss()
                randomlyEnableHint()
            }
        }

        alertDialog.setOnShowListener {
            timer.start()
            val selectButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            selectButton.setOnClickListener {
                timer.cancel()
                val selectedId = radioGroup.checkedRadioButtonId
                when (selectedId) {
                    R.id.radioHint1 -> {
                        enableSpecificHint(hintButton1)
                        lastSelectedHint = 1
                    }
                    R.id.radioHint2 -> {
                        enableSpecificHint(hintButton2)
                        lastSelectedHint = 2
                    }
                    R.id.radioHint3 -> {
                        enableSpecificHint(hintButton3)
                        lastSelectedHint = 3
                    }
                    else -> randomlyEnableHint()
                }
                alertDialog.dismiss()
            }
        }

        alertDialog.setOnDismissListener {
            timer.cancel()
        }

        alertDialog.show()
    }

    private fun enableSpecificHint(button: Button) {
        button.isEnabled = true
        button.alpha = 1.0f

        when (button) {
            hintButton1 -> isHint1Available = true
            hintButton2 -> isHint2Available = true
            hintButton3 -> isHint3Available = true
        }

        Toast.makeText(this, "Hint enabled!", Toast.LENGTH_SHORT).show()
    }

    private fun resetAllHints() {
        isHint1Available = false
        isHint2Available = false
        isHint3Available = false
        hintButton1.isEnabled = false
        hintButton2.isEnabled = false
        hintButton3.isEnabled = false
        hintButton1.alpha = 0.5f
        hintButton2.alpha = 0.5f
        hintButton3.alpha = 0.5f
        lastSelectedHint = 0
        allHintsEnabled = false
        consecutiveCorrectAnswers = 0
        hasUsedAnyHint = false
        roundsSinceLastHintUse = 0
    }

    private fun randomlyEnableHint() {
        val disabledHints = mutableListOf<Button>()

        if (!hintButton1.isEnabled) disabledHints.add(hintButton1)
        if (!hintButton2.isEnabled) disabledHints.add(hintButton2)
        if (!hintButton3.isEnabled) disabledHints.add(hintButton3)

        if (disabledHints.isNotEmpty()) {
            val randomHint = disabledHints.random()
            enableSpecificHint(randomHint)
        }
    }

    private fun updateHint1Availability() {
        if (isHint1Available) {
            val hiddenIndices = answers.indices.filter { answers[it] == '_' }
            if (hiddenIndices.size == 1) {
                hintButton1.isEnabled = false
                hintButton1.alpha = 0.5f
                return
            }
            if (hiddenIndices.size == 2) {
                val firstLetter = wordSearch!![hiddenIndices[0]]
                val secondLetter = wordSearch!![hiddenIndices[1]]
                if (firstLetter == secondLetter) {
                    hintButton1.isEnabled = false
                    hintButton1.alpha = 0.5f
                    return
                }
            }

            hintButton1.isEnabled = true
            hintButton1.alpha = 1.0f
        } else {
            hintButton1.isEnabled = false
            hintButton1.alpha = 0.5f
        }
    }

    private fun updateHint3Availability() {
        if (isHint3Available) {
            if (errors >= 1) {
                hintButton3.isEnabled = true
                hintButton3.alpha = 1.0f
            } else {
                hintButton3.isEnabled = false
                hintButton3.alpha = 0.5f
            }
        } else {
            hintButton3.isEnabled = false
            hintButton3.alpha = 0.5f
        }
    }

    private fun setupHintButtons() {
        updateHint3Availability()
        updateHint1Availability()

        hintButton1.setOnClickListener {
            if (!gameOver && hintButton1.isEnabled && isHint1Available) {
                val hiddenIndices = answers.indices.filter { answers[it] == '_' }

                if (hiddenIndices.isNotEmpty()) {
                    hasUsedAnyHint = true
                    roundsSinceLastHintUse = 0
                    Toast.makeText(this, "Letter revealed!", Toast.LENGTH_SHORT).show()
                    val randomIndex = hiddenIndices.random()
                    val letter = wordSearch!![randomIndex]

                    wordSearch!!.indices.forEach { index ->
                        if (wordSearch!![index] == letter) {
                            answers[index] = letter
                        }
                    }

                    wordTV?.text = stateWord()

                    val buttonId = resources.getIdentifier("button$letter", "id", packageName)
                    val button = findViewById<Button>(buttonId)
                    button.isEnabled = false
                    button.alpha = 0.5f

                    hintButton1.isEnabled = false
                    hintButton1.alpha = 0.5f
                    isHint1Available = false
                }
            }
        }

        hintButton2.setOnClickListener {
            if(!gameOver && hintButton2.isEnabled && isHint2Available) {
                hasUsedAnyHint = true
                roundsSinceLastHintUse = 0
                Toast.makeText(this, "5 wrong letters removed!", Toast.LENGTH_SHORT).show()
                val unusedLetters = ('A'..'Z').filter { !wordSearch!!.contains(it) }
                val randomLetters = unusedLetters.shuffled().take(5)

                randomLetters.forEach { letter ->
                    val buttonId = resources.getIdentifier("button$letter", "id", packageName)
                    val button = findViewById<Button>(buttonId)
                    button.isEnabled = false
                    button.alpha = 0.1f
                }

                hintButton2.isEnabled = false
                hintButton2.alpha = 0.5f
                isHint2Available = false
            }
        }

        hintButton3.setOnClickListener {
            if (!gameOver && hintButton3.isEnabled && isHint3Available) {
                if (errors >= 1) {
                    hasUsedAnyHint = true
                    roundsSinceLastHintUse = 0
                    Toast.makeText(this, "Extra life gained!", Toast.LENGTH_SHORT).show()
                    errors--
                    updateImage(errors)
                    hintButton3.isEnabled = false
                    hintButton3.alpha = 0.5f
                    isHint3Available = false
                } else {
                    Toast.makeText(this, "Can't use this hint yet!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun chooseWord(): Pair<String, String> {
        var (theme, word) = dbHelper.getRandomThemeAndWord()

        while (usedWords.contains(word) || recentThemes.contains(theme)) {
            val newPair = dbHelper.getRandomThemeAndWord()
            theme = newPair.first
            word = newPair.second
        }

        recentThemes.add(theme)

        if (recentThemes.size > 3) {
            recentThemes.removeFirst()
        }

        usedWords.add(word)

        return Pair(theme, word)
    }

    private fun updateImage(state: Int) {
        val resource = resources.getIdentifier("state$state", "drawable", packageName)
        image!!.setImageResource(resource)
    }

    @SuppressLint("SetTextI18n")
    fun newGame() {
        errors = 0
        letters.clear()

        if (hasUsedAnyHint) {
            roundsSinceLastHintUse++
            if (roundsSinceLastHintUse == 2) {
                showHintSelectionDialog()
                roundsSinceLastHintUse = 0
            }
        }

        if (usedWords.size == 0) {
            totalPlayTime = 0
            resetAllHints()
            hasUsedAnyHint = false
            roundsSinceLastHintUse = 0
        }

        when (lastSelectedHint) {
            1 -> {
                hintButton1.isEnabled = true
                isHint1Available = true
            }
            2 -> {
                hintButton2.isEnabled = true
                isHint2Available = true
            }
            3 -> {
                hintButton3.isEnabled = true
                isHint3Available = true
                updateHint3Availability()
            }
        }
        hintButton1.alpha = if (hintButton1.isEnabled) 1.0f else 0.5f
        hintButton2.alpha = if (hintButton2.isEnabled) 1.0f else 0.5f
        hintButton3.alpha = if (hintButton3.isEnabled) 1.0f else 0.5f
        usedWords.clear()
        val (theme, word) = chooseWord()
        wordSearch = word
        answers = CharArray(wordSearch!!.length) { '_' }
        updateImage(errors)
        wordTV!!.text = stateWord()
        themeTV!!.text = theme
        searchTV!!.text = ""

        if (!lastGameWon) {
            streak = 0
            resetAllHints()
            totalPlayTime = 0
        }else{
            updateHintButtonStates()
        }
        streakTV!!.text = "Streak: $streak"

        lastGameWon = false
        gameOver = false
        startTime = SystemClock.elapsedRealtime()

        for (i in 0 until 26) {
            val buttonId = resources.getIdentifier("button${('A' + i)}", "id", packageName)
            val button = findViewById<Button>(buttonId)
            button.isEnabled = true
            button.alpha = 1.0f
            button.visibility = View.VISIBLE
        }

        updateHint1Availability()
        updateHint3Availability()
        playAgainButton.visibility = View.GONE
    }

    private fun updateHintButtonStates() {
        if (isHint1Available) {
            updateHint1Availability()
        }

        hintButton2.isEnabled = isHint2Available
        hintButton2.alpha = if (isHint2Available) 1.0f else 0.5f

        if (isHint3Available) {
            updateHint3Availability()
        }
    }

    private fun readLetter(c: String) {
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

    private fun showHighScoreDialog(finalStreak: Int, timePlayed: Long) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_high_score, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)

        val alertDialog = dialogBuilder.setView(dialogView)
            .setTitle("Congratulations!")
            .setMessage("You achieved a streak of $finalStreak! Enter your name:")
            .setCancelable(false)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()

        alertDialog.setOnShowListener {
            val saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val playerName = editTextName.text.toString()
                val hasUpperCase = playerName.any { it.isUpperCase() }

                when {
                    playerName.isEmpty() -> {
                        Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                    }
                    playerName.length > 20 -> {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                    }
                    !hasUpperCase -> {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        dbHelper.saveHighScore(playerName, finalStreak, timePlayed)
                        Toast.makeText(this, "Successfully saved!", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }
                }
            }
        }

        alertDialog.show()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    fun touchLetter(v: View) {
        val letter = (v as Button).text.toString()
        readLetter(letter)
        wordTV!!.text = stateWord()
        updateImage(errors)
        updateHint3Availability()
        updateHint1Availability()

        v.isEnabled = false
        v.alpha = 0.1f

        if (wordSearch.contentEquals(String(answers))) {
            playCorrectSound()
            consecutiveCorrectAnswers++
            if (consecutiveCorrectAnswers == 2 && !allHintsEnabled) {
                showHintSelectionDialog()
                consecutiveCorrectAnswers = 0
            }

            val endTime = SystemClock.elapsedRealtime()
            val roundTime = endTime - startTime
            totalPlayTime += roundTime

            streak++
            currentStreak = streak
            val winMessage = when {
                streak >= 8 -> "INCREDIBLE"
                streak >= 6 -> "MASTER"
                streak >= 2 -> "AMAZING"
                else -> "YOU WIN!"
            }
            searchTV!!.text = winMessage
            streakTV!!.text = "Streak: $streak"
            lastGameWon = true
            wordTV!!.text = String(answers)
            playAgainButton.text = "NEXT ROUND"
            playAgainButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_next, 0)
            playAgainButton.backgroundTintList = getColorStateList(R.color.green)
            playAgainButton.visibility = View.VISIBLE
            hideLetterButtons()
            gameOver = true
        } else if (errors >= 6) {
            playWrongSound()
            val endTime = SystemClock.elapsedRealtime()
            val roundTime = endTime - startTime
            totalPlayTime += roundTime
            if (streak >= 2) {
                showHighScoreDialog(currentStreak, totalPlayTime)
            }
            updateImage(6)
            wordTV!!.text = wordSearch
            streak = 0
            streakTV!!.text = "Streak: $streak"
            lastGameWon = false
            gameOver = true
            searchTV!!.text = "You lose !!!"
            playAgainButton.text = "Play Again"
            playAgainButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_play_again, 0)
            playAgainButton.backgroundTintList = getColorStateList(android.R.color.holo_red_light)
            playAgainButton.visibility = View.VISIBLE
            hideLetterButtons()
        }
    }

    private fun hideLetterButtons() {
        for (i in 0 until 26) {
            val buttonId = resources.getIdentifier("button${('A' + i)}", "id", packageName)
            val button = findViewById<Button>(buttonId)
            button.visibility = View.GONE
        }
    }

    fun start(view: View?) {
        newGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundMusic.release()
        correctSound.release()
        wrongSound.release()
    }

}