package com.reas.redditdownloaderkotlin.ui.gallery

import android.Manifest
import android.animation.ObjectAnimator
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.reas.redditdownloaderkotlin.PostsViewModel
import com.reas.redditdownloaderkotlin.PostsViewModelFactory
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.RedditDownloaderApplication
import com.reas.redditdownloaderkotlin.databinding.FragmentGalleryBinding

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.instance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "GalleryFragment"

class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val view = binding.root

//        binding.postRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                var bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
//                val rect = Rect()
//                bottomNav.getGlobalVisibleRect(rect)
//
//                var translateBy: Int
//
//                if (dy > 0) {
//                    translateBy = rect.top
//
//                } else {
//                    translateBy = 0
//                }
//
//                Log.d("Scroll", "onScrolled dy: $dy")
//                Log.d("Scroll", "onScrolled translateBy: $translateBy")
//
//                ObjectAnimator.ofFloat(bottomNav, "translationY", translateBy.toFloat()).apply {
//                    duration = 500
//                    start()
//                }
//            }
//
//        })

        val adapter = GalleryRecylerViewAdapter(viewLifecycleOwner.lifecycleScope)
        binding.postRecyclerview.adapter = adapter
        binding.postRecyclerview.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        // Posts Livedata observer
        postsViewModel.allPosts.observe(this.requireActivity(), Observer { posts ->
            posts?.let { adapter.submitList(it) }
        })


        val bottomNavBar = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavBar.post {
            val fab = view.findViewById<FloatingActionButton>(R.id.fab)

            val layoutView = fab.layoutParams as ViewGroup.MarginLayoutParams

            Log.d(TAG, "onCreateView: navbar height${bottomNavBar.measuredHeight}")

            layoutView.setMargins(layoutView.leftMargin, layoutView.topMargin, layoutView.rightMargin, bottomNavBar.height + bottomNavBar.marginBottom*2)
            fab.requestLayout()
        }

        binding.fab.setOnClickListener {
            GalleryDialogFragment.instance.show(childFragmentManager, R.id.gallery_dialog_fragment.toString())
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkReadWritePermission() {
        var granted: Boolean = false
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permissions.forEach { el ->
            val response = ContextCompat.checkSelfPermission(requireActivity(), el)
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