package com.ivangames.blockpop

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var scoreText: TextView
    private lateinit var recordText: TextView
    private lateinit var newGameBtn: Button
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("blockpop", Context.MODE_PRIVATE)

        gameView = findViewById(R.id.gameView)
        scoreText = findViewById(R.id.scoreText)
        recordText = findViewById(R.id.recordText)
        newGameBtn = findViewById(R.id.newGameBtn)

        updateRecord()

        gameView.onScoreChanged = { score ->
            scoreText.text = "Счёт: $score"
        }

        gameView.onGameOver = {
            val record = prefs.getInt("record", 0)
            if (gameView.score > record) {
                prefs.edit().putInt("record", gameView.score).apply()
            }
            updateRecord()
            showGameOverDialog()
        }

        newGameBtn.setOnClickListener {
            gameView.reset()
        }
    }

    private fun updateRecord() {
        val record = prefs.getInt("record", 0)
        recordText.text = "Рекорд: $record"
    }

    private fun showGameOverDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_gameover, null)
        val text = view.findViewById<TextView>(R.id.gameOverText)
        val btn = view.findViewById<Button>(R.id.gameOverBtn)

        text.text = "Игра окончена!\n\nСчёт: ${gameView.score}\nРекорд: ${prefs.getInt("record", 0)}"

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        btn.setOnClickListener {
            dialog.dismiss()
            gameView.reset()
        }

        dialog.show()
    }
}
