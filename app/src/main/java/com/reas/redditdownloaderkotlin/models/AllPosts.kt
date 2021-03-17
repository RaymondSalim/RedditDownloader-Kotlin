package com.reas.redditdownloaderkotlin.models

import androidx.room.Embedded
import androidx.room.Relation

data class AllPosts(
    @Embedded val posts: Posts,
    @Relation(
        parentColumn = "url",
        entityColumn = "url"
    )
    val instagramPosts: InstagramPosts?,

    @Relation(
        parentColumn = "url",
        entityColumn = "url"
    )
    val redditPosts: RedditPosts?,

    @Relation(
        parentColumn = "url",
        entityColumn = "url"
    )
    val jobs: Jobs?
)
