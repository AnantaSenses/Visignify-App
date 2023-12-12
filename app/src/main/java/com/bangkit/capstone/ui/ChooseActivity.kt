package com.bangkit.capstone.ui

import android.Manifest
import com.bangkit.capstone.R
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bangkit.capstone.data.GetDataLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.Locale


class ChooseActivity : AppCompatActivity() {
//    private var fusedLocationClient: FusedLocationProviderClient? =
//        null //One of the location APIs in google play services
//    private var addressResultReceiver: LocationAddressResultReceiver? =
//        null //receives the address results

    @SuppressLint("StaticFieldLeak")
    private var currentLocation: Location? = null
//    private lateinit var locationCallback: LocationCallback
    private var mVoiceInputTv: TextView? = null
    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f
    private var sfx: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
//        addressResultReceiver = LocationAddressResultReceiver(Handler())
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                currentLocation = locationResult.getLocations().get(0)
//                address
//            }
//        }
//        startLocationUpdates() //call this function to check location permission
        if (checkIfAlreadyhavePermission()) {
            Toast.makeText(applicationContext, "Permission is granted", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this@ChooseActivity, arrayOf(Manifest.permission.READ_SMS),
                1
            )
        }
        textToSpeech = TextToSpeech(
            this
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.setSpeechRate(1f)
                if (firstTime == 0) textToSpeech!!.speak(
                    "Welcome to Vision App. Swipe right to listen the features of the app. And swipe left and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                //when user return from another activities to main activities.
                if (firstTime != 0) textToSpeech!!.speak(
                    "you are in main menu. just swipe left and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }
        mVoiceInputTv = findViewById<View>(R.id.voice_input) as TextView
    }

//    private fun startLocationUpdates() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
//            PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
//        } else {
//            val locationRequest = LocationRequest()
//            locationRequest.setInterval(10000)
//            locationRequest.setFastestInterval(10000)
//            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            locationCallback?.let {
//                fusedLocationClient?.requestLocationUpdates(locationRequest,
//                    it, null)
//            }
//        }
//    }

//    private val address: Unit
//        private get() {
//            if (!Geocoder.isPresent()) {
//                //            Toast.makeText(Home.this, "Can't find current address, ",
//                //                    Toast.LENGTH_SHORT).show();
//                return
//            }
//            val intent = Intent(this, GetDataLocation::class.java)
//            intent.putExtra("add_receiver", addressResultReceiver)
//            intent.putExtra("add_location", currentLocation)
//            startService(intent)
//        }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startLocationUpdates()
//            } else {
////                Toast.makeText(this, "Location permission not granted, " + "restart the app if you want the feature", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

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
                    val intent = Intent(this@ChooseActivity, HomeBlindActivity::class.java)
                    startActivity(intent)
                    return true // Exit the function early
                }
                if (x1 > x2) {
                    firstTime = 1
                    sfx!!.start()
                    startVoiceInput()
//                    locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
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
                } else if (mVoiceInputTv!!.text.toString().contains("read")) {
                    val intent = Intent(applicationContext, OCRReaderActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("calculator")) {
                    val intent = Intent(applicationContext, CalculatorActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("weather")) {
                    val intent = Intent(applicationContext, WeatherActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("object")) {
                    val intent = Intent(applicationContext, ObjectDetectionActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("battery")) {
                    val intent = Intent(applicationContext, BatteryActivity::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.setText(null)
                } else if (mVoiceInputTv!!.text.toString().contains("location")) {
                    val intent = Intent(applicationContext, LocationActivity::class.java)
                    intent.putExtra("location", currentAdd)
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

//    private inner class LocationAddressResultReceiver internal constructor(handler: Handler?) :
//        ResultReceiver(handler) {
//        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
//            if (resultCode == 0) {
//                address
//            }
//            if (resultCode == 1) {
//            }
//            if (resultCode == 3) {
//                city = resultData.getString("address_result")
//            }
//            currentAdd = resultData.getString("address_result")
//            showResults(currentAdd)
//        }
//    }

//    fun showResults(currentAdd: String?) {
//        Toast.makeText(this@ChooseActivity, currentAdd, Toast.LENGTH_LONG).show()
//
//        textToSpeech?.speak(currentAdd, TextToSpeech.QUEUE_FLUSH, null)
//    }


    override fun onResume() {
        super.onResume()
//        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
//        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE =
            2 //Request Code is used to check which permission called this function. // This request code is provided when the user is prompt for permission.
        private const val REQ_CODE_SPEECH_INPUT = 100
        private var firstTime = 0
        private var textToSpeech: TextToSpeech? = null
        var Readmessage: String? = null
        var name: String? = null
        private var currentAdd: String? = null
        private var city: String? = null
    }
}