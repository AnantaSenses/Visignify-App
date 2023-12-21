package com.bangkit.capstone.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.ActivityHomeDeafBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class HomeDeafActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHomeDeafBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeDeafBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeDeafFragment())


        val bottomNavbar = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavbar.background = null



        val floatButton = findViewById<FloatingActionButton>(R.id.float_button)

        floatButton.setOnClickListener{
            val intentBlind = Intent(this, ObjectDetectionActivity::class.java)
            startActivity(intentBlind)
        }


        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(HomeDeafFragment())
                R.id.settings -> replaceFragment(SettingsFragment())
                else -> {
                }
            }
            true
        }


    }
    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}