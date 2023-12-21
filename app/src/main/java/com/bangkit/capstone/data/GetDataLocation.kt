package com.bangkit.capstone.data

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.Objects

class GetDataLocation : IntentService(IDENTIFIER) {
    //An identifier is a name that identifies either a unique object or a unique class of objects from another java activities
    private var addressResultReceiver: ResultReceiver? = null

    //create object ResultReceiver to receive the address result
    private val texttospeech: TextToSpeech? = null
    override fun onHandleIntent(intent: Intent?) {
        //onHandleIntent is a intent service for passing the data
        var msg: String
        addressResultReceiver = Objects.requireNonNull(intent)?.getParcelableExtra("add_receiver")
        if (addressResultReceiver == null) {
            msg = "No receiver, not processing the request further"
            return
        }
        val location = intent!!.getParcelableExtra<Location>("add_location")
        if (location == null) {
            msg = "No location, can't go further without location"
            texttospeech!!.speak(msg, TextToSpeech.QUEUE_FLUSH, null)
            sendResultsToReceiver(0, msg)
            return
        }
        val geocoder = Geocoder(this, Locale.getDefault())
        //Geocoding refers to transforming street address or any address
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioException: Exception) {
            msg = "Error in getting address for the location"
        }
        if (addresses == null || addresses.size == 0) {
            msg = "No address found for the location"
            sendResultsToReceiver(1, msg)
        } else {
            val address = addresses[0]
            addressDetails = """
                Locality is, ${address.subLocality}.
                City is ,${address.locality}.
                State is, ${address.adminArea}.
                ${address.countryName}.
                
                """.trimIndent()
            sendResultsToReceiver(2, addressDetails)
            sendResultsToReceiver(3, address.locality)
        }
    }

    private fun sendResultsToReceiver(resultCode: Int, message: String?) {
        val bundle = Bundle()
        bundle.putString("address_result", message)
        addressResultReceiver!!.send(resultCode, bundle)
    }

    companion object {
        private const val IDENTIFIER = "GetAddressIntentService"
        var addressDetails: String? = null
    }
}