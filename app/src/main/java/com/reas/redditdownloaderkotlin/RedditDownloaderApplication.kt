package com.reas.redditdownloaderkotlin

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class RedditDownloaderApplication: Application() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { PostsRoomDatabase.getDatabase(this@RedditDownloaderApplication, applicationScope) }
    val repository by lazy { PostsRepository(database.postsDao()) }
}