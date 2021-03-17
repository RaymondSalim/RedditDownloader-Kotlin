package com.reas.redditdownloaderkotlin.util.downloader.reddit

import com.reas.redditdownloaderkotlin.util.downloader.BaseJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//@Serializable(with = RedditJsonSerializer::class)
@Serializable
data class RedditJson constructor(
    @SerialName("permalink")
    override var url: String,

    @SerialName("created_utc")
    override val createdAt: Long,

    @SerialName("over_18")
    override val isNsfw: Boolean,

    @SerialName("subreddit")
    val subreddit: String,

    @SerialName("title")
    val title: String,

    @SerialName("author")
    val author: String,

    @SerialName("domain")
    val mediaDomain: String,
    @SerialName("url_overridden_by_dest")
    var mediaUrl: String,
    @SerialName("is_gif")
    val isGif: Boolean = false
): BaseJSON()