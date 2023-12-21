package com.bangkit.capstone.ui

import android.Manifest.permission
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bangkit.capstone.R
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException
import java.util.Locale

class OCRReaderActivity : AppCompatActivity() {
    private var mVoiceInputTv: TextView? = null
    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f
    private lateinit var surfaceView: SurfaceView
    private lateinit var cameraSource: CameraSource
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var stringResult: String
    private lateinit var sfx: MediaPlayer

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocrreader)
        mVoiceInputTv = findViewById<View>(R.id.textViewOCR) as TextView
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission.CAMERA),
            PackageManager.PERMISSION_GRANTED
        )
        textToSpeech = TextToSpeech(
            this
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.CANADA
                textToSpeech!!.setSpeechRate(1f)
                Toast.makeText(
                    this@OCRReaderActivity,
                    "swipe right and say yes to read. and say no to return back to main menu",
                    Toast.LENGTH_SHORT
                ).show()
                textToSpeech!!.speak(
                    "swipe right and say yes to read. and say no to return back to main menu",
                    TextToSpeech.QUEUE_ADD,
                    null
                )
            }
        }
        mVoiceInputTv = findViewById<View>(R.id.textViewOCR) as TextView
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
                    textToSpeech!!.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null)
                    textToSpeech!!.speak(
                        "Swipe left to listen again. or swipe right and say what you want",
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                } else if (x1 > x2) {
                    sfx.start()
                    startVoiceInput()
                }
            }
        }
        return false
    }

    private fun textRecognizer() {
        Toast.makeText(this@OCRReaderActivity, "Tap on the screen and listen ", Toast.LENGTH_SHORT).show()
        textToSpeech!!.speak(
            " Tap on the screen take a picture of any text with your device and listen",
            TextToSpeech.QUEUE_FLUSH,
            null
        )
        textRecognizer = TextRecognizer.Builder(applicationContext).build()
        cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setRequestedPreviewSize(1280, 1024)
            .setAutoFocusEnabled(true)
            .build()
        surfaceView = findViewById(R.id.surfaceView)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            textToSpeech!!.speak(
                "Image is clearly visible tap on the screen",
                TextToSpeech.QUEUE_FLUSH,
                null
            )
        }, 5000)
        val context: Context = this
        surfaceView.getHolder().addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    cameraSource.start(surfaceView.getHolder())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })
    }

    private fun capture() {
        sfx = MediaPlayer.create(this@OCRReaderActivity, R.raw.tap_button)
        sfx.start()
        textRecognizer!!.setProcessor(object : Detector.Processor<TextBlock?> {
            override fun release() {}
            override fun receiveDetections(detections: Detections<TextBlock?>) {
                val sparseArray = detections.detectedItems
                val stringBuilder = StringBuilder()
                for (i in 0 until sparseArray.size()) {
                    val textBlock = sparseArray.valueAt(i)
                    if (textBlock != null && textBlock.value != null) {
                        stringBuilder.append(textBlock.value + " ")
                    }
                }
                val stringText = stringBuilder.toString()
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    stringResult = stringText
                    resultObtained()
                }
            }
        })
    }

    private fun resultObtained() {
        setContentView(R.layout.activity_ocrreader)
        val textView = findViewById<TextView>(R.id.textViewOCR)
        textView.text = stringResult
        textToSpeech!!.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null, null)
        textToSpeech!!.speak(
            "Swipe left to listen again. or swipe right and say what you want",
            TextToSpeech.QUEUE_ADD,
            null
        )
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    mVoiceInputTv!!.text = result!![0]
                }
                if (mVoiceInputTv!!.text.toString().contains("read")) {
                    val intent = Intent(applicationContext, OCRReaderActivity::class.java)
                    startActivity(intent)
                }
                if (mVoiceInputTv!!.text.toString().contains("object")) {
                    val intent = Intent(
                        applicationContext,
                        ObjectDetectionActivity::class.java
                    )
                    startActivity(intent)
                }
                if (mVoiceInputTv!!.text.toString().contains("battery")) {
                    val intent = Intent(applicationContext, BatteryActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                }
                if (mVoiceInputTv!!.text.toString().contains("location")) {
                    val intent = Intent(applicationContext, LocationActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                }
                if (mVoiceInputTv!!.text.toString().contains("weather")) {
                    val intent = Intent(applicationContext, WeatherActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                }
                 else {
                    textToSpeech!!.speak(
                        "Do not understand. just swipe right and Say it again",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
                if (mVoiceInputTv!!.text.toString().contains("calculator")) {
                    val intent = Intent(applicationContext, CalculatorActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("exit")) {
                    textToSpeech!!.speak("Closing the App", TextToSpeech.QUEUE_FLUSH, null)
                    val timer = Handler()
                    timer.postDelayed({ finish() }, 2000)
                } else {
                    textToSpeech!!.speak(
                        "Do not understand. just swipe right and Say it again",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
                if (mVoiceInputTv!!.text.toString().contains("main")) {
                    val i = Intent(this@OCRReaderActivity, HomeBlindActivity::class.java)
                    startActivity(i)
                }
                if (mVoiceInputTv!!.text.toString().contains("yes")) {
                    setContentView(R.layout.activity_ocrreader)
                    surfaceView = findViewById(R.id.surfaceView)
                    surfaceView.setOnClickListener(View.OnClickListener { capture() })
                    textRecognizer()
                    mVoiceInputTv!!.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("no")) {
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        textToSpeech!!.speak(
                            "you are in main menu. just swipe right and say what you want",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    }, 1000)
                    val intent = Intent(applicationContext, HomeBlindActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    public override fun onPause() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
        super.onPause()
    }

    companion object {
        private const val REQ_CODE_SPEECH_INPUT = 100
        private var textToSpeech: TextToSpeech? = null
        var Readmessage: String? = null
    }
}