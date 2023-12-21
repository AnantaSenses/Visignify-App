package com.bangkit.capstone.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.capstone.R
import java.util.Locale

class SplashAudioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_audio)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        textToSpeech = TextToSpeech(
            this
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.setSpeechRate(1f)
                textToSpeech!!.speak(
                    "Turn on the Audio for using this app entirely",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }
        val handler = Handler()
        handler.postDelayed({
            val openMainActivity = Intent(this@SplashAudioActivity, ChooseActivity::class.java)
            startActivity(openMainActivity)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 5000)
    }

    companion object {
        private var textToSpeech: TextToSpeech? = null
    }
}