package com.reas.redditdownloaderkotlin.ui.gallery

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.accessibility.AccessibilityManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment
import androidx.work.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.databinding.FragmentGalleryDialogBinding
import com.reas.redditdownloaderkotlin.util.UrlInputValidator
import com.reas.redditdownloaderkotlin.util.downloader.DownloadWorker

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

    private var _binding: FragmentGalleryDialogBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        _binding = FragmentGalleryDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val view = binding.root

        val validator = UrlInputValidator(binding.urlInputLayout)

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.urlInput.doOnTextChanged { text, _, _, _ ->
            validator.validate(text.toString())
            Log.d(TAG, text.toString())
        }

        binding.downloadButton.setOnClickListener {
            if (validator.isValid()) {
                // TODO Data is valid
                val downloadWorkRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .addTag(binding.urlInput.text.toString()) // Uses url Input as tag
                    .setInputData(workDataOf(
                        "URL" to binding.urlInput.text.toString()
                    ))
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                WorkManager
                    .getInstance(requireContext())
//                    .enqueueUniqueWork("download", ExistingWorkPolicy.APPEND , downloadWorkRequest)
                    .enqueue(downloadWorkRequest)

                Toast.makeText(requireContext(), "Download started", Toast.LENGTH_LONG)
                    .show()

                Log.d(TAG, "onCreateDialog: ${WorkManager.getInstance(requireContext()).getWorkInfosByTag(binding.urlInput.text.toString()).toString()}")

                dismiss()
            } else {
                validator.triggerValidationFailed()
                val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
                binding.urlInput.startAnimation(shake)
            }
        }


        return androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.RoundedCornersDialog)
            .setView(view)
            .create()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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