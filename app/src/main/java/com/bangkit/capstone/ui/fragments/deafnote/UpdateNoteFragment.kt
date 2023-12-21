package com.bangkit.capstone.ui.fragments.deafnote

import android.app.AlertDialog
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
import androidx.navigation.fragment.navArgs
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.FragmentUpdateNoteBinding
import com.bangkit.capstone.helper.toast
import com.bangkit.capstone.model.Note
import com.bangkit.capstone.ui.DeafNoteActivity
import com.bangkit.capstone.viewmodel.NoteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UpdateNoteFragment : Fragment(R.layout.fragment_update_note) {

    private var _binding: FragmentUpdateNoteBinding? = null
    private val binding get() = _binding!!

    private val args: UpdateNoteFragmentArgs by navArgs()
    private lateinit var currentNote: Note
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUpdateNoteBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteViewModel = (activity as DeafNoteActivity).noteViewModel
        currentNote = args.note!!

        binding.etNoteBodyUpdate.setText(currentNote.noteBody)
//        binding.etNoteTitleUpdate.setText(currentNote.noteTitle)

        binding.fabDone.setOnClickListener {
//            val title = binding.etNoteTitleUpdate.text.toString().trim()
            val body = binding.etNoteBodyUpdate.text.toString().trim()

            if (body.isNotEmpty()) {
                val note = Note(currentNote.id, body)
                noteViewModel.updateNote(note)

                view.findNavController().navigate(R.id.action_updateNoteFragment_to_homeFragment)

            } else {
                activity?.toast("Enter a note title please")
            }
        }

        binding.etNoteBodyUpdate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Update etNoteTitle in real-time
                binding.etNoteTitleUpdate.setText(s.toString())
            }
        })

        val clearButton = view.findViewById<Button>(R.id.clear_btn)
        clearButton.setOnClickListener {
            clearTextFields()
        }

        val deleteNote = view.findViewById<FloatingActionButton>(R.id.fab_delete)

        deleteNote.setOnClickListener {
            deleteNote()
        }
    }

    private fun clearTextFields() {
        binding.etNoteTitleUpdate.text.clear()
        binding.etNoteBodyUpdate.text.clear()
    }

    private fun deleteNote() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Note")
            setMessage("Are you sure you want to permanently delete this note?")
            setPositiveButton("DELETE") { _, _ ->
                noteViewModel.deleteNote(currentNote)
                view?.findNavController()?.navigate(
                    R.id.action_updateNoteFragment_to_homeFragment
                )
            }
            setNegativeButton("CANCEL", null)
        }.create().show()

    }


//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        menu.clear()
//        inflater.inflate(R.menu.menu_update_note, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.menu_delete -> {
//                deleteNote()
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}