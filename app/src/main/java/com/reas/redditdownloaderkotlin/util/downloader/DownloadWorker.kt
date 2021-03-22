package com.reas.redditdownloaderkotlin.util.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.database.AppDB
import com.reas.redditdownloaderkotlin.models.Jobs
import com.reas.redditdownloaderkotlin.models.Posts
import com.reas.redditdownloaderkotlin.models.PostsPlatform
import com.reas.redditdownloaderkotlin.models.RedditPosts
import com.reas.redditdownloaderkotlin.ui.main.MainActivity
import com.reas.redditdownloaderkotlin.util.downloader.reddit.RedditJson

private const val TAG = "DownloadWorker"

class DownloadWorker(appContext: Context, workerParameters: WorkerParameters): Worker(appContext, workerParameters) {
    private val appDB: AppDB = AppDB.getDatabase(applicationContext)
    private lateinit var url: String
    private var builder: NotificationCompat.Builder? = null
    private var jobId: Long = 0
    private var lastUpdate = 0L

    private val downloadListener = object : Downloader.DownloaderListener {
        override fun onStart() {
            createNotification()

            updateNotification(
                contentText = applicationContext.getString(R.string.notif_start)
            )
        }

        override fun onJsonGrabStart() {
            updateNotification(
                contentText = applicationContext.getString(R.string.notif_post_info_get),
            )
        }

        override fun onJsonGrabEnd(title: String) {
            NotificationManagerCompat.from(applicationContext).apply {
                builder!!.setContentTitle(title)
                notify(jobId.toInt(), builder!!.build())
            }

            updateNotification(
                contentText = applicationContext.getString(R.string.notif_post_info_success),
                progress = Triple(
                    first = 100, // max
                    second = 0.05F, // progress
                    third = false // indeterminate
                )
            )
        }

        override fun onDownloadStart() {
            updateNotification(
                contentText = applicationContext.getString(R.string.notif_media_download_start),
            )
        }

        override fun onDownloadProgressChange(progress: Float) {
            // https://github.com/kittinunf/fuel/blob/master/fuel/README.md#throttling-progress-output
            if (System.currentTimeMillis() - lastUpdate > 500) {
                lastUpdate = System.currentTimeMillis()
//                NotificationManagerCompat.from(applicationContext).apply {
//                    builder!!
//                        .setProgress(100, (progress * 100).toInt(), false)
//                    notify(jobId.toInt(), builder!!.build())
//                }

                val calculatedProgress = 0.05F + (progress * 85 / 100)

                updateNotification(
                    progress = Triple(
                        first = 100, // max
                        second = calculatedProgress, // progress
                        third = false // indeterminate
                    )
                )
            }
        }

        override fun onDownloadEnd() {
            Log.d(TAG, "onDownloadEnd: Download End")
//                        NotificationManagerCompat.from(applicationContext).apply {
//                            builder!!
//                                .setContentText("Download Complete")
//                                .setProgress(100, 90, false)
//                            notify(jobId.toInt(), builder!!.build())
//                        }

            updateNotification(
                contentText = applicationContext.getString(R.string.notif_media_download_complete),
                progress = Triple(
                    first = 100, // max
                    second = 0.9F, // progress
                    third = false // indeterminate
                )
            )
        }

        override fun onProcessingStart() {
            updateNotification(
                contentText = applicationContext.getString(R.string.notif_media_process_start),
                progress = Triple(
                    first = 100, // max
                    second = 0.9F, // progress
                    third = false // indeterminate
                )
            )
        }

        override fun onProcessingEnd() {
            updateNotification(
                contentText = applicationContext.getString(R.string.notif_media_process_complete),
                progress = Triple(
                    first = 100, // max
                    second = 1F, // progress
                    third = false // indeterminate
                )
            )
        }

        override fun onSuccess(data: MutableMap<Downloader.DownloadData, Any>) {
            createPostDB(data)

//                        NotificationManagerCompat.from(applicationContext).apply {
//                            builder!!
//                                .setContentText("Download Complete")
//                                .setProgress(100, 90, false)
//                            notify(jobId.toInt(), builder!!.build())
//                        }
            val fileUri = Uri.parse(data[Downloader.DownloadData.FILE_URI] as String)
            Log.d(TAG, "intent: URI: $fileUri")
            val fileName: String

            val json = data[Downloader.DownloadData.BASE_JSON]

            fileName = if (json is RedditJson) {
                json.title
            } else {
                "" // TODO
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "image/* video/*"
            }

            Log.d(TAG, "intent: ID: ${jobId.toInt()}")
            val sharePendingIntent = PendingIntent.getBroadcast(appContext, jobId.toInt(), Intent.createChooser(shareIntent, fileName), PendingIntent.FLAG_UPDATE_CURRENT)
            NotificationManagerCompat.from(applicationContext).apply {
                builder!!.apply {
                    addAction(0, "Share", sharePendingIntent)
                }
            }



            updateNotification(
                contentText = applicationContext.getString(R.string.notif_success),
                progress = Triple(
                    first = 0, // max
                    second = 0F, // progress
                    third = false // indeterminate
                ),
                /*actions = arrayOf(
                    Triple(
                        first = 0, // Icon
                        second = appContext.getString(R.string.notif_actions_share), // Text
                        third = sharePendingIntent // PendingIntent
                    )
                ),*/
                ongoing = false
            )
        }

        override fun onError(exception: Exception) {
            Log.d(TAG, "onError: Download Error")
            Log.d(TAG, "onError: Exception: $exception")
            Log.e(TAG, exception.stackTraceToString() )

//                        NotificationManagerCompat.from(applicationContext).apply {
//                            builder!!.setContentText("An Error Occurred")
//                                .setProgress(0, 0, false)
//                            notify(jobId.toInt(), builder!!.build())
//                        }

            updateNotification(
                contentText = applicationContext.getString(R.string.notif_error),
                progress = Triple(
                    first = 100, // max
                    second = 0F, // progress
                    third = false // indeterminate
                ),
                ongoing = false
            )

            throw exception
        }
    }

    override fun doWork(): Result {
        url = inputData.getString("URL")!!
        addJobToDB()

        try {
            Downloader()
                .setUrl(url)
                .setContext(applicationContext)
                .registerObservers { status ->
                    Log.d(TAG, "doWork: $status")
                    updateJobDB(status)
                }.setDownloaderListener(downloadListener)
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
                url = "https://reddit.com" + json.url,
                permalink = json.url,
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

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        builder = NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id)).apply {
            setSmallIcon(R.drawable.ic_notification_icon)
            setContentTitle("Dit")
            setContentIntent(pendingIntent)
            setProgress(100, 0, true)
            setOngoing(true)
        }

        NotificationManagerCompat.from(applicationContext).apply {
            notify(jobId.toInt(), builder!!.build())
        }
    }

    private fun updateNotification(
        title: String? = null,
        contentText: String? = null,
        progress: Triple<Int, Float?, Boolean>? = null,
        actions: Array<Triple<Int, CharSequence, PendingIntent>>? = null,
        ongoing: Boolean? = null
    ) {
        NotificationManagerCompat.from(applicationContext).apply {
            builder!!.apply {
                if (title != null) {
                    setContentTitle(title)
                }

                if (contentText != null) {
                    setContentText(contentText)
                }

                if (progress?.second != null) {
                    setProgress(progress.first, (progress.second!! * 100).toInt(), progress.third)

                    // Updates database
                    appDB.jobsDao().updateProgress( (progress.second!! * 100), this@DownloadWorker.url)
                }

                actions?.forEach {
                    this.addAction(it.first, it.second, it.third)
                }

                if (ongoing != null) {
                    setOngoing(ongoing)
                }
            }
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