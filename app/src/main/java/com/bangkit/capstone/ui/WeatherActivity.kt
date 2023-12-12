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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bangkit.capstone.R
import org.json.JSONException
import java.util.Locale

class WeatherActivity : AppCompatActivity() {
    private var cityInput: EditText? = null
    private val VOICE_CODE = 100
    private var textToSpeech: TextToSpeech? = null
    private var cityBtn: Button? = null
    private var voice_btn: ImageView? = null
    lateinit var cityTextView: TextView
    lateinit var timeTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var weatherStatusText: TextView
    private lateinit var temperatureText: TextView
    private var weatherStatusImg: ImageView? = null
    private var currentTime: String? = null
    private var dateOutput: String? = null
    private var cityEntered: String? = null
    private lateinit var sfx: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        cityTextView = findViewById(R.id.city_text_view)
        cityTextView.setText("")
        timeTextView = findViewById(R.id.time_text_view)
        timeTextView.setText("")
        dateTextView = findViewById(R.id.date_text_view)
        dateTextView.setText("")
        weatherStatusImg = findViewById(R.id.weather_img)
        temperatureText = findViewById(R.id.temperature_text)
        temperatureText.setText("")
        weatherStatusText = findViewById(R.id.weather_status_text)
        weatherStatusText.setText("")
        cityInput = findViewById(R.id.city_txt_input)
        cityBtn = findViewById(R.id.city_btn)
        cityInput = findViewById<View>(R.id.city_txt_input) as EditText
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.speak(
                    "tap on the screen and say the name of city.",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                textToSpeech!!.setSpeechRate(1f)
            }
        }
    }

    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        sfx = MediaPlayer.create(this@WeatherActivity, R.raw.tap_button)
        if (touchEvent.action == MotionEvent.ACTION_UP) {
            voice_to_text()
            sfx.start()
        }
        return super.onTouchEvent(touchEvent)
    }

    private fun voice_to_text() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Say Something!!"
        )
        try {
            startActivityForResult(intent, VOICE_CODE)
        } catch (e: ActivityNotFoundException) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            VOICE_CODE -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    cityInput!!.setText(result!![0])
                    if (cityInput!!.text.toString().contains("read")) {
                        val intent = Intent(applicationContext, OCRReaderActivity::class.java)
                        startActivity(intent)
                    } else if (cityInput!!.text.toString().contains("main menu")) {
                        val intent = Intent(applicationContext, HomeBlindActivity::class.java)
                        startActivity(intent)
                    } else if (cityInput!!.text.toString() == "location") {
                        val intent = Intent(applicationContext, LocationActivity::class.java)
                        startActivity(intent)
                        cityInput!!.setText(null)
                    } else if (cityInput!!.text.toString() == "battery") {
                        val intent = Intent(applicationContext, BatteryActivity::class.java)
                        startActivity(intent)
                        cityInput!!.setText(null)
                    }else if (cityInput!!.text.toString() == "object detection") {
                        val intent = Intent(applicationContext, ObjectDetectionActivity::class.java)
                        startActivity(intent)
                        cityInput!!.setText(null)
                    } else if (cityInput!!.text.toString().contains("calculator")) {
                        val intent = Intent(applicationContext, CalculatorActivity::class.java)
                        startActivity(intent)
                    } else if (cityInput!!.text.toString().contains("exit")) {
                        textToSpeech!!.speak("Closing the App", TextToSpeech.QUEUE_FLUSH, null)
                        val timer = Handler()
                        timer.postDelayed({
                            onPause()
                            finishAffinity()
                        }, 2000)
                    } else {
                        api_url(cityInput!!.text.toString())
                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }

        // user will have to say city name
        //To ensure editText is not empty
        if (cityInput!!.text.toString().contains("")) Toast.makeText(
            this@WeatherActivity,
            "Enter city name",
            Toast.LENGTH_SHORT
        ).show() else {
            cityEntered = cityInput!!.text.toString()
            api_url(cityEntered!!) //passing user inputy
        }
    }

    fun api_url(citySearch: String) {
        // creating url as per user input
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$citySearch&appid=b761b4cfe64507fdd7579ab7daf29996&units=metric"
        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val mainObject = response.getJSONObject("main")
                    val weatherArray = response.getJSONArray("weather")
                    val description = weatherArray.getJSONObject(0)
                    val icon = weatherArray.getJSONObject(0)
                    val iconId = icon.getString("icon")
                    val temp = Math.round(mainObject.getDouble("temp")).toString() + "°C"
                    val desc = description.getString("main")
                    updateUI(temp, desc) // method for setting values to the textViews
                    SetIcon(iconId) // setting icon as per response from api
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) { error ->
            try {
                textToSpeech!!.speak(
                    "Not getting weather details. Tap on the screen and say the name of city",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    fun updateUI(temperature: String?, description: String?) {
        cityEntered = cityInput!!.text.toString().replace("location".toRegex(), "")
        cityEntered = cityInput!!.text.toString().replace("calculator".toRegex(), "")
        cityEntered = cityInput!!.text.toString().replace("read".toRegex(), "")

        // showing output
        temperatureText!!.text = temperature ?: ""
        cityInput!!.setText(cityEntered)

        if (temperature == null || temperature.isEmpty()) {
            textToSpeech!!.speak(
                "Not getting weather details. Tap on the screen and say the name of the city",
                TextToSpeech.QUEUE_FLUSH,
                null
            )
        } else {
            textToSpeech!!.speak(
                "Temperature in the city $cityEntered is $temperature",
                TextToSpeech.QUEUE_FLUSH,
                null
            )
            textToSpeech!!.speak(
                "Tap on the screen and say the name of the city or say what you want",
                TextToSpeech.QUEUE_ADD,
                null
            )
        }

        weatherStatusText!!.text = description ?: ""
        timeTextView!!.text = currentTime
        dateTextView!!.text = dateOutput
        cityTextView!!.text = cityEntered
    }


    fun SetIcon(id: String?) {
        when (id) {
            "01d" -> weatherStatusImg!!.setImageResource(R.drawable.clear_skyd)
            "01n" -> weatherStatusImg!!.setImageResource(R.drawable.clear_skyn)
            "02d" -> weatherStatusImg!!.setImageResource(R.drawable.few_cloudsd)
            "02n" -> weatherStatusImg!!.setImageResource(R.drawable.few_cloudn)
            "03d" -> weatherStatusImg!!.setImageResource(R.drawable.few_cloudsd)
            "03n" -> weatherStatusImg!!.setImageResource(R.drawable.few_cloudn)
            "04d" -> weatherStatusImg!!.setImageResource(R.drawable.few_cloudsd)
            "04n" -> weatherStatusImg!!.setImageResource(R.drawable.few_cloudn)
            "09d" -> weatherStatusImg!!.setImageResource(R.drawable.raind)
            "09n" -> weatherStatusImg!!.setImageResource(R.drawable.rain_n)
            "10d" -> weatherStatusImg!!.setImageResource(R.drawable.raind)
            "10n" -> weatherStatusImg!!.setImageResource(R.drawable.rain_n)
            "11d" -> weatherStatusImg!!.setImageResource(R.drawable.stormd)
            "11n" -> weatherStatusImg!!.setImageResource(R.drawable.stormn)
            "13d" -> weatherStatusImg!!.setImageResource(R.drawable.snowd)
            "13n" -> weatherStatusImg!!.setImageResource(R.drawable.snown)
            "50d" -> weatherStatusImg!!.setImageResource(R.drawable.mist)
            "50n" -> weatherStatusImg!!.setImageResource(R.drawable.mistn)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    public override fun onPause() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
        super.onPause()
    }
}