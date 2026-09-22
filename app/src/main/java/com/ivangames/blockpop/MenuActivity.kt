package com.ivangames.blockpop

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var themeBtn: Button
    private lateinit var prefs: android.content.SharedPreferences
    private var themeMode = "dark"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        prefs = getSharedPreferences("blockpop", Context.MODE_PRIVATE)
        themeMode = prefs.getString("themeMode", "dark") ?: "dark"

        rootLayout = findViewById(R.id.rootLayout)
        themeBtn = findViewById(R.id.themeBtn)

        val playBtn = findViewById<Button>(R.id.playBtn)
        val recordsBtn = findViewById<Button>(R.id.recordsBtn)
        val themesBtn = findViewById<Button>(R.id.themesBtn)

        applyTheme()

        themeBtn.setOnClickListener { toggleTheme() }

        playBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        recordsBtn.setOnClickListener {
            val record = prefs.getInt("record", 0)
            AlertDialog.Builder(this)
                .setTitle("🏆 Рекорд")
                .setMessage("Твой лучший счёт:\n\n$record очков")
                .setPositiveButton("OK", null)
                .show()
        }

        themesBtn.setOnClickListener {
            showThemeDialog()
        }
    }

    private fun toggleTheme() {
        themeMode = if (themeMode == "dark") "pink" else "dark"
        prefs.edit().putString("themeMode", themeMode).apply()
        applyTheme()
    }

    private fun showThemeDialog() {
        val options = arrayOf("🌙 Тёмная", "💗 Розовая")
        val current = if (themeMode == "pink") 1 else 0
        AlertDialog.Builder(this)
            .setTitle("Выбери тему")
            .setSingleChoiceItems(options, current) { dialog, which ->
                themeMode = if (which == 1) "pink" else "dark"
                prefs.edit().putString("themeMode", themeMode).apply()
                applyTheme()
                dialog.dismiss()
            }
            .show()
    }

    private fun applyTheme() {
        when (themeMode) {
            "pink" -> {
                rootLayout.setBackgroundResource(R.drawable.bg_pink)
                themeBtn.text = "💗"
            }
            else -> {
                rootLayout.setBackgroundResource(R.drawable.bg_gradient)
                themeBtn.text = "🌙"
            }
        }
    }
}
