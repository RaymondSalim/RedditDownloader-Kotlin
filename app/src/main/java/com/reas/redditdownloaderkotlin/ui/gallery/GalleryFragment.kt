package com.reas.redditdownloaderkotlin.ui.gallery

import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.reas.redditdownloaderkotlin.GalleryViewModel
import com.reas.redditdownloaderkotlin.GalleryViewModelFactory
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.RedditDownloaderApplication
import com.reas.redditdownloaderkotlin.databinding.FragmentGalleryBinding
import com.reas.redditdownloaderkotlin.models.AllPosts
import java.util.ArrayList
import java.util.stream.Collectors

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.instance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "GalleryFragment"

class GalleryFragment : Fragment(), SearchView.OnQueryTextListener {
    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mActionMode: ActionMode? = null

    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.gallery_contextbar, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.share -> {
                    val data = (binding.postRecyclerview.adapter as GalleryRecyclerViewAdapter).getSelectedPosts()
                    Log.d(TAG, "onActionItemClicked: selectedPost size: ${data.size}")
                    val multipleData: Boolean = data.size > 1
                    val shareIntent = Intent().apply {
                        type = "image/* video/*"

                        if (multipleData) {
                            val uriList = data.toList().stream().map {
                                Uri.parse(it.second.posts.fileUri)
                            }.collect(
                                Collectors.toList()
                            ) as ArrayList

                            action = Intent.ACTION_SEND_MULTIPLE
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                        } else {
                            val key = data.keys.first()
                            Log.d(TAG, "onActionItemClicked: ${data}")
                            Log.d(TAG, "onActionItemClicked: ${data[key]}")
                            val uri = Uri.parse(data[key]?.posts?.fileUri)

                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, uri)
                        }
                    }


                    startActivity(Intent.createChooser(shareIntent, "Share media to.."))
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            Log.d(TAG, "onDestroyActionMode: ")
            (binding.postRecyclerview.adapter as GalleryRecyclerViewAdapter).clearSelectedPosts()

            mActionMode = null
        }
    }

    private val galleryViewModel: GalleryViewModel by viewModels {
        GalleryViewModelFactory((this.requireActivity().application as RedditDownloaderApplication).repository)
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

        val adapter = GalleryRecyclerViewAdapter(viewLifecycleOwner.lifecycleScope).apply {
            setListener(object: GalleryRecyclerViewAdapter.AdapterInterface {
                override fun onItemChanged(selectedPost: MutableMap<Int, AllPosts>) {
                    galleryViewModel.recyclerViewSelectedPosts.value = selectedPost
                }

            })
        }
        binding.postRecyclerview.adapter = adapter
        binding.postRecyclerview.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)


        galleryViewModel.apply GVM@{
            with (this@GalleryFragment.requireActivity()) Activity@{
                // Posts Livedata observer
                allPosts.observe(this@Activity, { posts ->
                    posts?.let { adapter.submitList(it) }
                })

                // SelectedPosts recyclerview Livedata observer
                recyclerViewSelectedPosts.observe(this@Activity, {
                    if (it.isNotEmpty()) {
                        if (mActionMode == null) {
                            mActionMode = this@Activity.startActionMode(actionModeCallback)
                        }

                        mActionMode?.title = "${it.size} selected"

                    } else {
                        mActionMode?.finish()
                    }
                })
            }
        }


        val bottomNavBar = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavBar.post {
            val fab = view.findViewById<FloatingActionButton>(R.id.fab)

            val layoutView = fab.layoutParams as ViewGroup.MarginLayoutParams

            Log.d(TAG, "onCreateView: navbar height${bottomNavBar.measuredHeight}")

            layoutView.setMargins(
                layoutView.leftMargin,
                layoutView.topMargin,
                layoutView.rightMargin,
                bottomNavBar.height + bottomNavBar.marginBottom * 2
            )
            fab.requestLayout()
        }

        binding.fab.setOnClickListener {
            val frag = GalleryDialogFragment.instance
            if (!frag.isAdded) {
                frag.show(childFragmentManager, R.id.gallery_dialog_fragment.toString())
            }
        }

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ${item.itemId}")
        return super.onOptionsItemSelected(item)
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

    override fun onQueryTextSubmit(query: String?): Boolean {
        Toast.makeText(requireContext(), "query ${query}", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Toast.makeText(requireContext(), "newtext ${newText}", Toast.LENGTH_SHORT).show()
        return true
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