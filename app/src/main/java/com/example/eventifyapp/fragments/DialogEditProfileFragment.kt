package com.example.eventifyapp.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.eventifyapp.R
import com.google.android.material.textfield.TextInputEditText

class DialogEditProfileFragment : DialogFragment() {

    interface OnSaveListener {
        fun onSave(name: String, username: String, bio: String, location: String)
    }

    var listener: OnSaveListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_edit_profile, null)

        val etEditName = view.findViewById<TextInputEditText>(R.id.etEditName)
        val etEditBio = view.findViewById<TextInputEditText>(R.id.etEditBio)
        val etEditLocation = view.findViewById<TextInputEditText>(R.id.etEditLocation)

        // Retrieve arguments
        val name = arguments?.getString("name") ?: ""
        val username = arguments?.getString("username") ?: ""
        val bio = arguments?.getString("bio") ?: ""
        val location = arguments?.getString("location") ?: ""

        // Set initial values - since etEditName represents Username (as seen by its hint), we load username if not empty, otherwise name.
        etEditName.setText(if (username.isNotEmpty()) username else name)
        etEditBio.setText(bio)
        etEditLocation.setText(location)

        builder.setView(view)
            .setTitle("Edit Profile")
            .setPositiveButton("Save") { _, _ ->
                val newUsername = etEditName.text.toString().trim()
                val newBio = etEditBio.text.toString().trim()
                val newLocation = etEditLocation.text.toString().trim()
                listener?.onSave(
                    name = newUsername,
                    username = newUsername,
                    bio = newBio,
                    location = newLocation
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }
}