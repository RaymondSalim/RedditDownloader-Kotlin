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
import android.content.res.AssetFileDescriptor
import android.database.sqlite.SQLiteConstraintException
import android.graphics.BitmapFactory
import android.media.*
import android.os.ParcelFileDescriptor
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ProgressCallback
import com.github.kittinunf.fuel.core.requests.FileDestinationCallback
import com.github.kittinunf.fuel.core.requests.StreamDestinationCallback
import java.nio.ByteBuffer
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.*


private const val TAG = "Downloader"

typealias Observer = (status: JobStatus) -> Unit

/**
 * [url] is validated string.
 */
class Downloader() {
    /**
     *  The Downloader class will run in the following order when the start() function is called
     *  NOTE: onError() can be called anywhere in the following steps. When it is called, the process is stopped
     *  1. onStart()
     *  2. onJsonGrabStart()
     *  3. onJsonGrabEnd()
     *  4. onDownloadStart()
     *  4a.onDownloadProgressChange()
     *  5. onDownloadEnd()
     *  6. onProcessingStart()
     *  7. onProcessingEnd()
     *  8. onSuccess() / onError()
     *
     */
    interface DownloaderListener {
        fun onStart()
        fun onJsonGrabStart()
        fun onJsonGrabEnd(title: String)
        fun onDownloadStart()
        fun onDownloadProgressChange(progress: Float)
        fun onDownloadEnd()
        fun onProcessingStart()
        fun onProcessingEnd()
        fun onSuccess(data: MutableMap<DownloadData, Any>)
        fun onError(exception: Exception, displayError: Boolean = false)
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
    private var isVRedditGif = false
    private var height: Int? = null
    private var width: Int? = null

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
        val exception = Exception("URL is not a valid reddit/instagram post")
        listener?.onError(exception, displayError = true)
        throw exception
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
            Log.d(TAG, "getMediaUrl: $url")
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

    private fun downloadFromMediaUrl(urlIn: String, force: Boolean = false) {
        listener?.onDownloadStart()
        updateStatus(JobStatus.MEDIA_DOWNLOAD_START)

        if (urlIn.contains("v.redd") && !force) {
            return downloadVRedditMedia(urlIn)
        }

        var outputStream: OutputStream? = null

        val progressHandler: ProgressCallback = { readBytes, totalBytes ->
            listener?.onDownloadProgressChange(readBytes / totalBytes.toFloat() )
        }
        
        val streamDestination: StreamDestinationCallback = { response, request ->
            val fileName = generateFileName(extension = getFileExtension(response))

            do {
                try {
                    outputStream = getOutputStream(fileName = fileName)
                } catch (ex: SQLiteConstraintException) {}
            } while (outputStream == null)

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
                listener?.onDownloadEnd()
                updateStatus(JobStatus.MEDIA_DOWNLOAD_END)
            }

            is Result.Failure -> {
                Log.d(TAG, "downloadFromMediaUrl: failed")
                updateStatus(JobStatus.FAILED)
                listener?.onError(result.getException())
                throw result.getException()
            }
        }

        listener?.onProcessingStart()
        updateStatus(JobStatus.PROCESSING_START)

        getWidthAndHeight(mediaUri = this.fileUri)

        listener?.onProcessingEnd()
        updateStatus(JobStatus.PROCESSING_END)
    }

    private fun generateFileName(extension: String): String {
        return "${getFileName()}_${LocalDateTime.now()}.$extension"
    }

    private fun removeMediaStorePendingStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            appContext!!.contentResolver.update(fileUri!!, values, null, null)
        }

    }

    /**
     * VReddit video file codec are not commonly supported, so we are converting it.
     */
    private fun downloadVRedditMedia(urlIn: String) {
        this.mimeType = "video/mp4"
        val fileName = generateFileName(extension = "mp4")
        var outputStream: OutputStream? = null

        do {
            try {
                outputStream = getOutputStream(fileName, mimeType = "video/mp4")
            } catch (ex: SQLiteConstraintException) {}
        } while (outputStream == null)

        var audioFuel: Triple<Request, Response, Result<ByteArray, FuelError>>? = null
        val outputFile = File.createTempFile(getFileName() + ".mp4", null, appContext!!.externalCacheDir)
        val tempVideoFile: File = File.createTempFile(getFileName() + ".video", null, appContext!!.externalCacheDir) // TODO CHECK IF EXTENSION WORKS
        var tempAudioFile: File? = null


        // Audio file not present
        // This is not a guarantee that no audio file is present, further check is required
        val hasAudio: Boolean = if (isVRedditGif) {
            false
        } else {
            val audioUrl = getVRedditAudioUrl(urlIn)

            audioFuel = Fuel.get(audioUrl)
                .response()

            val mimeType = audioFuel.second.headers["content-type"].first()

            // Checks if the page has valid audio file
            (mimeType == "video/mp4")
        }

        // Downloads audio file if it exists
        if (hasAudio) {
            val audioFileName = getFileName() + ".audio" // TODO CHECK IF EXTENSION WORKS
            tempAudioFile = File.createTempFile(audioFileName, null, appContext!!.externalCacheDir)
            Log.d(TAG, "downloadVRedditMedia: audioFile: ${tempAudioFile!!.canonicalPath}")
            val inputStream = audioFuel!!.second.body().toStream()
            copyStreamToFile(inputStream, tempAudioFile)
        }

        // Downloads video file
        val fileDestinationCallback: FileDestinationCallback = { _, _ ->
            tempVideoFile
        }

        val progressCallback: ProgressCallback = { readBytes, totalBytes ->
            listener?.onDownloadProgressChange(readBytes / totalBytes.toFloat() )
        }
        val (request, response, result) = Fuel.download(urlIn)
            .fileDestination(fileDestinationCallback)
            .progress(progressCallback)
            .response()

        when (result) {
            is Result.Success -> {
                Log.d(TAG, "downloadFromMediaUrl: success")
                listener?.onDownloadEnd()
                updateStatus(JobStatus.MEDIA_DOWNLOAD_END)
            }

            is Result.Failure -> {
                Log.d(TAG, "downloadFromMediaUrl: failed")
                updateStatus(JobStatus.FAILED)
                listener?.onError(result.getException())
                throw result.getException()
            }
        }
        listener?.onDownloadEnd()
        updateStatus(JobStatus.MEDIA_DOWNLOAD_END)

        listener?.onProcessingStart()
        updateStatus(JobStatus.PROCESSING_START)
        // Starts muxing
        try {
            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val videoMedEx = MediaExtractor()
            var audioMedEx: MediaExtractor? = null

            val videoAFD = AssetFileDescriptor(ParcelFileDescriptor.open(tempVideoFile, ParcelFileDescriptor.MODE_READ_WRITE), 0, AssetFileDescriptor.UNKNOWN_LENGTH)
            val audioAFD: AssetFileDescriptor

            videoMedEx.apply {
                setDataSource(videoAFD.fileDescriptor, videoAFD.startOffset, videoAFD.length)
                selectTrack(0)
                seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            }

            val sampleSize = 1024 * 1024
            val offset = 100

            val videoMedFormat = videoMedEx.getTrackFormat(0)
            val audioMedFormat: MediaFormat

            var videoTrackIndex = muxer.addTrack(videoMedFormat)
            var audioTrackIndex = 0

            val videoByteBuffer = ByteBuffer.allocate(sampleSize)
            var audioByteBuffer: ByteBuffer? = null

            var isFinished = false

            val videoBufferInfo = MediaCodec.BufferInfo()
            var audioBufferInfo: MediaCodec.BufferInfo? = null

            if (hasAudio) {
                audioMedEx = MediaExtractor()
                audioAFD = AssetFileDescriptor(ParcelFileDescriptor.open(tempAudioFile, ParcelFileDescriptor.MODE_READ_WRITE), 0, AssetFileDescriptor.UNKNOWN_LENGTH)

                audioMedEx.apply {
                    setDataSource(audioAFD.fileDescriptor, audioAFD.startOffset, audioAFD.length)
                    selectTrack(0)
                    seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                }

                audioMedFormat = audioMedEx.getTrackFormat(0)
                audioTrackIndex = muxer.addTrack(audioMedFormat)
                audioByteBuffer = ByteBuffer.allocate(sampleSize)
                audioBufferInfo = MediaCodec.BufferInfo()

            }

            muxer.start()
            while (!isFinished) {
                videoBufferInfo.apply {
                    this.offset = offset
                    this.size = videoMedEx.readSampleData(videoByteBuffer, offset)
                }

                if (videoBufferInfo.size < 0) {
                    Log.d(TAG, "downloadVRedditMedia: Saw video EOF")
                    isFinished = true
                    videoBufferInfo.size = 0
                } else {
                    videoBufferInfo.apply {
                        this.presentationTimeUs = videoMedEx.sampleTime
                        this.flags = videoMedEx.sampleFlags

                        muxer.writeSampleData(videoTrackIndex, videoByteBuffer, videoBufferInfo)
                        videoMedEx.advance()
                    }
                }
            }

            if (hasAudio) {
                var audioFinished = false

                while (!audioFinished) {
                    audioBufferInfo!!.apply {
                        this.offset = offset
                        this.size = audioMedEx!!.readSampleData(audioByteBuffer!!, offset)
                    }

                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                        Log.d(TAG, "downloadVRedditMedia: Saw audio EOF")
                        audioFinished = true
                        audioBufferInfo.size = 0
                    } else {
                        audioBufferInfo.apply {
                            this.presentationTimeUs = audioMedEx!!.sampleTime
                            this.flags = audioMedEx.sampleFlags

                            muxer.writeSampleData(audioTrackIndex, audioByteBuffer!!, audioBufferInfo)
                            audioMedEx.advance()

//                        frameCount++
                        }
                    }

                }
            }

            getWidthAndHeight(videoMediaFormat = videoMedFormat)

            muxer.stop()
            muxer.release()
            videoMedEx.release()
            audioMedEx?.release()
        } catch (fileNotFound: FileNotFoundException) {

        } catch (exception: Exception) {
            listener?.onError(exception)
        } finally {
            tempAudioFile?.delete()
            tempVideoFile.delete()

        }

        Files.copy(outputFile.toPath(), outputStream)
        outputStream.close()
        removeMediaStorePendingStatus()
        outputFile.delete()

        listener?.onProcessingEnd()
        updateStatus(JobStatus.PROCESSING_END)
    }

    private fun getWidthAndHeight(videoMediaFormat: MediaFormat? = null, mediaUri: Uri? = null) {
        var width = 0
        var height = 0

        videoMediaFormat?.let { format ->
            width = format.getInteger(MediaFormat.KEY_WIDTH)
            height = format.getInteger(MediaFormat.KEY_HEIGHT)
            with (format) {
                if (containsKey("crop-left") && containsKey("crop-right")) {
                    width = getInteger("crop-right") + 1 - getInteger("crop-left")
                }

                if (containsKey("crop-top") && containsKey("crop-bottom")) {
                    height = getInteger("crop-bottom") + 1 - getInteger("crop-top")
                }
            }
        }

        mediaUri?.let { uri ->
            if (this.mimeType!!.contains("image")) {

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true

                val afd = appContext?.contentResolver?.openAssetFileDescriptor(uri, "r")

                BitmapFactory.decodeFileDescriptor(afd?.fileDescriptor, null, options)

                width = options.outWidth
                height = options.outHeight
            } else {
                val keyCodes =
                    arrayOf(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT,
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                    )

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(appContext, uri)
                height = retriever.extractMetadata(keyCodes[0]).toInt()
                width = retriever.extractMetadata(keyCodes[1]).toInt()
            }
        }


        Log.d(TAG, "getWidthAndHeight: Height: ${height} Width: ${width}")
        this.width = width
        this.height = height
    }

    private fun copyStreamToFile(inputStream: InputStream, file: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(file)
            input.copyTo(outputStream)
        }
    }

    private fun getVRedditAudioUrl(urlIn: String): String {
        val regex = """(DASH_(?:\d)+.mp4)""".toRegex()
        return urlIn.replace(regex, "DASH_audio.mp4")
    }

    /**
     * Gets OutputStream from mediastore if API > 29 else from FileOutputStream
     */
    private fun getOutputStream(fileName: String, mimeType: String? = null): OutputStream {
        val responseMimeType: String = mimeType ?: this.mimeType!!

        mediaIsImage = (responseMimeType.substringBefore("/") == "image" )
        val envDir = if (mediaIsImage!!) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES
        val resolver = appContext!!.contentResolver
        var outputStream: OutputStream? = null

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
                put(MediaStore.MediaColumns.MIME_TYPE, responseMimeType)
                put(MediaStore.MediaColumns.IS_PENDING, 1)

                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    envDir + File.separator + appContext!!.getString(R.string.app_name)
                )
            }

            resolver.run {
                this@Downloader.fileUri = this.insert(mediaContentUri, values) ?: throw SQLiteConstraintException("UNIQUE constraint failed")
                outputStream = openOutputStream(fileUri!!) ?: return@run
            }

        } else {

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
        this.mimeType = mimeType.first()
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
        listener?.onJsonGrabStart()
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

                    val redditMediaUrl = RedditMediaUrl(JSONObject(post))
                    redditJson.mediaUrl = redditMediaUrl.getUrl()
                    Log.d(TAG, "getPageJson: url: ${redditJson.mediaUrl}")
                    this.isVRedditGif = redditMediaUrl.isGif()
                    this.baseJSON = redditJson

                    listener?.onJsonGrabEnd(title = redditJson.title)
                    updateStatus(JobStatus.GETTING_JSON_END)
                    Log.d(TAG, "getPageJson: END")
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

                updateStatus(JobStatus.FAILED)

                val exception = Exception("Failed to get page JSON")
                listener?.onError(exception)
                throw exception
            }
        }
    }

    fun start() {
        Log.d(TAG, "start: Start")
        listener?.onStart()
        updateStatus(JobStatus.START)

        try {
            if (this.baseJSON == null) {
                getPageJson()
            }

            val mediaUrl = getMediaUrl()

            downloadFromMediaUrl(mediaUrl)

        } catch (ex: NullPointerException) {
            listener?.onError(ex)
            updateStatus(JobStatus.FAILED)
        }

        listener?.onSuccess(
            mutableMapOf(
                DownloadData.BASE_JSON to this.baseJSON!!,
                DownloadData.FILE_URI to this.fileUri.toString(),
                DownloadData.MIME_TYPE to this.mimeType!!,
                DownloadData.WIDTH_HEIGHT to Pair(this.width!!, this.height!!),
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
        MIME_TYPE,
        WIDTH_HEIGHT
    }


}