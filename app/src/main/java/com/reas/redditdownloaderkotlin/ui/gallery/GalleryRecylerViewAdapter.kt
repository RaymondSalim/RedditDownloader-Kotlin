package com.reas.redditdownloaderkotlin.ui.gallery

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import java.lang.Exception

private const val TAG = "GalleryRecyclerViewAdapter"

class GalleryRecylerViewAdapter(val scope: CoroutineScope): ListAdapter<AllPosts, GalleryRecylerViewAdapter.PostsViewHolder>(PostsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder.create(parent, scope)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class PostsViewHolder(itemView: View, val scope: CoroutineScope): RecyclerView.ViewHolder(itemView) {
        private val draweeView:SimpleDraweeView = itemView.findViewById(R.id.draweeView)

//        fun bind(post: AllPosts?) {
//            val fileUri = post?.posts?.fileUri
//            Log.d(TAG, "bind: $fileUri")
//            val abs = File(Uri.decode(fileUri))
//
//
//            /**
//             * Checks if file exists
//             *   - Exists:
//             *      - Setup DraweeController
//             *      - Sets draweeView aspect ratio based on file
//             *   - Does not exists:
//             *      - Sets draweeView aspect ratio to 1
//             */
//            if (abs.exists()) {
//                draweeView.controller = Fresco.newDraweeControllerBuilder()
//                    .setUri("file://" + abs.path)
//                    .setAutoPlayAnimations(true)
//                    .build()
//                try {
//                    draweeView.aspectRatio = getAspectRatio(abs)
//                } catch (e: IllegalArgumentException) {
//                    if (abs.exists()) {
//                        bind(post)
//                    } else {
//                        // TODO REMOVE FILE FROM DATABASE
//                        scope.launch {
//                            val db =
//                                AppDB.getDatabase(itemView.context, scope = scope).postsDao().deleteWithUrl(
//                                    post?.posts?.url!!
//                                )
//                        }
//                    }
//                }
//            } else {
//                draweeView.aspectRatio = 1F
//            }
//
//
//
//        }

        fun bind(post: AllPosts?) {
            val postVar = post?.posts!!
            val fileUri = Uri.parse(postVar.fileUri)
            if (!fileExists(uri = fileUri, url = postVar.url)) {
                return
            }
            Log.d(TAG, "bind: uri: $fileUri")

            val aspectRatio = getAspectRatio(fileUri, mimeType = postVar.mimeType)
            val filePath = getFilePath(fileUri, mimeType = postVar.mimeType, url = postVar.url) ?: return
            Log.d(TAG, "bind: aspect ratio: $aspectRatio")

            draweeView.controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.fromFile(File(filePath)))
                .setAutoPlayAnimations(true)
                .build()
            draweeView.aspectRatio = aspectRatio
            Log.d(TAG, "bind: uri: ${Uri.fromFile(File(fileUri.path!!))}")

            itemView.setOnClickListener {
                Toast.makeText(itemView.context, fileUri.path, Toast.LENGTH_SHORT).show()
                Toast.makeText(itemView.context, Uri.fromFile(File(filePath)).toString(), Toast.LENGTH_SHORT).show()
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
            // MediaMetadataRetriever doesn't support gif
            if (mimeType.contains("gif")) {
                return getAspectRatioBitmapFactory(uri)
            }

            val keyCodes =
                if (mimeType.contains("video")) {
                    arrayOf(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT,
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                    )
                } else {
                    arrayOf(
                        MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT,
                        MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH
                    )
                }


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

        companion object {
            fun create(parent: ViewGroup, scope: CoroutineScope): PostsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_post_items, parent, false)
                return PostsViewHolder(view, scope)
            }
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