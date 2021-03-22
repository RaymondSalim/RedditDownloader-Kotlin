package com.reas.redditdownloaderkotlin.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.reas.redditdownloaderkotlin.util.downloader.JobStatus
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


/**
 * Possible entries for status
 *
 */

@Entity(tableName = "jobs_table")
data class Jobs(
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "progress_percentage") var progressPercentage: Float = 0F, // value between 0 to 1 inclusive
    @ColumnInfo(name = "status") var status: JobStatus,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "retry_count") var retryCount: Int = 0
) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}

class JobsConverter {
    @TypeConverter
    fun fromString(value: String?): JobStatus? {
        return value?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun toString(value: JobStatus?): String? {
        return value?.let { Json.encodeToString(it) }
    }
}
