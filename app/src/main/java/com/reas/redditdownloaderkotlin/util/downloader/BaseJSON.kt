package com.reas.redditdownloaderkotlin.util.downloader

import kotlinx.serialization.SerialName

abstract class BaseJSON {
    abstract var url: String
    abstract val createdAt: Long
    abstract val isNsfw: Boolean

}