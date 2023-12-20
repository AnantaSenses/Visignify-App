package com.bangkit.capstone.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.capstone.R
import java.util.Locale

class HomeBlindActivity : AppCompatActivity() {
    private lateinit var mVoiceInputTv: TextView
    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f
    private lateinit var sfx: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_blind)
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
                    "Our Visignify App comes with some unique feature. You can use it by saying Object. Reading. Calculator. Weather. Battery. Location. and Exit. Swipe left and say what you want ",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }
        mVoiceInputTv = findViewById<View>(R.id.voiceInput) as TextView
    }

    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        sfx = MediaPlayer.create(this, R.raw.swipe)
        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = touchEvent.x
                y1 = touchEvent.y
            }

            MotionEvent.ACTION_UP -> {
                x2 = touchEvent.x
                y2 = touchEvent.y
                if (x1 > x2) {
                    sfx.start()
                    textToSpeech!!.stop()
                    startVoiceInput()
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
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?")
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
                if (mVoiceInputTv!!.text.toString().contains("read")) {
                    val intent = Intent(applicationContext, OCRReaderActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("calculator")) {
                    val intent = Intent(applicationContext, CalculatorActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("weather")) {
                    val intent = Intent(applicationContext, WeatherActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("object")) {
                    onPause()
                    val intent = Intent(
                        applicationContext,
                        ObjectDetectionActivity::class.java
                    )
                    startActivity(intent)
                    mVoiceInputTv.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("battery")) {
                    val intent = Intent(applicationContext, BatteryActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("location")) {
                    val intent = Intent(applicationContext, LocationActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv.setText(null)
//                } else if (mVoiceInputTv!!.text.toString().contains("exit")) {
//                    textToSpeech!!.speak("Closing the App", TextToSpeech.QUEUE_FLUSH, null)
//                    val timer = Handler()
//                    timer.postDelayed({
//                        onPause()
//                        finishAffinity()
//                        System.exit(0)
//                    }, 2000)
                } else {
                    textToSpeech!!.speak(
                        "Do not understand. Tap on the screen Say again",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    public override fun onDestroy() {
        if (mVoiceInputTv!!.text.toString().contains("exit")) {
            finish()
        }
        super.onDestroy()
    }

    public override fun onPause() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
        super.onPause()
    }

    companion object {
        private const val REQ_CODE_SPEECH_INPUT = 100
        var Readmessage: String? = null
        private var textToSpeech: TextToSpeech? = null
    }
}