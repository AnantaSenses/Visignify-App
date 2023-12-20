package com.bangkit.capstone.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.capstone.R
import com.bangkit.capstone.model.LearnSign

class LearnSignDetailActivity : AppCompatActivity() {

    companion object{
        const val key_learn_sign = "KEY_LEARN_SIGN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_sign_detail)

        val arrowBack = findViewById<ImageView>(R.id.arrow_back_learn_detail)

        val tvDetailAlphabet = findViewById<TextView>(R.id.learn_alphabet)
        val ivDetailPhoto = findViewById<ImageView>(R.id.ivSignLaguage)

        val data = intent.getParcelableExtra<LearnSign>("DATA")
        Log.d("Detail Data", data?.alphabet.toString())

        val dataLearnSign = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra<LearnSign>(key_learn_sign, LearnSign::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<LearnSign>(key_learn_sign)
        }

        if (dataLearnSign != null){
            tvDetailAlphabet.text = dataLearnSign.alphabet
        }
        dataLearnSign?.let { ivDetailPhoto.setImageResource(it.photo) }
        print(dataLearnSign)

        arrowBack.setOnClickListener{
            finish()
        }
    }
}