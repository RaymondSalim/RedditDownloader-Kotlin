package com.reas.redditdownloaderkotlin.gallery

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.reas.redditdownloaderkotlin.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "GalleryDialogFragment"

class GalleryDialogFragment() : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val inflater = requireActivity().layoutInflater

        val view = inflater.inflate(R.layout.fragment_gallery_dialog, null)

        with (view) {
            val validator = UrlInputValidator(findViewById(R.id.url_input_layout))

            findViewById<Button>(R.id.cancel_button).setOnClickListener {
                dismiss()
            }

            val textInputEditText = findViewById<TextInputEditText>(R.id.url_input)

            textInputEditText.doOnTextChanged { text, _, _, _ ->
                validator.validate(text.toString())
                Log.d(TAG, text.toString())
            }

            findViewById<MaterialButton>(R.id.download_button).setOnClickListener {
                if (validator.isValid()) {
                    // TODO Data is valid
                    Toast.makeText(requireContext(), findViewById<TextInputEditText>(R.id.url_input).text, Toast.LENGTH_SHORT).show()

                } else {
                    validator.triggerValidationFailed()
                    val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
                    textInputEditText.startAnimation(shake)
                }
            }
        }


        return androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.RoundedCornersDialog)
            .setView(view)
            .create()

    }

    companion object {
        @JvmStatic
        val instance = GalleryDialogFragment()
        fun instance(param1: String, param2: String) =
            GalleryDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}