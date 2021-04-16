package com.reas.redditdownloaderkotlin.util.search

import android.content.SearchRecentSuggestionsProvider

class SearchRecentProvider: SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        val AUTHORITY = "com.reas.redditdownloaderkotlin.util.search.SearchRecentProvider"
        val MODE = DATABASE_MODE_QUERIES
    }
}