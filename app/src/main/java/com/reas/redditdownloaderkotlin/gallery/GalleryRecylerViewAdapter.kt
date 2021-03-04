package com.reas.redditdownloaderkotlin.gallery

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.reas.redditdownloaderkotlin.Posts
import com.reas.redditdownloaderkotlin.PostsRoomDatabase
import com.reas.redditdownloaderkotlin.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalArgumentException

class GalleryRecylerViewAdapter(val scope: CoroutineScope): ListAdapter<Posts, GalleryRecylerViewAdapter.PostsViewHolder>(PostsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder.create(parent, scope)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class PostsViewHolder(itemView: View, val scope: CoroutineScope): RecyclerView.ViewHolder(itemView) {
        private val draweeView:SimpleDraweeView = itemView.findViewById(R.id.draweeView)

        fun bind(post: Posts?) {
            val filePath = post?.filePath
            val abs = File(Uri.decode(filePath))

            /**
             * Checks if file exists
             *   - Exists:
             *      - Setup DraweeController
             *      - Sets draweeView aspect ratio based on file
             *   - Does not exists:
             *      - Sets draweeView aspect ratio to 1
             */
            if (abs.exists()) {
                draweeView.controller = Fresco.newDraweeControllerBuilder()
                    .setUri("file://" + abs.path)
                    .setAutoPlayAnimations(true)
                    .build()
                try {
                    draweeView.aspectRatio = getAspectRatio(abs)
                } catch (e: IllegalArgumentException) {
                    if (abs.exists()) {
                        bind(post)
                    } else {
                        // TODO REMOVE FILE FROM DATABASE
                        scope.launch {
                            val db =
                                PostsRoomDatabase.getDatabase(itemView.context, scope = scope).postsDao().deleteWithUrl(
                                    post?.url!!
                                )
                        }
                    }
                }
            } else {
                draweeView.aspectRatio = 1F
            }



        }

        @Throws(IllegalArgumentException::class)
        fun getAspectRatio(file: File): Float {
            val width: Float
            val height: Float
            val extension = file.extension

            return if (extension.contains("mp4")) {
                // File is a video
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(file.absolutePath)
                width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toFloat()
                height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toFloat()
                width / height
            } else {
                // File is an image
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(file.absolutePath, options)
                options.outWidth.toFloat() / options.outHeight.toFloat()
            }
        }

        companion object {
            fun create(parent: ViewGroup, scope: CoroutineScope): PostsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_post_items, parent, false)
                return PostsViewHolder(view, scope)
            }
        }
    }

    class PostsComparator: DiffUtil.ItemCallback<Posts>() {
        override fun areItemsTheSame(oldItem: Posts, newItem: Posts): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Posts, newItem: Posts): Boolean {
            return oldItem == newItem
        }

    }
}