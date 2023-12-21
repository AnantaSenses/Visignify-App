package com.bangkit.capstone.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bangkit.capstone.R
import com.bangkit.capstone.data.GetDataLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.Locale

class LocationActivity : AppCompatActivity() {
    var x1 = 0f
    var x2 = 0f
    private var fusedLocationClient: FusedLocationProviderClient? =
        null //One of the location APIs in google play services
    private var addressResultReceiver: LocationAddressResultReceiver? =
        null //receives the address results
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    private var textToSpeech: TextToSpeech? = null
    private lateinit var sfx: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        addressResultReceiver = LocationAddressResultReceiver(Handler())
        //A Handler allows you to send and process Message and Runnable objects
        currentAddTv = findViewById(R.id.textView)
        textToSpeech = TextToSpeech(
            this@LocationActivity
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.getDefault()
                textToSpeech!!.setSpeechRate(1f)
                textToSpeech!!.speak(
                    "swipe left to get current location and swipe right to return in main menu",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.locations[0]
                address
            }
        }
        startLocationUpdates() //call this function to check location permission
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val locationRequest = LocationRequest()
            locationRequest.interval = 2000
            locationRequest.fastestInterval = 1000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback!!, null)
        }
    }

    private val address: Unit
        private get() {
            if (!Geocoder.isPresent()) {
                Toast.makeText(
                    this@LocationActivity, "Can't find current address, ",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent = Intent(this, GetDataLocation::class.java)
            intent.putExtra("add_receiver", addressResultReceiver)
            intent.putExtra("add_location", currentLocation)
            startService(intent)
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    "Location permission not granted, " + "restart the app if you want the feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private inner class LocationAddressResultReceiver internal constructor(handler: Handler?) :
        ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            if (resultCode == 0) {
                Log.d("Address", "Location null retrying")
                address
            }
            if (resultCode == 1) {
                Toast.makeText(this@LocationActivity, "Address not found, ", Toast.LENGTH_SHORT)
                    .show()
            }
            if (resultCode == 2) {
                val currentAdd = resultData.getString("address_result")
                showResults(currentAdd)
            }
        }
    }

    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        sfx = MediaPlayer.create(this, R.raw.swipe)
        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> x1 = touchEvent.x
            MotionEvent.ACTION_UP -> {
                x2 = touchEvent.x
                if (x1 > x2) { //swipe left
                    sfx.start()
                    if (currentAddTv!!.text.toString().isEmpty()) {
                        textToSpeech!!.speak(
                            "Please turn on location",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    } else {
                        textToSpeech!!.speak(
                            "Your current location is " + currentAddTv!!.text.toString(),
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
                if (x1 < x2) { //swipe right
                    sfx.start()
                    val i = Intent(this@LocationActivity, HomeBlindActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        textToSpeech!!.speak(
                            "You are in main menu. just swipe right and say what you want",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    }
                }
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
        fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE =
            2 //Request Code is used to check which permission called this function. // This request code is provided when the user is prompt for permission.

        @SuppressLint("StaticFieldLeak")
        var currentAddTv: TextView? = null
        fun showResults(currentAdd: String?) {
            currentAddTv!!.text = currentAdd
            //textToSpeech.speak(currentAdd,TextToSpeech.QUEUE_FLUSH,null);
        }
    }
}