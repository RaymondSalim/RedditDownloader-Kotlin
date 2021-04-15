package com.reas.redditdownloaderkotlin.ui.media_viewer

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import com.github.piasy.biv.view.FrescoImageViewFactory
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.databinding.ActivityMediaViewerBinding
import com.reas.redditdownloaderkotlin.models.AllPosts
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val TAG = "MediaViewerActivity"

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MediaViewerActivity : AppCompatActivity() {
    private lateinit var fullscreenContent: View
    private lateinit var fullscreenContentControls: LinearLayout

    private lateinit var binding: ActivityMediaViewerBinding
    private val hideHandler = Handler()
    private lateinit var postInfo: AllPosts

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
//        when (motionEvent.action) {
//            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS)
//            }
//            MotionEvent.ACTION_UP -> view.performClick()
//            else -> {
//            }
//        }
//        false
//    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMediaViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        postInfo = Json.decodeFromString(intent.getStringExtra("post_info")!!)

        isFullscreen = true

        with (postInfo.posts.mimeType) {
            Log.d(TAG, "onCreate: $this")
            when {
                contains("video") -> {
                    with(binding) {
                        Log.d(TAG, "onCreate: vid")
                        root.removeView(this.viewerBigImage)
                        fullscreenContent = this.videoView

                        videoView.setVideoURI(Uri.parse(postInfo.posts.fileUri))
                        videoView.start()
                    }
                }

                contains("gif") -> {
                    Log.d(TAG, "onCreate: gif")
                    binding.root.removeView(binding.videoView)

                    binding.viewerBigImage.apply {
                        setImageViewFactory(FrescoImageViewFactory())
                        Log.d(TAG, "onCreate: ${Uri.parse(postInfo.posts.fileUri)}")
                        showImage(Uri.parse(postInfo.posts.fileUri))
                        fullscreenContent = this
                    }
                }

                contains("image") -> {
                    Log.d(TAG, "onCreate: image")
                    binding.root.removeView(binding.videoView)

                    binding.viewerBigImage.apply { 
                        showImage(Uri.parse(postInfo.posts.fileUri))
                        Log.d(TAG, "onCreate: ${Uri.parse(postInfo.posts.fileUri)}")
                        fullscreenContent = this
                    }
                }

                else -> {
                    Log.d(TAG, "onCreate: else")
                }

            }
        }

        fullscreenContent.setOnClickListener {
            toggle()
        }
        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById<ImageButton>(R.id.viewer_delete_btn).setOnTouchListener(delayHideTouchListener)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.viewerToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.viewerBackBtn.setOnClickListener {
            finish()
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
//        delayedHide(100)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}