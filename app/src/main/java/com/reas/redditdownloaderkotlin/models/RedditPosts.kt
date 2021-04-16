package com.reas.redditdownloaderkotlin.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Data are mostly gathered from json response (reddit.com/r/xxx/xxx/.json)
 * @param url = data > children > data > permalink
 * @param title = data > children > data > title
 * @param subreddit = data > children > data > subreddit_name_prefixed
 * @param date = data > children > data > created_utc
 * @param author = data > children > data > author
 */
@Serializable
@Entity(tableName = "posts_reddit_table")
data class RedditPosts(
    @ColumnInfo(name = "url") val url: String?,
    @PrimaryKey @ColumnInfo(name = "permalink") val permalink: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "subreddit") val subreddit: String,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "author") val author: String
)