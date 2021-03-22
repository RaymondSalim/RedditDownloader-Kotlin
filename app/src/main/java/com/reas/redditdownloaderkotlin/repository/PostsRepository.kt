package com.reas.redditdownloaderkotlin.repository

import androidx.annotation.WorkerThread
import com.reas.redditdownloaderkotlin.models.Posts
import com.reas.redditdownloaderkotlin.database.PostsDAO
import com.reas.redditdownloaderkotlin.models.AllPosts
import com.reas.redditdownloaderkotlin.models.InstagramPosts
import com.reas.redditdownloaderkotlin.models.RedditPosts
import kotlinx.coroutines.flow.Flow

class PostsRepository(private val postsDao: PostsDAO) {
    val allPosts: Flow<List<AllPosts>> = postsDao.getAll();

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(post: Posts, redditPosts: RedditPosts) {
        postsDao.insert(post, redditPosts)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(post: Posts, instagramPosts: InstagramPosts) {
        postsDao.insert(post, instagramPosts)
    }
}