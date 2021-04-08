package com.reas.redditdownloaderkotlin.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.*
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.reas.redditdownloaderkotlin.database.AppDB
import com.reas.redditdownloaderkotlin.models.Posts
import com.reas.redditdownloaderkotlin.models.PostsPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

private const val TAG = "MediaScanner"

class MediaScanner(private val context: Context) {
    private val contentResolver: ContentResolver = context.contentResolver
    private val appDB: AppDB = AppDB.getDatabase(context)

    /**
     * Scans for existing media created by this app in mediastore API for changes
     * Uses CoroutineScope as this activity might get destroyed quickly
     */
    @SuppressLint("InlinedApi")
    @Suppress("DEPRECATION")
    fun scanMedia() = CoroutineScope(Dispatchers.IO).launch {
        Log.d(TAG, "scanMedia:")
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    ),
                    MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                )
            } else {
                arrayOf(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            }

        val collectionFile =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else Uri.parse("A")
//        Log.d(TAG, "scanMedia: $collectionFile")

        val projection = arrayOf(
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns._ID
        )

        val selection = "${MediaStore.MediaColumns.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(
            "Dit"
        )

        collection.forEach { it ->
            Log.d(TAG, "scanMedia: $it")
            val query = contentResolver.query(
                it,
                projection,
                selection,
                selectionArgs,
                null
            )

            Log.d(TAG, "scanMedia: ${query}")
            Log.d(TAG, "scanMedia: ${query?.count}")

            query!!.use { cursor ->
                while (cursor.moveToNext()) {
                    try {
                        val post = Posts(
                            fileUri = getFileUri(
                                id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID)),
                                appendedUri = it
                            ),
                            url = null,
                            mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                            platform = PostsPlatform.UNKNOWN,
                            height = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)),
                            width = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH))
                        )

                        addToDb(post)
                    } catch (e: Exception) {

                    }
//                    cursor.columnNames.forEach { colName ->
//                        val index = cursor.getColumnIndex(colName)
//                        Log.d(
//                            TAG, "scanMedia: [$colName]: ${
//                                cursor.getStringOrNull(index)
//                                    ?: cursor.getIntOrNull(index)
//                                    ?: cursor.getLongOrNull(index)
//                                    ?: cursor.getDoubleOrNull(index)
//                                    ?: cursor.getFloatOrNull(index)
//                                    ?: cursor.getShortOrNull(index)
//                            }"
//                        )
//
//
//
//                    }
                }
            }
            query.close()
        }
    }

    private fun getFileUri(id: Int, appendedUri: Uri): String {
        return ContentUris.withAppendedId(appendedUri, id.toLong()).toString()
    }

    private fun addToDb(post: Posts) {
        appDB.postsDao().insert(post)
    }
}