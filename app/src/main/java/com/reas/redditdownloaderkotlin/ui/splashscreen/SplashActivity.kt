package com.reas.redditdownloaderkotlin.ui.splashscreen

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.database.*
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.reas.redditdownloaderkotlin.PERMISSION_REQUEST_CODE
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.RedditDownloaderApplication
import com.reas.redditdownloaderkotlin.databinding.ActivitySplashBinding
import com.reas.redditdownloaderkotlin.ui.main.MainActivity
import com.reas.redditdownloaderkotlin.util.MediaScanner

private const val TAG = "SplashActivityTAG"

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var viewPager: ViewPager2? = null
    private var progressBar: ProgressBar? = null
    private var permissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)

        checkDarkMode(sharedPref)

        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firstRunCheck(sharedPref)

        initViewPager()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty()) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    endAppIntro()
                } else {
                    showPermissionRejectedConfirmation()
                }
            }
            else -> {}
        }
    }


    /**
     * Checks if it's the first time running the application, if not then close this activity and start MainActivity
     */
    private fun firstRunCheck(sharedPref: SharedPreferences) {

        // Finishes current Activity and opens MainActivity if not first time running the app
        if (!sharedPref.getBoolean("FIRST_RUN", true)) {
            launchMainActivity()
        }

        // Scans if there are external changes to files downloaded
        MediaScanner(applicationContext).scanMedia()
    }

    private fun initViewPager() {
        val granted: Boolean
        with (RedditDownloaderApplication) {
            granted = checkPermission(applicationContext, this.permissions)
        }
        Log.d(TAG, "initViewPager: $granted")

        val layouts: MutableList<Int> = if (granted) {
            mutableListOf(
                R.layout.splash_slide_1,
                R.layout.splash_slide_2,
                R.layout.splash_slide_3
            )
        } else {
            mutableListOf(
                R.layout.splash_slide_1,
                R.layout.splash_slide_2,
                R.layout.splash_slide_3,
                R.layout.splash_slide_4
            )
        }

        viewPager = findViewById(R.id.viewpager)

        SplashScreenAdapter().apply {
            submitList(layouts)
        }.also {
            viewPager!!.adapter = it
        }

        progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        // Skip button goes to the last page for permission request
        binding.skipButton.setOnClickListener {
            if (granted) {
                endAppIntro()
            }
            progressBar!!.setProgress(75, true)

            viewPager!!.currentItem = layouts.size - 1
        }

        viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d(TAG, "onPageSelected: $position")
                super.onPageSelected(position)
                progressBar!!.setProgress( (((position / (layouts.size).toFloat()) + (1/4F)) * 100).toInt(), true)

                if (!permissionRequested) {
                    if (position == layouts.size - 1) {
                        binding.nextButton.text = getString(R.string.done)
                        binding.skipButton.visibility = View.GONE

                        binding.nextButton.setOnClickListener {
                            if (!granted) {
                                showPermissionRejectedConfirmation()
                            } else {
                                endAppIntro()
                            }
                        }
                    } else {
                        binding.nextButton.text = getString(R.string.next)
                        binding.skipButton.visibility = View.VISIBLE

                        binding.nextButton.setOnClickListener {
                            viewPager!!.currentItem++;
                        }
                    }
                } else {
                    with (binding) {
                        with (nextButton) {
                            text = getString(R.string.get_me_out)
                            setOnClickListener {
                               endAppIntro()
                            }
                        }

                        skipButton.visibility = View.GONE
                    }



                }
            }
        })
    }

    private fun showPermissionRejectedConfirmation() {
        permissionRequested = true
        progressBar!!.visibility = View.GONE
        (viewPager!!.adapter as SplashScreenAdapter).submitList(
            mutableListOf(
                R.layout.splash_slide_permission_confirmation
            )
        )
    }

    private fun endAppIntro() {
        updateSharedPreferences()
        launchMainActivity()
    }

    private fun launchMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Updates sharedpreferences value
     */
    private fun updateSharedPreferences() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)
        with (sharedPref.edit()) {
            putBoolean("FIRST_RUN", false)
//            putString("DOWNLOAD_LOCATION", getExternalFilesDir(null)?.canonicalPath)
            apply()

        }
    }

    /**
     * Sets the theme on startup based on sharedpreference
     */
    private fun checkDarkMode(sharedPref: SharedPreferences) {
        val preferredTheme = sharedPref.getString("PREFERRED_THEME", AppCompatDelegate.MODE_NIGHT_UNSPECIFIED.toString())
        AppCompatDelegate.setDefaultNightMode(preferredTheme!!.toInt())

    }
}