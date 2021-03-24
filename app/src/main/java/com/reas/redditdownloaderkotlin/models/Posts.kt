package com.reas.redditdownloaderkotlin.models

import android.content.ContentResolver
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.reas.redditdownloaderkotlin.util.downloader.JobStatus
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant


/**
 * @param createdAt = current time in millis
 * @param filePath
 */
@Entity(tableName = "posts_table")
data class Posts(
    @PrimaryKey @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "downloaded_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "file_uri") val fileUri: String?,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "platform") val platform: PostsPlatform, // Instagram or Reddit
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "width") val width: Int,
)

enum class PostsPlatform {
    INSTAGRAM,
    REDDIT
}

class PostsPlatformConverter {
    @TypeConverter
    fun fromString(value: String?): PostsPlatform? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun toString(value: PostsPlatform?): String? {
        return value?.let { Json.encodeToString(it) }
    }
}