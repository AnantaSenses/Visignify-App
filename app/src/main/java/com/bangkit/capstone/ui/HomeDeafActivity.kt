package com.bangkit.capstone.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.capstone.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeDeafActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_deaf)
        val buttonNavbar = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        buttonNavbar.background = null

        val signDetectionCard = findViewById<LinearLayout>(R.id.sign_detection)
        val weatherInformationCard = findViewById<LinearLayout>(R.id.weather_information)
        val deafNoteCard = findViewById<LinearLayout>(R.id.deaf_note)
        val learnSignCard = findViewById<LinearLayout>(R.id.learn_sign)

        signDetectionCard.setOnClickListener {
            val intentBlind = Intent(this, SignDetectionActivity::class.java)
            startActivity(intentBlind)
        }
        weatherInformationCard.setOnClickListener{
            val intentDeaf = Intent(this, WeatherActivity::class.java)
            startActivity(intentDeaf)
        }
        deafNoteCard.setOnClickListener{
            val intentDeaf = Intent(this, DeafNoteActivity::class.java)
            startActivity(intentDeaf)
        }
        learnSignCard.setOnClickListener{
            val intentDeaf = Intent(this, LearnSignActivity::class.java)
            startActivity(intentDeaf)
        }
    }
}