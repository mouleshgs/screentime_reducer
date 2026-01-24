package com.example.screentimereducer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import android.content.Intent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)

        // Hardcoded credentials
        val correctUsername = "admin"
        val correctPassword = "1234"

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username == correctUsername && password == correctPassword) {
                // Move to next screen
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
            } else {
                // Stay on same page
                tvMessage.text = "Try again"
            }
        }
    }
}
