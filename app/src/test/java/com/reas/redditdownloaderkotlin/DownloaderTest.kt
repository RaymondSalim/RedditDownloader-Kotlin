package com.reas.redditdownloaderkotlin

import com.reas.redditdownloaderkotlin.util.downloader.Downloader
import com.reas.redditdownloaderkotlin.util.downloader.reddit.RedditJson
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DownloaderTest {
    class MainTest {
        private val redditUrl = "https://www.reddit.com/r/yehhhhhh/comments/m2i1x9/"
        private val redditDownloader = Downloader()
            .setUrl(redditUrl)
        private val subreddit = redditUrl.substringAfter("/r/").substringBefore("/")

        // Ensures no exception
        @Test
        fun getJSON_reddit_isValid() {
            redditDownloader.getPageJson()
            assert(true)
        }

        @Test
        fun getSubreddit_isValid() {
            val redditJson = redditDownloader.getPageJson() as RedditJson
            assertThat(redditJson.subreddit).isEqualTo(subreddit)
        }

        @Test
        fun getUrl_reddit_isValid() {
            val redditJson = redditDownloader.getPageJson() as RedditJson
            assertThat(redditJson.url).isEqualTo("/r/yehhhhhh/comments/m2i1x9/test_1a_imgur_direct_iimgur/")
        }

        @Test
        fun getTitle_reddit_isValid() {
            val redditJson = redditDownloader.getPageJson() as RedditJson
            assertThat(redditJson.title).isEqualTo("Test 1a - Imgur Direct (i.imgur)")
        }

        @Test
        fun getAuthor_reddit_isValid() {
            val redditJson = redditDownloader.getPageJson() as RedditJson
            assertThat(redditJson.author).isEqualTo("yehhhhhh")
        }

        @Test
        fun getCreatedUTC_reddit_isValid() {
            val redditJson = redditDownloader.getPageJson() as RedditJson
            assertThat(redditJson.createdAt).isEqualTo(1615438029)
        }

        @Test
        fun getDomain_reddit_isValid() {
            val redditJson = redditDownloader.getPageJson() as RedditJson
            assertThat(redditJson.mediaDomain).isEqualTo("i.imgur.com")
        }

        @Test
        fun getMediaUrl_reddit_isValid() {
            val redditJson = redditDownloader.getPageJson() as RedditJson
            assertThat(redditJson.mediaUrl).isEqualTo("https://i.imgur.com/jxWYgnq.mp4")
        }

        @Test
        fun getIsGif_reddit_isFalse() {
            val redditJson = redditDownloader.getPageJson() as RedditJson
            assertThat(redditJson.isGif).isFalse()
        }

        @Test
        fun downloadTest() {
            redditDownloader.Test().testFuel("https://i.imgur.com/eW2UM7P.jpeg")
        }

        @Test
        fun downloadTest2() {
            redditDownloader.Test().testFuelDownload("https://i.imgur.com/eW2UM7P.jpeg")
        }
    }

    class HelperTest {
        private val redditUrl = "https://www.reddit.com/r/yehhhhhh/comments/m2i1x9/"
        private val redditDownloader = Downloader()
            .setUrl(redditUrl)

        /**
         * Reflection
         * https://medium.com/mindorks/how-to-unit-Test-private-methods-in-java-and-kotlin-d3cae49dccd
         */

        @Test
        fun removeQuery_test() {
            // Reflection
            val method = Downloader::class.java.getDeclaredMethod("removeQuery", String::class.java)
            method.isAccessible = true

            val result = method.invoke(redditDownloader, redditUrl + "?utm_source=share&utm_medium=web2x&context=3")
            assertThat(result).isEqualTo(redditUrl)
        }
    }
}