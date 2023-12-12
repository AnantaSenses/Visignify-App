package com.bangkit.capstone.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.capstone.R
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import java.util.Locale

class CalculatorActivity : AppCompatActivity() {
    var txtScreen: TextView? = null
    var textToSpeech: TextToSpeech? = null
    var txtInput: TextView? = null
    private var lastNumeric = false

    // Represent that current state is in error or not
    private var stateError = false
    private val REQ_CODE_SPEECH_INPUT = 100
    private lateinit var sfx: MediaPlayer
    private lateinit var calculationTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setNumericOnClickListener()
        setOperatorOnClickListener()
        txtInput = findViewById(R.id.txtInput)

        calculationTextView = findViewById(R.id.calculation_text_view)
        textToSpeech = TextToSpeech(
            applicationContext
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.setSpeechRate(1f)
                Toast.makeText(
                    this@CalculatorActivity,
                    "Opening the calculator......  just tap on the screen and say what you want to calculate. And Press the volume up button to return the main menu",
                    Toast.LENGTH_SHORT
                ).show()
                textToSpeech!!.speak(
                    "Opening the calculator......  just tap on the screen and say what you want to calculate or say what you want ",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }
    }

    private fun setNumericOnClickListener() {
        // Create a common OnClickListener
        View.OnClickListener { v -> // Just append/set the text of clicked button
            val button = v as Button
            if (stateError) {
                // If current state is Error, replace the error message
                txtScreen!!.text = button.text
                stateError = false
            } else {
                // If not, already there is a valid expression so append to it
                txtScreen!!.append(button.text)
            }

            // Set the flag
            lastNumeric = true
        }
    }

    private fun setOperatorOnClickListener() {
        // Create a common OnClickListener for operators
        View.OnClickListener { v -> // If the current state is Error do not append the operator
            // If the last input is number only, append the operator
            if (lastNumeric && !stateError) {
                val button = v as Button
                txtScreen!!.append(button.text)
                lastNumeric = false
            }
        }

        // Clear button
        findViewById<View>(R.id.btnClear).setOnClickListener {
            txtScreen!!.text = "" // Clear the screen
            txtInput!!.text = "" // Clear the input
            // Reset all the states and flags
            lastNumeric = false
            stateError = false
        }
        findViewById<View>(R.id.tap_speak).setOnClickListener {
            sfx = MediaPlayer.create(this@CalculatorActivity, R.raw.tap_button)
            sfx.start()
            if (stateError) {
                // If current state is Error, replace the error message
                txtScreen!!.text = "Try Again"
                stateError = false
            } else {
                // If not, already there is a valid expression so append to it
                promptSpeechInput()
            }
            // Set the flag
            lastNumeric = true
        }
    }

    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            getString(R.string.speech_prompt)
        )
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(
                applicationContext,
                getString(R.string.speech_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onEqual() {
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (lastNumeric && !stateError) {
            // Read the expression
            val inputNumber = txtInput!!.text.toString()
            calculationTextView.text = inputNumber
            // Create an Expression (A class from exp4j library)
            var expression: Expression? = null
            try {
                try {
                    expression = ExpressionBuilder(inputNumber).build()
                    val result = expression.evaluate()
                    calculationTextView.text = "$inputNumber = $result"
                    Toast.makeText(this@CalculatorActivity, "Answer is", Toast.LENGTH_SHORT).show()
                    textToSpeech!!.speak(
                        "Answer is $result",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                    textToSpeech!!.speak(
                        "tap on the screen and say what you want",
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                    textToSpeech!!.setSpeechRate(1f)
                } catch (e: Exception) {
                    calculationTextView.text = "Error, tap on the screen and say again"
                    textToSpeech!!.speak(
                        "Error, tap on the screen and say again",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                    onPause()
                }
            } catch (ex: ArithmeticException) {
                // Display an error message
                txtScreen!!.text = "Error"
                textToSpeech!!.speak(
                    "Error, tap on the screen and say again",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                stateError = true
                lastNumeric = true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    var change = result.toString()
                    txtInput!!.text = result!![0]
                    if (txtInput!!.text.toString().contains("read")) {
                        val intent = Intent(applicationContext, OCRReaderActivity::class.java)
                        startActivity(intent)
//                    } else if (txtInput!!.text.toString().contains("weather")) {
//                        val intent = Intent(applicationContext, Weather::class.java)
//                        startActivity(intent)
//                        txtInput.setText(null)
//                    } else if (txtInput!!.text.toString().contains("time and date")) {
//                        val intent = Intent(applicationContext, DateAndTime::class.java)
//                        startActivity(intent)
//                    } else if (txtInput!!.text.toString().contains("location")) {
//                        val intent = Intent(applicationContext, LocationActivity::class.java)
//                        startActivity(intent)
//                        txtInput.setText(null)
//                    } else if (txtInput!!.text.toString().contains("battery")) {
//                        val intent = Intent(applicationContext, Battery::class.java)
//                        startActivity(intent)
//                        txtInput.setText(null)
                    } else if (txtInput!!.text.toString().contains("exit")) {
                        textToSpeech!!.speak("Closing the App", TextToSpeech.QUEUE_FLUSH, null)
                        val timer = Handler()
                        timer.postDelayed({
                            finish()
                            finishAffinity()
                            super.onPause()
                        }, 2000)
                    } else {
                        textToSpeech!!.speak(
                            "Do not understand. Tap on the screen Say again",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)


                    // english-lang
                    change = change.replace("x", "*")
                    change = change.replace("X", "*")
                    change = change.replace("add", "+")
                    change = change.replace("sub", "-")
                    change = change.replace("to", "2")
                    change = change.replace(" plus ", "+")
                    change = change.replace("two", "2")
                    change = change.replace(" minus ", "-")
                    change = change.replace(" times ", "*")
                    change = change.replace(" into ", "*")
                    change = change.replace(" in2 ", "*")
                    change = change.replace(" multiply by ", "*")
                    change = change.replace(" divide by ", "/")
                    change = change.replace("divide", "/")
                    change = change.replace("equal", "=")
                    change = change.replace("equals", "=")
                    if (change.contains("=")) {
                        change = change.replace("=", "")
                    }
                    txtInput!!.text = change
                    onEqual()
                }
            }
        }
    }

    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> touchEvent.x
            MotionEvent.ACTION_UP -> {
                val x2 = touchEvent.x
                val x1 = touchEvent.x
                if (x1 < x2) {
                    val i = Intent(this@CalculatorActivity, HomeBlindActivity::class.java)
                    startActivity(i)
                } else {
                    if (x1 > x2) {
                        val i = Intent(this@CalculatorActivity, HomeBlindActivity::class.java)
                        startActivity(i)
                    }
                }
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val intent = Intent(applicationContext, HomeBlindActivity::class.java)
            startActivity(intent)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({ }, 1000)
        }
        return true
    }

    public override fun onDestroy() {
        if (txtInput!!.text.toString() == "exit") {
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