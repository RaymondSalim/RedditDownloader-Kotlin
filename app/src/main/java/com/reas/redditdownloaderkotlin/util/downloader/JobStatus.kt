package com.reas.redditdownloaderkotlin.util.downloader

enum class JobStatus {
    ADDED_TO_WORKMANAGER,
    FAILED,
    GETTING_JSON_START,
    GETTING_JSON_END,
    MEDIA_DOWNLOAD_START,
    MEDIA_DOWNLOAD_END,
    SUCCESS
}