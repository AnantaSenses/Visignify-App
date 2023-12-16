package com.bangkit.capstone.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.bangkit.capstone.R

class HomeDeafFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_deaf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val signDetectionCard = view.findViewById<LinearLayout>(R.id.sign_detection)
        val weatherInformationCard = view.findViewById<LinearLayout>(R.id.weather_information)
        val deafNoteCard = view.findViewById<LinearLayout>(R.id.deaf_note)
        val learnSignCard = view.findViewById<LinearLayout>(R.id.learn_sign)

        signDetectionCard.setOnClickListener {
            val intentBlind = Intent(context, SignDetectionActivity::class.java)
            startActivity(intentBlind)
        }
        weatherInformationCard.setOnClickListener{
            val intentDeaf = Intent(context, WeatherActivity::class.java)
            startActivity(intentDeaf)
        }
        deafNoteCard.setOnClickListener{
            val intentDeaf = Intent(context, DeafNoteActivity::class.java)
            startActivity(intentDeaf)
        }
        learnSignCard.setOnClickListener{
            val intentDeaf = Intent(context, LearnSignActivity::class.java)
            startActivity(intentDeaf)
        }
    }

}