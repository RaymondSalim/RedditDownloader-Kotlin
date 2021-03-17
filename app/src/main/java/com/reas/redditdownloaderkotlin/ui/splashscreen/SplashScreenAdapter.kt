package com.reas.redditdownloaderkotlin.ui.splashscreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SplashScreenAdapter: ListAdapter<Int, SplashScreenAdapter.SplashScreenViewHolder>(comparator) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SplashScreenViewHolder {
        return SplashScreenViewHolder.create(parent, viewType)
    }

    override fun onBindViewHolder(viewHolder: SplashScreenViewHolder, position: Int) {
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position]
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    class SplashScreenViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup, viewType: Int): SplashScreenViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(viewType, parent, false)

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