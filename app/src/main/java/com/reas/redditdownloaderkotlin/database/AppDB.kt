package com.reas.redditdownloaderkotlin.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.reas.redditdownloaderkotlin.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Posts::class, InstagramPosts::class, RedditPosts::class, Jobs::class], version = 1, exportSchema = false)
@TypeConverters(JobsConverter::class, PostsPlatformConverter::class)
abstract class AppDB: RoomDatabase() {
    abstract fun postsDao(): PostsDAO
    abstract fun jobsDao(): JobsDAO

    // TODO Remove
    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
//                scope.launch {
//                    var postsDao = database.postsDao()
//
//                    postsDao.deleteAll()
//
//                    var post = Posts(
//                        url = "reddit.com/r/aww/comments/kqkvlv/this_is_binx_binx_sat_like_this_the_whole_ride/",
//                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqkvlv.jpg",
//                        platform = PostsPlatform.REDDIT
//                    )
//                    var redditPosts = RedditPosts(
//                        url = "reddit.com/r/aww/comments/kqkvlv/this_is_binx_binx_sat_like_this_the_whole_ride/",
//                        title = "This is Binx... Binx sat like this the whole ride home.",
//                        subreddit = "r/aww",
//                        date = 1609802756L,
//                        author = "Serpent86",
//                    )
//
//                    postsDao.insert(post, redditPosts)
//
//                    post = Posts(
//                        url = "reddit.com/r/aww/comments/kqenjf/i_asked_my_fiancé_to_make_our_cat_a_cheesy_80s/",
//                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqenjf.png",
//                        platform = PostsPlatform.REDDIT
//                    )
//
//                    redditPosts = RedditPosts(
//                        url = "reddit.com/r/aww/comments/kqenjf/i_asked_my_fiancé_to_make_our_cat_a_cheesy_80s/",
//                        title = "I asked my fiancé to make our cat a cheesy 80’s portrait. He delivered.",
//                        subreddit = "r/aww",
//                        date = 1609785151L,
//                        author = "ICANHAZLOWERCASE",
//                    )
//
//                    postsDao.insert(post, redditPosts)
//
//                    post = Posts(
//                        url = "reddit.com/r/aww/comments/kqixau/do_you_like_rabbits/",
//                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqixau.mp4",
//                        platform = PostsPlatform.REDDIT
//                    )
//
//                    redditPosts = RedditPosts(
//                        url = "reddit.com/r/aww/comments/kqixau/do_you_like_rabbits/",
//                        title = "Do you like rabbits???",
//                        subreddit = "r/aww",
//                        date = 1609797126L,
//                        author = "claudieta",
//                    )
//
//                    postsDao.insert(post, redditPosts)
//
//                    post = Posts(
//                        url = "reddit.com/r/gifs/comments/kqdk42/browsing/",
//                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif",
//                        platform = PostsPlatform.REDDIT
//                    )
//
//                    redditPosts = RedditPosts(
//                        url = "reddit.com/r/gifs/comments/kqdk42/browsing/",
//                        title = "Browsing",
//                        subreddit = "r/gifs",
//                        date = 1609782038L,
//                        author = "jeandolly",
//                    )
//
//                    postsDao.insert(post, redditPosts)
//
//                    post = Posts(
//                        url = "reddit.com/r/gifs/comments/jorgijero/browsing/",
//                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif",
//                        platform = PostsPlatform.REDDIT
//                    )
//
//                    redditPosts = RedditPosts(
//                        url = "reddit.com/r/gifs/comments/jorgijero/browsing/",
//                        title = "Browsing",
//                        subreddit = "r/gifs",
//                        date = 1609782038L,
//                        author = "jeandolly",
//                    )
//
//                    postsDao.insert(post, redditPosts)
//
//                    post = Posts(
//                        url = "reddit.com/r/gifs/comments/wer/browsing/",
//                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif",
//                        platform = PostsPlatform.REDDIT
//                    )
//
//                    redditPosts = RedditPosts(
//                        url = "reddit.com/r/gifs/comments/wer/browsing/",
//                        title = "Browsing",
//                        subreddit = "r/gifs",
//                        date = 1609782038L,
//                        author = "jeandolly",
//                    )
//
//                    postsDao.insert(post, redditPosts)
//
//                    post = Posts(
//                        url = "reddit.com/r/gifs/comments/qww/browsing/",
//                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif",
//                        platform = PostsPlatform.REDDIT
//                    )
//
//                    redditPosts = RedditPosts(
//                        url = "reddit.com/r/gifs/comments/qww/browsing/",
//                        title = "Browsing",
//                        subreddit = "r/gifs",
//                        date = 1609782038L,
//                        author = "jeandolly",
//                    )
//
//                    postsDao.insert(post, redditPosts)
//
//                    post = Posts(
//                        url = "reddit.com/r/gifs/comments/dd/browsing/",
//                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif",
//                        platform = PostsPlatform.REDDIT
//                    )
//
//                    redditPosts = RedditPosts(
//                        url = "reddit.com/r/gifs/comments/dd/browsing/",
//                        title = "Browsing",
//                        subreddit = "r/gifs",
//                        date = 1609782038L,
//                        author = "jeandolly",
//                    )
//
//                    postsDao.insert(post, redditPosts)
//
//                    post = Posts(
//                        url = "reddit.com/r/gifs/comments/gr/browsing/",
//                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif",
//                        platform = PostsPlatform.REDDIT
//                    )
//
//                    redditPosts = RedditPosts(
//                        url = "reddit.com/r/gifs/comments/gr/browsing/",
//                        title = "Browsing",
//                        subreddit = "r/gifs",
//                        date = 1609782038L,
//                        author = "jeandolly",
//                    )
//
//                    postsDao.insert(post, redditPosts)
//
//
//                }
            }
        }
    }

    companion object {
        @Volatile
        var INSTANCE: AppDB? = null

        fun getDatabase(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "app_db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDB::class.java,
                        "app_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}