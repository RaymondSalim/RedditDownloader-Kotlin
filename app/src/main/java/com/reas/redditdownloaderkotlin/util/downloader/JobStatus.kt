package com.reas.redditdownloaderkotlin.util.downloader

enum class JobStatus {
    ADDED_TO_WORKMANAGER,
    START,
    GETTING_JSON_START,
    GETTING_JSON_END,
    MEDIA_DOWNLOAD_START,
    MEDIA_DOWNLOAD_END,
    PROCESSING_START,
    PROCESSING_END,
    SUCCESS,
    FAILED
}