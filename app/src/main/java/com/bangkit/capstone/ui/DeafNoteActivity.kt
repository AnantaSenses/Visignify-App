package com.bangkit.capstone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.ActivityDeafNoteBinding
import com.bangkit.capstone.db.NoteDatabase
import com.bangkit.capstone.repository.NoteRepository
import com.bangkit.capstone.viewmodel.NoteViewModel
import com.bangkit.capstone.viewmodel.NoteViewModelProviderFactory

class DeafNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeafNoteBinding
    lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeafNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val arrowBack = findViewById<ImageView>(R.id.arrow_back_deaf_note)

        arrowBack.setOnClickListener{
            finish()
        }
        setUpViewModel()
    }

    private fun setUpViewModel() {
        val noteRepository = NoteRepository(
            NoteDatabase(this)
        )

        val viewModelProviderFactory =
            NoteViewModelProviderFactory(
                application, noteRepository
            )

        noteViewModel = ViewModelProvider(
            this,
            viewModelProviderFactory
        )[NoteViewModel::class.java]
    }

}