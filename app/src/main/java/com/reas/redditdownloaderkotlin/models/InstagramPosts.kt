package com.reas.redditdownloaderkotlin.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts_instagram_table")
data class InstagramPosts(
    @PrimaryKey @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "caption") val caption: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "date") val date: Long
)
