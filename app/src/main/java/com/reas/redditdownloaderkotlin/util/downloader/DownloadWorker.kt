package com.reas.redditdownloaderkotlin.util.downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.database.AppDB
import com.reas.redditdownloaderkotlin.models.Jobs
import com.reas.redditdownloaderkotlin.models.Posts
import com.reas.redditdownloaderkotlin.models.PostsPlatform
import com.reas.redditdownloaderkotlin.models.RedditPosts
import com.reas.redditdownloaderkotlin.util.downloader.reddit.RedditJson
import java.io.File

private const val TAG = "DownloadWorker"

class DownloadWorker(appContext: Context, workerParameters: WorkerParameters): Worker(appContext, workerParameters) {
    private lateinit var appDB: AppDB
    private lateinit var url: String
    private var builder: NotificationCompat.Builder? = null
    private var jobId: Long = 0

    override fun doWork(): Result {
        url = inputData.getString("URL")!!
        appDB = AppDB.getDatabase(applicationContext)

        addJobToDB()

        try {
            createNotification()
            Downloader()
                .setUrl(url)
                .setContext(applicationContext)
                .registerObservers { status ->
                    Log.d(TAG, "doWork: $status")
                    updateJobDB(status)
                }.setDownloaderListener(object : Downloader.DownloaderListener {
                    override fun onSuccess(data: MutableMap<Downloader.DownloadData, Any>) {
                        super.onSuccess(data)
                        createPostDB(data)
                    }

                    override fun onError(exception: Exception) {
                        Log.d(TAG, "onError: Download Error")
                        Log.d(TAG, "onError: Exception: $exception")
                        super.onError(exception)
                        NotificationManagerCompat.from(applicationContext).apply {
                            builder!!.setContentText("An Error Occurred")
                                .setProgress(0, 0, false)
                            notify(jobId.toInt(), builder!!.build())
                        }

                        throw exception
                    }

                    override fun onDownloadProgressChange(progress: Float) {
                        super.onDownloadProgressChange(progress)

                        appDB.jobsDao().updateProgress(progress, this@DownloadWorker.url)

                        NotificationManagerCompat.from(applicationContext).apply {
                            builder!!
                                .setProgress(100, (progress * 100).toInt(), false)
                            notify(jobId.toInt(), builder!!.build())
                        }
                    }

                    override fun onDownloadEnd() {
                        Log.d(TAG, "onDownloadEnd: Download End")
                        super.onDownloadEnd()

                        appDB.jobsDao().updateProgress(1F, this@DownloadWorker.url)


                        NotificationManagerCompat.from(applicationContext).apply {
                            builder!!
                                .setContentText("Download complete")
                                .setProgress(0, 0, false)
                            notify(jobId.toInt(), builder!!.build())
                        }
                    }

                    override fun onJsonGrabbed(title: String) {
                        super.onJsonGrabbed(title)
                        NotificationManagerCompat.from(applicationContext).apply {
                            builder!!.setContentTitle(title)
                            notify(jobId.toInt(), builder!!.build())
                        }
                    }
                })
                .start()
        } catch (exception: Exception) {
            Log.d(TAG, "doWork: Exception ${exception.message}")
            return Result.failure(
                workDataOf(
                    "Exception" to exception.cause.toString(),
                    "StackTrace" to exception.stackTrace.toString()
                )
            )
        }


        return Result.success()
    }

    private fun createPostDB(data: MutableMap<Downloader.DownloadData, Any>) {
        val json = data[Downloader.DownloadData.BASE_JSON]
        val fileUri = data[Downloader.DownloadData.FILE_URI]
        val mimeType = data[Downloader.DownloadData.MIME_TYPE] as String
        if (json is RedditJson) {
            val redditPost = RedditPosts(
                url = json.url,
                title = json.title,
                subreddit = json.subreddit,
                date = json.createdAt,
                author = json.author
            )
            val post = Posts(
                url = "https://reddit.com" + json.url,
                fileUri = fileUri.toString(),
                platform = PostsPlatform.REDDIT,
                mimeType = mimeType
            )
            appDB.postsDao().insert(post, redditPost)

        }

    }

    private fun addJobToDB() {
        val job = Jobs(
            url = url,
            status = JobStatus.ADDED_TO_WORKMANAGER
        )

        jobId = appDB.jobsDao().insert(job)
    }

    private fun updateJobDB(status: JobStatus) {
        appDB.jobsDao().updateJobStatus(status, url)
    }

    private fun createNotification() {
        createNotificationChannel()
        builder = NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id)).apply {
            setSmallIcon(R.drawable.ic_notification_icon)
            setContentTitle("Dit")
        }

        NotificationManagerCompat.from(applicationContext).apply {
            builder!!
                .setProgress(100, 0, true)
                .setContentText("Download in Progress")

            notify(jobId.toInt(), builder!!.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = applicationContext.getString(R.string.notification_channel_id)
            val name = applicationContext.getString(R.string.notification_channel_name)
            val descriptionText = applicationContext.getString(R.string.notification_channel_description)
            val importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}