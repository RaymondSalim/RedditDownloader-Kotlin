package com.reas.redditdownloaderkotlin.ui.splashscreen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.RedditDownloaderApplication

class SplashScreenAdapter: ListAdapter<Int, SplashScreenAdapter.SplashScreenViewHolder>(comparator) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SplashScreenViewHolder {
        return SplashScreenViewHolder.create(parent, viewType)
    }

    override fun onBindViewHolder(viewHolder: SplashScreenViewHolder, position: Int) {
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position]
    }

    class SplashScreenViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup, viewType: Int): SplashScreenViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(viewType, parent, false)

                if (viewType == R.layout.splash_slide_4) {
                    with (view) {
                        findViewById<LottieAnimationView>(R.id.lottie_splash_image).apply {
                            setMaxFrame(25)
                        }

                        findViewById<MaterialButton>(R.id.request_btn).apply {
                            setOnClickListener {
                                with (RedditDownloaderApplication) {
                                    requestPermission(it.context, this.permissions)
                                }
                            }
                        }

                    }


                }

                return SplashScreenViewHolder(view)
            }
        }
    }

    companion object {
        private val comparator = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }

        }
    }

}