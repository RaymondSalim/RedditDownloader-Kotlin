package com.reas.redditdownloaderkotlin.gallery

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
class GalleryDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val inflater = requireActivity().layoutInflater


        return androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.RoundedCorners)
            .setView(inflater.inflate(R.layout.fragment_gallery_dialog, null))
            .create()


//        return MaterialAlertDialogBuilder(requireActivity(), R.style.RoundedCornersMaterial)
//            .setView(inflater.inflate(R.layout.fragment_gallery_dialog, null))
//            .create()


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