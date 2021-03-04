package com.reas.redditdownloaderkotlin.splashscreen

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.reas.redditdownloaderkotlin.MainActivity
import com.reas.redditdownloaderkotlin.R
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.splash_slide_1.*

private const val TAG = "SplashActivityTAG"

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this@SplashActivity)

        checkDarkMode(sharedPref)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firstRunCheck(sharedPref)

        initViewPager()
    }


    /**
     * Checks if it's the first time running the application, if not then close this activity and start MainActivity
     */
    private fun firstRunCheck(sharedPref: SharedPreferences) {
        if (!sharedPref.getBoolean("FIRST_RUN", true)) {
            launchMainActivity()
        }
    }

    private fun initViewPager() {
        val layouts = mutableListOf(
                R.layout.splash_slide_1,
                R.layout.splash_slide_2,
                R.layout.splash_slide_3
        )

        val viewPager = findViewById<ViewPager2>(R.id.viewpager)
        SplashScreenAdapter().apply {
            submitList(layouts)
        }.also {
            viewPager.adapter = it
        }

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        skip_button.setOnClickListener {
            progressBar.setProgress(100, true)
            setSharedPref()
            launchMainActivity()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d(TAG, "onPageSelected: $position")
                super.onPageSelected(position)
                progressBar.setProgress( (((position / (layouts.size).toFloat()) + (1/3F)) * 100).toInt(), true)

                if (position == layouts.size - 1) {
                    next_button.text = getString(R.string.done)
                    skip_button.visibility = View.GONE

                    next_button.setOnClickListener {
                        setSharedPref()
                        launchMainActivity()
                    }
                } else {
                    next_button.text = getString(R.string.next)
                    skip_button.visibility = View.VISIBLE

                    next_button.setOnClickListener {
                        viewPager.currentItem++;
                    }
                }
            }
        })




    }

    private fun launchMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Updates sharedpreferences value
     */
    private fun setSharedPref() {
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
//        val preferredTheme = sharedPref.getString("PREFERRED_THEME", AppCompatDelegate.MODE_NIGHT_UNSPECIFIED.toString())
        val preferredTheme = sharedPref.getString("PREFERRED_THEME", AppCompatDelegate.MODE_NIGHT_UNSPECIFIED.toString())
        AppCompatDelegate.setDefaultNightMode(preferredTheme!!.toInt())

    }
}