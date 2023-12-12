package com.bangkit.capstone.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.bangkit.capstone.R

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val buttonBlind = findViewById<Button>(R.id.btn_blind)
        val buttonDeaf = findViewById<Button>(R.id.btn_deaf)

        buttonBlind.setOnClickListener {
            val intentBlind = Intent(this, SplashAudioActivity::class.java)
            startActivity(intentBlind)
        }
        buttonDeaf.setOnClickListener{
            val intentDeaf = Intent(this, HomeDeafActivity::class.java)
            startActivity(intentDeaf)
        }

    }
}