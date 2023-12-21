package com.bangkit.capstone.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bangkit.capstone.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val arrowBack = findViewById<ImageView>(R.id.arrow_back)

        arrowBack.setOnClickListener{
            finish()
        }
    }
}