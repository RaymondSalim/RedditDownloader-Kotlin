package com.reas.redditdownloaderkotlin.ui.search

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cursoradapter.widget.CursorAdapter
import com.reas.redditdownloaderkotlin.R


private const val TAG = "SearchRecentAdapter"

class SearchRecentAdapter(context: Context?, cursor: Cursor?, flags: Int) :
    CursorAdapter(context, cursor, flags) {

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        Log.d(TAG, "newView: ")
        return LayoutInflater.from(context).inflate(R.layout.search_recent_items, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        Log.d(TAG, "bindView: ")
        view?.findViewById<TextView>(R.id.recent_text).apply {
            cursor?.let {
                this?.text = it.getString(it.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
                Log.d(TAG, "bindView: ${this?.text}")
            }
        }
    }

    fun getSuggestionText(position: Int): String? {
        if (position >= 0 && position < cursor.count) {
            val cursor = cursor
            cursor.moveToPosition(position)
            return cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
        }
        return null
    }
}