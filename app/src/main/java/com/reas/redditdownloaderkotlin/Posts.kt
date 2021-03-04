package com.reas.redditdownloaderkotlin

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant


/**
 * Data are mostly gathered from json response (reddit.com/r/xxx/xxx/.json)
 * @param url = data > children > data > permalink
 * @param postTitle = data > children > data > title
 * @param postSubreddit = data > children > data > subreddit_name_prefixed
 * @param postDate = data > children > data > created_utc
 * @param postUser = data > children > data > author
 * @param createdAt = current time in millis
 * @param filePath = data > children > data > id
 */
@Entity(tableName = "posts_table", primaryKeys = ["url"])
data class Posts(
    @ColumnInfo(name = "url") val url: String, // data > children > data > permalink
    @ColumnInfo(name = "post_title") val postTitle: String?, // data > children > data > title
    @ColumnInfo(name = "post_subreddit") val postSubreddit: String?, // data > children > data > subreddit_name_prefixed
    @ColumnInfo(name = "post_date") val postDate: Long?, // data > children > data > created_utc
    @ColumnInfo(name = "post_user") val postUser: String?, // data > children > data > author
    @ColumnInfo(name = "created_at") val createdAt: Long? = System.currentTimeMillis(),
    @ColumnInfo(name = "file_path") val filePath: String?
)