package com.bangkit.capstone.ui.fragments.deafnote

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.FragmentNewNoteBinding
import com.bangkit.capstone.helper.toast
import com.bangkit.capstone.model.Note
import com.bangkit.capstone.ui.DeafNoteActivity
import com.bangkit.capstone.viewmodel.NoteViewModel
import com.google.android.material.snackbar.Snackbar

class NewNoteFragment : Fragment(R.layout.fragment_new_note) {

    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNewNoteBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteViewModel = (activity as DeafNoteActivity).noteViewModel
        mView = view

        val saveNote = view.findViewById<Button>(R.id.fab_done)
        saveNote.setOnClickListener {
            saveNote(view)
        }

        binding.etNoteBody.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Update etNoteTitle in real-time
                binding.etNoteTitle.setText(s.toString())
            }
        })

        val clearButton = view.findViewById<Button>(R.id.clear_btn)
        clearButton.setOnClickListener {
            clearTextFields()
        }
    }

    private fun clearTextFields() {
        binding.etNoteTitle.text.clear()
        binding.etNoteBody.text.clear()
    }

    private fun saveNote(view: View) {
//        val noteTitle = binding.etNoteTitle.text.toString().trim()
        val noteBody = binding.etNoteBody.text.toString().trim()

        if (noteBody.isNotEmpty()) {
            val note = Note(0, noteBody)

            noteViewModel.addNote(note)
            Snackbar.make(
                view, "Note saved successfully",
                Snackbar.LENGTH_SHORT
            ).show()
            view.findNavController().navigate(R.id.action_newNoteFragment_to_homeFragment)

        } else {
            activity?.toast("Please enter note message")
        }
    }


//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        menu.clear()
//        inflater.inflate(R.menu.menu_new_note, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.menu_save -> {
//                saveNote(mView)
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}