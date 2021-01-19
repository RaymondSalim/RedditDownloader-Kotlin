package com.reas.redditdownloaderkotlin

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class PostsRepository(private val postsDao: PostsDAO) {
    val allPosts: Flow<List<Posts>> = postsDao.getAll();

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(post: Posts) {
        postsDao.insert(post)
    }
}