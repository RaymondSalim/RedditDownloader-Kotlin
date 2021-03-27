package com.reas.redditdownloaderkotlin.ui.gallery

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.database.AppDB
import com.reas.redditdownloaderkotlin.models.AllPosts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

private const val TAG = "GalleryRecyclerViewAdapter"

class GalleryRecyclerViewAdapter(private val scope: CoroutineScope): ListAdapter<AllPosts, GalleryRecyclerViewAdapter.PostsViewHolder>(PostsComparator()) {
    private val selectedPosts = mutableMapOf<Int, AllPosts>()

    private var listener: AdapterInterface? = null

    interface AdapterInterface {
        fun onItemChanged(selectedPost: MutableMap<Int, AllPosts>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder(parent, scope)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val current = getItem(position)
        val isSelected = selectedPosts[position] == getItem(position)
        holder.bind(current, selected = isSelected)
    }

    fun setListener(adapterInterface: AdapterInterface) {
        this.listener = adapterInterface
    }

    fun clearSelectedPosts() {
        val iterator = this.selectedPosts.iterator()
        while (iterator.hasNext()) {
            notifyItemChanged(iterator.next().key)
            iterator.remove()
        }
    }

    fun getSelectedPosts(): MutableMap<Int, AllPosts> {
        return this.selectedPosts
    }

    inner class PostsViewHolder(itemView: View, private val scope: CoroutineScope): RecyclerView.ViewHolder(itemView) {
        constructor(parent: ViewGroup, scope: CoroutineScope): this(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_post_items, parent, false),
            scope
        )

        private val draweeView:SimpleDraweeView = itemView.findViewById(R.id.drawee_view)

        private fun toggleSelectedItem(post: AllPosts, isAdded: Boolean) {
            val layout = itemView.findViewById<RelativeLayout>(R.id.recycler_view_layout)
            val checkBox = itemView.findViewById<CheckBox>(R.id.recycler_view_checkbox)

            val currPadding = layout.paddingLeft
            val newPadding: Int

            if (isAdded) {
                newPadding = itemView.context.resources.getDimension(R.dimen.recyclerview_item_padding_selected).toInt()
                checkBox.visibility = View.VISIBLE
            } else {
                newPadding = 0
                checkBox.visibility = View.INVISIBLE
            }

            ValueAnimator.ofInt(currPadding, newPadding).apply {
                addUpdateListener {
                    layout.setPadding(it.animatedValue as Int)
                }
                duration = 200
                start()
            }

            checkBox.isChecked = isAdded
        }

        fun bind(post: AllPosts?, selected: Boolean) {
            val postVar = post?.posts!!
            val fileUri = Uri.parse(postVar.fileUri)
            if (!fileExists(uri = fileUri, url = postVar.url)) {
                return
            }
            Log.d(TAG, "bind: uri: $fileUri")

            val aspectRatio =
                if (postVar.height == 0 || postVar.width == 0)
                    getAspectRatio(fileUri, mimeType = postVar.mimeType)
                else
                    postVar.width.toFloat() / postVar.height


            val filePath = getFilePath(fileUri, mimeType = postVar.mimeType, url = postVar.url) ?: return
            Log.d(TAG, "bind: aspect ratio: $aspectRatio")

            draweeView.controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.fromFile(File(filePath)))
                .setAutoPlayAnimations(true)
                .build()
            draweeView.aspectRatio = aspectRatio
            Log.d(TAG, "bind: uri: ${Uri.fromFile(File(fileUri.path!!))}")

            itemView.setOnClickListener {
                if (this@GalleryRecyclerViewAdapter.selectedPosts.isNotEmpty()) {
                    val added = selectItem(post, adapterPosition)
                    toggleSelectedItem(post, added)
                } else {
                    Toast.makeText(itemView.context, fileUri.path, Toast.LENGTH_SHORT).show()
                    Toast.makeText(itemView.context, Uri.fromFile(File(filePath)).toString(), Toast.LENGTH_SHORT).show()
                }
            }

            itemView.setOnLongClickListener {
                val added = selectItem(post, adapterPosition)
                toggleSelectedItem(post, added)
                true
            }

            toggleSelectedItem(post, selected)
        }

        private fun selectItem(post: AllPosts, position: Int): Boolean {
            with (this@GalleryRecyclerViewAdapter.selectedPosts) {
                when (post) {
                    this[position] -> {
                        this.remove(position, post)
                        listener?.onItemChanged(selectedPosts)

                        return@selectItem false
                    }
                    else -> {
                        this[position] = post
                        listener?.onItemChanged(selectedPosts)

                        return@selectItem true
                    }
                }
            }
        }

        private fun fileExists(uri: Uri, url: String): Boolean {
            return try {
                itemView.context.contentResolver.openAssetFileDescriptor(uri, "r")?.close()
                true
            } catch (e: FileNotFoundException) {
                handleFileNotFound(url)
                false
            }
        }

        private fun handleFileNotFound(url: String) {
            scope.launch(Dispatchers.Main) {
                val db = AppDB.INSTANCE?.postsDao()
                db?.deleteWithUrl(url)
                db?.deleteInstagramPostsWithUrl(url)
                db?.deleteRedditPostsWithUrl(url)
            }
        }

        private fun getFilePath(fileUri: Uri, mimeType: String, url: String): String? {
            var path: String? = null
            val id = fileUri.toString().substringAfterLast("/")
            Log.d(TAG, "getFilePath: $id")

            val projection = arrayOf(
                "_data"
            )
            Log.d(TAG, "getFilePath: $projection")

            val selection = "_id=?"
            val selectionArgs = arrayOf(
                id
            )
            Log.d(TAG, "getFilePath: $selectionArgs")

            val uri =
                if (mimeType.contains("image")) {
                    MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                }

            val contentResolver = itemView.context.contentResolver
            val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
            Log.d(TAG, "getFilePath: $cursor")

            if (cursor != null) {
                Log.d(TAG, "getFilePath: ${cursor.count}")
                while (cursor.moveToNext()) {
                    val bucket = cursor.getColumnIndex(projection[0])
                    path = cursor.getString(bucket)
                    Log.d(TAG, "getFilePath: string: ${cursor.getString(bucket)}")
                }
                cursor.close()
                Log.d(TAG, "getFilePath: path: $path")
                return path
            }
            return null // TODO Support for < API 29
        }

        private fun getAspectRatio(uri: Uri, mimeType: String): Float {
            // BitmapFactory supports images better
            if (mimeType.contains("image")) {
                return getAspectRatioBitmapFactory(uri)
            }

            val keyCodes = arrayOf(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT,
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
            )


            val width: Float
            val height: Float

            val retriever = MediaMetadataRetriever()
            Log.d(TAG, "getAspectRatio: context?: ${itemView.context ?: "ISNULL"}")
            Log.d(TAG, "getAspectRatio: uri: $uri")
            retriever.setDataSource(itemView.context, uri)
            val name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            Log.d(TAG, "getAspectRatio: name: $name")
            height = retriever.extractMetadata(keyCodes[0]).toFloat()
            width = retriever.extractMetadata(keyCodes[1]).toFloat()

            Log.d(TAG, "getAspectRatio: $width")
            Log.d(TAG, "getAspectRatio: $height")
            return width / height

        }

        private fun getAspectRatioBitmapFactory(uri: Uri): Float {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            val afd = itemView.context.contentResolver.openAssetFileDescriptor(uri, "r")

            BitmapFactory.decodeFileDescriptor(afd?.fileDescriptor, null, options)
            return options.outWidth.toFloat() / options.outHeight.toFloat()
        }
    }

    class PostsComparator: DiffUtil.ItemCallback<AllPosts>() {
        override fun areItemsTheSame(oldItem: AllPosts, newItem: AllPosts): Boolean {
            Log.d(TAG, "areItemsTheSame: old: $oldItem")
            Log.d(TAG, "areItemsTheSame: new: $newItem")
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: AllPosts, newItem: AllPosts): Boolean {
            Log.d(TAG, "areContentsTheSame: old: $oldItem")
            Log.d(TAG, "areContentsTheSame: new: $newItem")
            return oldItem == newItem
        }

    }
}