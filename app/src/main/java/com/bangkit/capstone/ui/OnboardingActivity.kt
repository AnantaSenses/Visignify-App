package com.bangkit.capstone.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bangkit.capstone.R
import java.util.Locale

class OnboardingActivity : AppCompatActivity() {

    @SuppressLint("StaticFieldLeak")
    private var mVoiceInputTv: TextView? = null
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f
    private var sfx: MediaPlayer? = null
    @SuppressLint("MissingInflatedId")
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

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        if (checkIfAlreadyhavePermission()) {
            Toast.makeText(applicationContext, "Permission is granted", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this@OnboardingActivity, arrayOf(Manifest.permission.READ_SMS),
                1
            )
        }
        ChooseActivity.textToSpeech = TextToSpeech(
            this
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                ChooseActivity.textToSpeech!!.language = Locale.US
                ChooseActivity.textToSpeech!!.setSpeechRate(1f)
                if (ChooseActivity.firstTime == 0) ChooseActivity.textToSpeech!!.speak(
                    "Swipe right to use the blind features. And swipe left and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                //when user return from another activities to main activities.
                if (ChooseActivity.firstTime != 0) ChooseActivity.textToSpeech!!.speak(
                    "you are in main menu. just swipe left and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }
        mVoiceInputTv = findViewById<View>(R.id.voice_input) as TextView

    }

    private fun checkIfAlreadyhavePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        firstTime = 1
        sfx = MediaPlayer.create(this, R.raw.swipe)
        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = touchEvent.x
                y1 = touchEvent.y
            }

            MotionEvent.ACTION_UP -> {
                x2 = touchEvent.x
                y2 = touchEvent.y
                if (x1 < x2) {
                    firstTime = 1
                    sfx!!.start()
                    val intent = Intent(this@OnboardingActivity, SplashAudioActivity::class.java)
                    startActivity(intent)
                    return true // Exit the function early
                }
                if (x1 > x2) {
                    firstTime = 1
                    sfx!!.start()
                    startVoiceInput()
                    return true // Exit the function early
                }
            }
        }
        return false
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?")
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            a.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                mVoiceInputTv!!.text = result!![0]
                if (mVoiceInputTv!!.text.toString().contains("exit")) {
                    textToSpeech!!.speak("Closing the App", TextToSpeech.QUEUE_FLUSH, null)
                    val timer = Handler()
                    timer.postDelayed({
                        finishAffinity()
                        System.exit(0)
                    }, 2000)
                }  else if (mVoiceInputTv!!.text.toString().contains("blind")) {
                    val intent = Intent(applicationContext, ChooseActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("deaf")) {
                    val intent = Intent(applicationContext, HomeDeafActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("exit")) {
                    textToSpeech!!.speak("Closing the App", TextToSpeech.QUEUE_FLUSH, null)
                    mVoiceInputTv!!.setText(null)
                    val timer = Handler()
                    timer.postDelayed({ finishAffinity() }, 5000)
                } else {
                    textToSpeech!!.speak(
                        "Do not understand. Swipe left Say again",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
    }

    companion object {
        private const val REQ_CODE_SPEECH_INPUT = 100
        var firstTime = 0
        var textToSpeech: TextToSpeech? = null
        var name: String? = null
    }
}