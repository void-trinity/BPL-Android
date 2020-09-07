package com.anotherdimension.bpl

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        transitToOther()
    }

    private fun transitToOther() {
        val sharedPreference = getSharedPreferences("BPL", Context.MODE_PRIVATE)
        var intent: Intent? = null

        val token: String? = sharedPreference.getString("token", "NA")

        intent = if (token == "NA") {
            Intent(applicationContext, LoginActivity::class.java)
        } else {
            Intent(applicationContext, MainActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}