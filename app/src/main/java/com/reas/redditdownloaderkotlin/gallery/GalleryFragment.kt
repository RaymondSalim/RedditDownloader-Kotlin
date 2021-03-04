package com.reas.redditdownloaderkotlin.gallery

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.TransitionInflater
import com.reas.redditdownloaderkotlin.PostsViewModel
import com.reas.redditdownloaderkotlin.PostsViewModelFactory
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.RedditDownloaderApplication
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.instance] factory method to
 * create an instance of this fragment.
 */
class GalleryFragment : Fragment() {
    private val postsViewModel: PostsViewModel by viewModels {
        PostsViewModelFactory((this.requireActivity().application as RedditDownloaderApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO Move permission request to proper location
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val contextWrapper = ContextWrapper(this.requireActivity())
        val directory = contextWrapper.getExternalFilesDir(null)

        checkReadWritePermission()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        val recylerViewAdapter = view.findViewById<RecyclerView>(R.id.post_recyclerview)
        val adapter = GalleryRecylerViewAdapter(viewLifecycleOwner.lifecycleScope)
        recylerViewAdapter.adapter = adapter
        recylerViewAdapter.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        // Posts Livedata observer
        postsViewModel.allPosts.observe(this.requireActivity(), Observer { posts ->
            posts?.let { adapter.submitList(it) }
        })

        return view
    }


    private fun checkReadWritePermission() {
        var granted: Boolean = false
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permissions.forEach { el ->
            var response = ContextCompat.checkSelfPermission(requireActivity(), el)
            granted = response == PackageManager.PERMISSION_GRANTED
            }

        val READ_EXTERNAL_STORAGE_REQUEST = 5
        // TODO Tell user why we need permission
        if (!granted) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                READ_EXTERNAL_STORAGE_REQUEST
            )
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment GalleryFragment.
         */
        @JvmStatic
        val instance = GalleryFragment()
    }
}