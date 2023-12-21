package com.bangkit.capstone.ObjectDetection.utils

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle

class ErrorDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        val activity = activity
        return AlertDialog.Builder(activity)
            .setMessage(arguments.getString(ARG_MESSAGE))
            .setPositiveButton(
                R.string.ok
            ) { dialogInterface: DialogInterface?, i: Int -> activity.finish() }
            .create()
    }

    companion object {
        private const val ARG_MESSAGE = "message"

        @JvmStatic
        fun newInstance(message: String?): ErrorDialog {
            val dialog = ErrorDialog()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            dialog.arguments = args
            return dialog
        }

    }
}