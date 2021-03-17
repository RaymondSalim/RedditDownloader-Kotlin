package com.reas.redditdownloaderkotlin.util.downloader

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.DestinationAsStreamCallback
import com.github.kittinunf.result.Result;
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.util.downloader.reddit.RedditJson
import com.reas.redditdownloaderkotlin.util.downloader.reddit.RedditMediaUrl
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import kotlin.NullPointerException

import android.content.Intent







private const val TAG = "Downloader"

typealias Observer = (status: JobStatus) -> Unit

/**
 * [url] is validated string.
 */
class Downloader() {
    interface DownloaderListener {
        fun onSuccess(data: MutableMap<DownloadData, Any>) {}
        fun onError(exception: Exception) {
            throw exception
        }
        fun onJsonGrabbed(title: String) {

        }
        fun onDownloadStart() {}
        fun onDownloadEnd() {}
        fun onDownloadProgressChange(progress: Float) {
            Log.d(TAG, "onDownloadProgressChange: $progress")
        }
    }

    private var observers = mutableListOf<Observer>()
    private var listener: DownloaderListener? = null
    private var appContext: Context? = null

    private var url: String = ""
    private var baseJSON: BaseJSON? = null
    private var isReddit = false
    private var jobStatus = JobStatus.ADDED_TO_WORKMANAGER
    private var processedUrl: String? = null
    private var fileUri: Uri? = null
    private var mimeType: String? = null
    private var mediaIsImage: Boolean? = null

    fun registerObservers(observer: Observer): Downloader {
        observers.add(observer)
        return this
    }

    fun setDownloaderListener(listener: DownloaderListener): Downloader {
        this.listener = listener
        return this
    }

    fun setUrl(url: String): Downloader {
        this.url = url
        this.processedUrl = getJsonUrl()
        return this
    }

    fun setContext(context: Context): Downloader {
        this.appContext = context
        return this
    }

    private fun notifyObservers() {
        observers.forEach { it(this.jobStatus) }
    }

    private fun getJsonUrl(): String {
        val redditRegex = """(reddit\.com|redd\.it)""".toRegex(RegexOption.IGNORE_CASE)
        val instagramRegex = """(instagram.com|instagr.am)""".toRegex(RegexOption.IGNORE_CASE)

        when {
            redditRegex.containsMatchIn(url) -> {
                this.isReddit = true
                return getRedditJsonUrl()
            }

            instagramRegex.containsMatchIn(url) -> {
                return getInstagramJsonUrl()
            }
        }

        throw Exception("URL is not a valid reddit/instagram post")
    }

    private fun getRedditJsonUrl(): String {
        val tempUrl = removeQuery(url)

        return "$tempUrl.json"
    }

    private fun getInstagramJsonUrl(): String {
        val tempUrl = removeQuery(url)

        return "$tempUrl/?__a=1"
    }

    private fun getMediaUrl(): String {
        if (isReddit) {
            var url = (this.baseJSON as RedditJson).mediaUrl

            // Replaces imgur .gifv to .mp4
            if (url.contains("imgur") && url.contains("gifv")) {
                url = url.replace(".gifv", ".mp4")
            }


            return url
        } else {
//            TODO Instagram JSON
        }
        return ""
    }

    private fun downloadFromMediaUrl(urlIn: String) {
        updateStatus(JobStatus.MEDIA_DOWNLOAD_START)

        if ("""(v.redd)""".toRegex().containsMatchIn(urlIn)) {
            return downloadVRedditMedia(urlIn)
        }

        var outputStream: OutputStream? = null

        val progressHandler: ((readBytes:Long, totalBytes:Long) -> Unit) = { readBytes, totalBytes ->
            Log.d(TAG, "downloadFromMediaUrl: $totalBytes")
            listener?.onDownloadProgressChange(readBytes / totalBytes.toFloat())
        }
        
        val streamDestination: ((response: Response, request: Request) -> Pair<OutputStream, DestinationAsStreamCallback>) = { response, request ->
            Log.d(TAG, "downloadFromMediaUrl: StreamDestination")
            val fileName = getFileName() + "." + getFileExtension(response)

            outputStream = getOutputStream(response, request, fileName = fileName)

            Pair(outputStream!!, { ByteArrayInputStream(ByteArray(0)) } )
        }

        val (request, response, result) = Fuel.download(urlIn)
            .streamDestination(streamDestination)
            .progress(progressHandler)
            .response()

        removeMediaStorePendingStatus()

        when (result) {
            is Result.Success -> {
                Log.d(TAG, "downloadFromMediaUrl: success")
                updateStatus(JobStatus.MEDIA_DOWNLOAD_END)
                listener?.onDownloadEnd()
            }

            is Result.Failure -> {
                Log.d(TAG, "downloadFromMediaUrl: failed")
                updateStatus(JobStatus.FAILED)
                listener?.onError(result.getException())
            }
        }

    }

    private fun removeMediaStorePendingStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            appContext!!.contentResolver.update(fileUri!!, values, null, null)
        }

    }

    private fun downloadVRedditMedia(urlIn: String) {
        // TODO
    }

    private fun getOutputStream(response: Response, request: Request, fileName: String): OutputStream {
        val resolver = appContext!!.contentResolver
        val mimeType = response.headers["content-type"]
        this.mimeType = mimeType.first()
        mediaIsImage = (mimeType.first().substringBefore("/") == "image" )

        var outputStream: OutputStream? = null
        var envDir: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val mediaContentUri =
                if (mediaIsImage!!) {
                    MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType.first())
                put(MediaStore.MediaColumns.IS_PENDING, 1)

                envDir = if (mediaIsImage!!) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    envDir + File.separator + appContext!!.getString(R.string.app_name)
                )
            }


            resolver.run {
                fileUri = resolver.insert(mediaContentUri, values) ?: return@run
                outputStream = openOutputStream(fileUri!!) ?: return@run
            }

        } else {
            envDir = if (mediaIsImage!!) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES

            val path = Environment.getExternalStoragePublicDirectory( envDir + File.separator + appContext!!.getString(R.string.app_name) ).absolutePath
            val file = File(path, fileName)
            this.fileUri = Uri.fromFile(file)
            outputStream = FileOutputStream(file)
        }


        return outputStream!!
    }

    private fun getFileName(): String {
        if (isReddit) {
            return (this.baseJSON as RedditJson).title
        } else {
//            TODO Instagram Caption
        }
        return ""
    }

    private fun getFileExtension(response: Response): String {
        val mimeType = response.headers["content-type"]
        return mimeType.first().substringAfter("/")
    }

    private fun removeQuery(urlIn: String): String {
        val queryRegex = """([?][a-z0-9=&%_]*)?""".toRegex(RegexOption.IGNORE_CASE)

        return queryRegex.replace(urlIn, "")
    }

    private fun updateStatus(jobStatus: JobStatus) {
        this.jobStatus = jobStatus
        notifyObservers()
    }
    
    fun getPageJson(): BaseJSON {
        updateStatus(JobStatus.GETTING_JSON_START)

        val (request, response, result) = Fuel.get(processedUrl!!)
            .header(
                Pair("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36")
            )
            .responseString()
        Log.d(TAG, "getPageJson URL: $processedUrl")
        when (result) {
            is Result.Success -> {
                val data = result.get()

                if (isReddit) {
                    val mainArray = JSONArray(data)

                    val post = mainArray
                        .getJSONObject(0)
                        .getJSONObject("data")
                        .getJSONArray("children")
                        .getJSONObject(0)
                        .getJSONObject("data")
                        .toString()

                    val redditJson = Json{
                        ignoreUnknownKeys = true
                        isLenient = true
                    }.decodeFromString<RedditJson>(post)

                    redditJson.mediaUrl = RedditMediaUrl(JSONObject(post)).getUrl()
                    this.baseJSON = redditJson
                    listener?.onJsonGrabbed(redditJson.title)


                    return this.baseJSON as RedditJson
                }
                // Is instagram

//                listener?.onJsonGrabbed(instagramJson.title)
                updateStatus(JobStatus.GETTING_JSON_END)

                return this.baseJSON!!
            }

            is Result.Failure -> {
                val ex = result.getException()
                Log.d(TAG, "getPageJson exception: $ex")
            }
        }

        updateStatus(JobStatus.FAILED)
        throw Exception("Failed to get page JSON")
    }

    fun start() {
        Log.d(TAG, "start: Start")

        try {
            if (this.baseJSON == null) {
                getPageJson()
            }

            val mediaUrl = getMediaUrl()

            downloadFromMediaUrl(mediaUrl)

        } catch (ex: NullPointerException) {
            updateStatus(JobStatus.FAILED)
            listener?.onError(ex)
        }

        listener?.onSuccess(
            mutableMapOf(
                DownloadData.BASE_JSON to this.baseJSON!!,
                DownloadData.FILE_URI to this.fileUri.toString(),
                DownloadData.MIME_TYPE to this.mimeType!!
            )
        )
        updateStatus(JobStatus.SUCCESS)
    }

    inner class Test {
        fun testFuel(urlIn: String) {
            val (request, response, result) =
                Fuel.download(urlIn)
                    .response()
            val contentType = response.headers["content-type"].toString()
            Log.d(TAG, "testFuel: $contentType")

        }

        fun testFuelDownload(urlIn: String) {
            val a = FileOutputStream(File("/home/raymonds/Desktop/asd.mp4"))

            val (request, response, result) =
                Fuel.download(urlIn)
                    .streamDestination{ response, request ->
                        Log.d(TAG, "testFuelDownload: ")
                        Pair(a, { FileInputStream(File("/home/raymonds/Desktop/asd.mp4")) })
                    }
                    .progress { readBytes, totalBytes ->
                        Log.d(TAG, "testFuelDownload: ${readBytes.toFloat() / totalBytes.toFloat()}")
                    }
                    .response()
            val contentType = response.headers["content-type"].toString()
            Log.d(TAG, "testFuel: $contentType")

        }
    }
    enum class DownloadData {
        BASE_JSON,
        FILE_URI,
        MIME_TYPE

    }


}