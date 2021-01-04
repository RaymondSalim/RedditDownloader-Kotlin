package com.reas.redditdownloaderkotlin

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "posts")
data class Posts(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "url") val url: String?,
    @ColumnInfo(name = "post_title") val postTitle: String?,
    @ColumnInfo(name = "post_subreddit") val postSubreddit: String?,
    @ColumnInfo(name = "post_date") val postDate: Date?,
    @ColumnInfo(name = "post_user") val postUser: String?,
    @ColumnInfo(name = "download_date") val downloadedDate: Date?
)