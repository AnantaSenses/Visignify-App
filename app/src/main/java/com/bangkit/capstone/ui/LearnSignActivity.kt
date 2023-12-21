package com.bangkit.capstone.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.capstone.R
import com.bangkit.capstone.adapter.LearnSignAdapter
import com.bangkit.capstone.databinding.ActivityLearnSignBinding
import com.bangkit.capstone.model.LearnSign
import java.util.Locale

class LearnSignActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLearnSignBinding
    private lateinit var rvLearnSign: RecyclerView
    private lateinit var searchView: SearchView
    private val list = ArrayList<LearnSign>()
    private lateinit var adapter: LearnSignAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_learn_sign)
        binding = ActivityLearnSignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val arrowBack = findViewById<ImageView>(R.id.arrow_back_learn)

        arrowBack.setOnClickListener{
            finish()
        }
//        searchView = findViewById(R.id.search_view)
        rvLearnSign = findViewById(R.id.rv_LearnSign)
        rvLearnSign.setHasFixedSize(true)
        rvLearnSign.layoutManager = LinearLayoutManager(this)

        adapter = LearnSignAdapter(list)
        rvLearnSign.adapter = adapter


        list.addAll(getLearnSign())
        showRecyclerList()

//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                filterList(newText)
//                return true
//            }
//
//        })

    }

//    private fun filterList(query: String?){
//
//        if(query != null){
//            val filteredList = ArrayList<LearnSign>()
//            for (i in list){
//                if (i.alphabet.lowercase(Locale.ROOT).contains(query)){
//                    filteredList.add(i)
//                }
//            }
//            if (filteredList.isEmpty()){
//                Toast.makeText(this, "No Sign Alphabet Found", Toast.LENGTH_SHORT).show()
//            }else{
//                adapter.setFilteredList(filteredList)
//            }
//        }
//    }

    private fun getLearnSign(): ArrayList<LearnSign> {
        val dataAlphabet = resources.getStringArray(R.array.data_alphabet)
        val dataPhoto = resources.obtainTypedArray(R.array.data_photo)
        val listShirt = ArrayList<LearnSign>()
        for (i in dataAlphabet.indices) {
            val shirt = LearnSign(dataAlphabet[i], dataPhoto.getResourceId(i, -1))
            listShirt.add(shirt)
        }
        return listShirt
    }

    private fun showRecyclerList() {
        binding.rvLearnSign.layoutManager = LinearLayoutManager(this)
        val listLearnSignAdapter = LearnSignAdapter(list)
        binding.rvLearnSign.adapter = listLearnSignAdapter
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}