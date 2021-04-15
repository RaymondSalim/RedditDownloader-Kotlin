package com.reas.redditdownloaderkotlin.ui.gallery

import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.marginBottom
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.RedditDownloaderApplication
import com.reas.redditdownloaderkotlin.databinding.FragmentGalleryBinding
import com.reas.redditdownloaderkotlin.models.AllPosts
import com.reas.redditdownloaderkotlin.ui.media_viewer.MediaViewerActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    private val galleryViewModel: GalleryViewModel by viewModels {
        GalleryViewModelFactory((this.requireActivity().application as RedditDownloaderApplication).repository)
    }

    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.gallery_contextbar, menu)

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val data = (binding.postRecyclerview.adapter as GalleryRecyclerViewAdapter).getSelectedPosts()

            when (item?.itemId) {
                R.id.share_btn -> {
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

                R.id.favorite_btn -> {
                    item.apply {
                        isChecked = !isChecked

                        icon = resources.getDrawable(
                            if (isChecked)
                                R.drawable.ic_baseline_favorite_24
                            else
                                R.drawable.ic_baseline_favorite_border_24,
                            null)
                    }
                    val postsList = data.values.toList()
                    galleryViewModel.updateFavorite(postsList, item.isChecked)
                    data.keys.forEach {
                        (binding.postRecyclerview.adapter as GalleryRecyclerViewAdapter).notifyItemChanged(it)
                    }

                }

                R.id.delete_btn -> {
//                    TODO
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            Log.d(TAG, "onDestroyActionMode: ")
            (binding.postRecyclerview.adapter as GalleryRecyclerViewAdapter).apply {
                clearSelectedPosts()
                setMultiselect(false)
                notifyDataSetChanged()
            }

            mActionMode = null
        }
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

                override fun onItemClicked(post: AllPosts?) {
                    val intent = Intent(requireContext(), MediaViewerActivity::class.java)
                    intent.putExtra("post_info", Json.encodeToString(post))
                    startActivity(intent)
                }

            })
        }
        binding.postRecyclerview.adapter = adapter
        binding.postRecyclerview.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)


        galleryViewModel.apply GVM@{
            with (this@GalleryFragment.requireActivity()) Activity@{
                // Posts Livedata observer
                allPosts.observe(this@Activity, { posts ->
                    posts?.let {
                        adapter.submitList(it)

                        view.findViewById<RelativeLayout>(R.id.empty_view).visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                })

                // SelectedPosts recyclerview Livedata observer
                recyclerViewSelectedPosts.observe(this@Activity, {
                    if (it.isNotEmpty()) {

                        // First time context action bar is toggled
                        if (mActionMode == null) {
                            mActionMode = this@Activity.startActionMode(actionModeCallback)
                            adapter.apply {
                                setMultiselect(true)
                                notifyDataSetChanged()
                            }
                        }

                        mActionMode?.menu?.apply {
                            fun <K, V> MutableMap<K, V>.forEachIteration(action: (Int, V) -> Unit): Unit {
                                for (i in 0 until this.keys.size) {
                                    val key = this.keys.toList()[i]
                                    action(i, this[key]!!)
                                }
                            }

                            var theSame = true
                            var first = false

                            (binding.postRecyclerview.adapter as GalleryRecyclerViewAdapter).getSelectedPosts()
                                .forEachIteration loop@{ i, allPosts ->
                                    if (i == 0) {
                                        first = allPosts.posts.isFavorite
                                    } else {
                                        if (allPosts.posts.isFavorite != first) {
                                            theSame = false
                                        }
                                    }
                                }
                            Log.d(TAG, "onCreateView: first: $first")
                            Log.d(TAG, "onCreateView: theSame: $theSame")


                            // Show favorite icon if all selected are the same
                            findItem(R.id.favorite_btn).apply {
                                Log.d(TAG, "onCreateView: favbtn")
                                if (theSame) {
                                    isVisible = true
                                    isChecked = first
                                } else {
                                    isVisible = false
                                }


                                icon = resources.getDrawable(
                                    if (isChecked)
                                        R.drawable.ic_baseline_favorite_24
                                    else
                                        R.drawable.ic_baseline_favorite_border_24,
                                    null
                                )
                            }
                        }

                        mActionMode?.title = "${it.size} selected"

                    } else {
                        adapter.apply {
                            setMultiselect(false)
                            notifyDataSetChanged()
                        }
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