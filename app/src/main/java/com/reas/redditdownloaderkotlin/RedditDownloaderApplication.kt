package com.reas.redditdownloaderkotlin

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.reas.redditdownloaderkotlin.database.AppDB
import com.reas.redditdownloaderkotlin.repository.PostsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

const val PERMISSION_REQUEST_CODE = 34895

class RedditDownloaderApplication: Application() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDB.getDatabase(this@RedditDownloaderApplication, applicationScope) }
    val repository by lazy { PostsRepository(database.postsDao()) }

    companion object {
        var permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        fun checkPermission(context: Context, permissions: Array<String>): Boolean {
            var granted: Boolean = true

            permissions.takeWhile { granted }.forEach { el ->
                val response = ContextCompat.checkSelfPermission(context, el)
                granted = response == PackageManager.PERMISSION_GRANTED
            }

            return granted
        }

        fun requestPermission(context: Context, permissions: Array<String>) {
            // TODO Tell user why we need permission
            ActivityCompat.requestPermissions(
                context as Activity,
                permissions,
                PERMISSION_REQUEST_CODE
            )
        }
    }
}