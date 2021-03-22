package com.reas.redditdownloaderkotlin.ui.share_intent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.ui.gallery.GalleryDialogFragment

class ShareIntentActivity : AppCompatActivity() {
    private val fragmentListener = object : GalleryDialogFragment.GalleryDialogFragmentListener {
        override fun onFragmentDetach() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_intent)



        val urlFromIntent = intent?.getStringExtra(Intent.EXTRA_TEXT)

        if (urlFromIntent != null) {
            GalleryDialogFragment.instance(url = urlFromIntent, listenerIn = fragmentListener)
                .show(supportFragmentManager, R.id.gallery_dialog_fragment.toString())
        } else {
            finish()
        }
    }
}