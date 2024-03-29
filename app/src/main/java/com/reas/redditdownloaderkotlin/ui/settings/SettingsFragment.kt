package com.reas.redditdownloaderkotlin.ui.settings

import android.app.Activity
import android.content.Intent
import android.content.SearchRecentSuggestionsProvider
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.reas.redditdownloaderkotlin.R
import com.reas.redditdownloaderkotlin.util.MediaScanner
import com.reas.redditdownloaderkotlin.util.search.SearchRecentProvider

private const val DIRECTORY_REQUEST_CODE = 1;

class SettingsFragment : SharedPreferences.OnSharedPreferenceChangeListener, PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this);

//        val downloadLocation = findPreference<Preference>("DOWNLOAD_LOCATION")
//        with(downloadLocation) {
//            this?.summary = this?.sharedPreferences?.getString("DOWNLOAD_LOCATION", "Not Set")
//
//            this?.setOnPreferenceClickListener {
//                val fileManagerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, context.getExternalFilesDir(null)?.canonicalPath)
//                }
//
//                startActivityForResult(fileManagerIntent, DIRECTORY_REQUEST_CODE)
//                true
//            }
//        }

        findPreference<Preference>("MEDIA_SCAN")?.let {
            it.setOnPreferenceClickListener {
                Toast.makeText(requireContext(), "Scanning media...", Toast.LENGTH_SHORT).show()
                MediaScanner(requireContext()).scanMedia()
                true
            }
        }

        findPreference<Preference>("CLEAR_RECENT_SEARCH")?.let {
            it.setOnPreferenceClickListener {
                SearchRecentSuggestions(requireContext(), SearchRecentProvider.AUTHORITY, SearchRecentProvider.MODE)
                    .clearHistory()
                true
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            DIRECTORY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val directory = data?.data?.path?.replace("/tree/primary", "/")?.replace(":", "")

                    val pref = findPreference<Preference>("DOWNLOAD_LOCATION")
                    with(pref) {
                        this!!.sharedPreferences.edit()
                            .putString("DOWNLOAD_LOCATION", directory).apply()

//                        this.callChangeListener(directory)
                    }



                }
            }
        }
    }

    override fun onSharedPreferenceChanged(pref: SharedPreferences, key: String?) {
        Log.d("Pref Changed", "Key: ${key}, Value: ${pref.all[key]}")

        when(key) {
            "PREFERRED_THEME" -> AppCompatDelegate.setDefaultNightMode(pref.getString(key, "-100")!!.toInt())

            "DOWNLOAD_LOCATION" -> {
                findPreference<Preference>(key)!!.summary = pref.getString(key, "Not Found")
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment GalleryFragment.
         */
        @JvmStatic
        val instance = SettingsFragment()
    }

}