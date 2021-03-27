package com.reas.redditdownloaderkotlin.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.databinding.ActivityMainBinding
import com.reas.redditdownloaderkotlin.ui.gallery.GalleryFragment
import com.reas.redditdownloaderkotlin.ui.settings.SettingsFragment
import com.facebook.common.logging.FLog

import com.facebook.imagepipeline.core.ImagePipelineConfig

import com.facebook.imagepipeline.listener.RequestLoggingListener

import com.facebook.imagepipeline.listener.RequestListener





const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding


    private val settingsFragment = SettingsFragment.instance
    private val galleryFragment = GalleryFragment.instance
    private lateinit var lastFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d(TAG, savedInstanceState.toString())

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Fresco.initialize(this@MainActivity)


        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("lastFragment")) {
                lastFragment = supportFragmentManager.getFragment(savedInstanceState, "lastFragment")!!
            }
        }

        initializeFragments()

        initializeBottomNavView()


        Toast.makeText(applicationContext, noBackupFilesDir.absolutePath, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onCreate: $noBackupFilesDir")

    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (lastFragment.isAdded) {
            supportFragmentManager.putFragment(outState, "lastFragment", lastFragment)
        }
        Log.d(TAG, "onSaveInstanceState: Saving")
        super.onSaveInstanceState(outState)
    }

    private fun initializeFragments() {
        Log.d(TAG, "initializeFragments: List of Fragments: ${supportFragmentManager.fragments.toString()}")

        // Adds new fragment to the fragment manager if it doesn't exists
        supportFragmentManager.commitNow {
            with(supportFragmentManager) {
                if (this.findFragmentByTag(R.id.settings_fragment.toString()) !is Fragment) {
                    Log.d(TAG, "initializeFragments: FM Does not have settingsFragment, initializing...")
                    add(R.id.fragment_container_view, settingsFragment, R.id.settings_fragment.toString())
                }

                if (this.findFragmentByTag(R.id.gallery_fragment.toString()) !is Fragment) {
                    Log.d(TAG, "initializeFragments: FM Does not have galleryFragment, initializing...")
                    add(R.id.fragment_container_view, galleryFragment, R.id.gallery_fragment.toString())
                }
            }
        }

        hideFragments()
    }

    private fun initializeBottomNavView() {
        val bottomNavViewBackground = binding.bottomNav.background as MaterialShapeDrawable

        bottomNavViewBackground.shapeAppearanceModel =
            bottomNavViewBackground.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, 25F)
                .build()

        binding.bottomNav.setOnNavigationItemSelectedListener { item ->

            Log.d(TAG, "initializeBottomNavView: Last Fragment = $lastFragment")
            // Skips if the clicked menuItem is the current fragment
            if (lastFragment.tag?.toInt() == item.itemId) {
                Log.d(TAG, "initializeBottomNavView: Skipped")
                return@setOnNavigationItemSelectedListener false
            }

            Log.d(TAG, "initializeBottomNavView: Hiding Last Fragment")
            supportFragmentManager.commitNow {
                setAnimation(this, lastFragment)
                hide(lastFragment)

                Log.d(TAG, "initializeBottomNavView: when(item.itemId)")
                Log.d(TAG, "initializeBottomNavView: item.itemId = ${item.itemId}")
                Log.d(TAG, "initializeBottomNavView: list of fragments = ${supportFragmentManager.fragments}")
                val frag = supportFragmentManager.findFragmentByTag(item.itemId.toString())
                Log.d(TAG, "initializeBottomNavView: frag = $frag")

                if (frag != null) {
                    setAnimation(this, frag)
                    show(frag)
                    lastFragment = frag
                }
            }
            true
        }
    }

    private fun setAnimation(fragmentTransaction: FragmentTransaction, fragment: Fragment) {
        with (fragment) {
            when (this) {
                is GalleryFragment -> {
                    fragmentTransaction.setCustomAnimations(
                            R.anim.slide_right_enter,
                            R.anim.slide_left_exit
                    )
                }
                is SettingsFragment -> {
                    fragmentTransaction.setCustomAnimations(
                            R.anim.slide_left_enter,
                            R.anim.slide_right_exit
                    )
                }
                else -> {}
            }
        }
    }

    private fun hideFragments() {
        // Gets updated fragment list
        val listOfFragments = supportFragmentManager.fragments
        Log.d(TAG, "initializeFragments: List of Fragments: $listOfFragments")

        /**
         * If lastFragment is initialized (from savedInstanceState), we hide all other fragments
         * else, visible fragment should be the main fragment (GalleryFragment)
         */
        supportFragmentManager.commitNow {
            if (this@MainActivity::lastFragment.isInitialized) {
                Log.d(TAG, "initializeFragments: Last Fragment is Initialized")
                Log.d(TAG, "initializeFragments: Last Fragment Tag is ${lastFragment.tag!!}")
                listOfFragments.remove(lastFragment)
                Log.d(TAG, "initializeFragments: List of Fragments: $listOfFragments")

            } else {
                listOfFragments.remove(galleryFragment)
                Log.d(TAG, "initializeFragments: List of Fragments: $listOfFragments")

                lastFragment = galleryFragment
            }

            listOfFragments.forEach {
                Log.d(TAG, "hideFragments: Hiding $it")
                hide(it)
            }
        }
    }
}