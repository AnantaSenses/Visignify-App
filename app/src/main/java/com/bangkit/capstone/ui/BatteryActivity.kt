package com.bangkit.capstone.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.capstone.R
import java.util.Locale

class BatteryActivity : AppCompatActivity() {
    lateinit var text: TextView
    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f
    var textToSpeech: TextToSpeech? = null
    private lateinit var sfx: MediaPlayer
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val intent = Intent(applicationContext, HomeBlindActivity::class.java)
            startActivity(intent)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                textToSpeech!!.speak(
                    "you are in main menu. just swipe right and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }, 1000)
        }
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery)
        text = findViewById(R.id.text)
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.getDefault()
                textToSpeech!!.setSpeechRate(1f)
                val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
                val percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                text.text = "Battery Percentage is $percentage %"
                text.text.toString()
                if (percentage < 50) {
                    textToSpeech!!.speak(
                        "Battery Percentage is$percentage %.please charge the phone",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                    textToSpeech!!.speak(
                        "swipe left to listen again or swipe right to return back in main menu",
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                } else {
                    textToSpeech!!.speak(
                        "Battery percentage is$percentage%.Mobile does not require charging ",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                    textToSpeech!!.speak(
                        "swipe left to listen again or swipe right to return back in main menu",
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                }
            }
        }


//        final Handler handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                textToSpeech.speak("Press volume up button to return in main menu", TextToSpeech.QUEUE_FLUSH, null);
//            }
//        }, 5000);
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
                if (x1 < x2) {
                    sfx.start()
                    val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
                    val percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    text!!.text = "Battery Percentage is $percentage %"
                    text!!.text.toString()
                    textToSpeech!!.speak(
                        "Battery percentage is$percentage%",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
                if (x1 > x2) {
                    sfx.start()
                    val intent = Intent(this@BatteryActivity, HomeBlindActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
        }
        return false
    }

    public override fun onDestroy() {
        if (text!!.text.toString() == "exit") {
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
}