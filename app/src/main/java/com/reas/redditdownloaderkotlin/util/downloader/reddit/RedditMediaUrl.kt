package com.reas.redditdownloaderkotlin.util.downloader.reddit

import org.json.JSONObject

class RedditMediaUrl(private val postData: JSONObject) {
    fun getUrl(): String {
        val domain = postData.getString("domain")

        with(domain) {
            when {
                // https://www.reddit.com/m1eqw0
                // https://www.reddit.com/m1iwhx
                // https://www.reddit.com/m1hwmb
                contains("v.redd.it", true) -> {
                    return@getUrl getVRedditMediaUrl()
                }

                // https://www.reddit.com/m1gfo4
                contains("i.redd.it", true) -> {
                    return@getUrl getUrlOverridenByDest()
                }

                contains("giphy") -> {
                    TODO("Not yet implemented")
                }

                // https://www.reddit.com/m1fue7
//              // https://www.reddit.com/m1arv6
                contains("gfycat.com", true) -> {
                    return@getUrl getGfycatMediaUrl()
                }

                // TODO Add support for "imgur.com" https://www.reddit.com/m1if4y
                contains("i.imgur.com", true) -> {
                    return@getUrl getUrlOverridenByDest()
                }
                else -> {}
            }
        }
        throw Exception("Failed to get media URL")
    }

    fun isGif(): Boolean {
        val domain = postData.getString("domain")
        if (!domain.equals("v.redd.it")) return false

        return postData.getJSONObject("secure_media").getJSONObject("reddit_video").getBoolean("is_gif");
    }

    private fun getVRedditMediaUrl(): String {
        return postData.getJSONObject("secure_media").getJSONObject("reddit_video").getString("fallback_url")
    }

    private fun getUrlOverridenByDest(): String {
        return postData.getString("url_overridden_by_dest")
    }

    private fun getGfycatMediaUrl(): String {
        val url = postData.getJSONObject("secure_media").getJSONObject("oembed").getString("thumbnail_url")
        val regex = """gfycat\.com[/][a-z]+""".toRegex(RegexOption.IGNORE_CASE)
        return "https://giant.${url.replace(regex, "")}.mp4"
    }
}