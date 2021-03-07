package com.reas.redditdownloaderkotlin

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Posts::class), version = 1, exportSchema = false)
public abstract class PostsRoomDatabase: RoomDatabase() {
    abstract fun postsDao(): PostsDAO

    // TODO Remove
    private class PostsDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    var postsDao = database.postsDao()

                    postsDao.deleteAll()

                    var post = Posts(
                        url = "reddit.com/r/aww/comments/kqkvlv/this_is_binx_binx_sat_like_this_the_whole_ride/",
                        postTitle = "This is Binx... Binx sat like this the whole ride home.",
                        postSubreddit = "r/aww",
                        postDate = 1609802756L,
                        postUser = "Serpent86",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqkvlv.jpg"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/aww/comments/kqenjf/i_asked_my_fiancé_to_make_our_cat_a_cheesy_80s/",
                        postTitle = "I asked my fiancé to make our cat a cheesy 80’s portrait. He delivered.",
                        postSubreddit = "r/aww",
                        postDate = 1609785151L,
                        postUser = "ICANHAZLOWERCASE",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqenjf.png"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/aww/comments/kqixau/do_you_like_rabbits/",
                        postTitle = "Do you like rabbits???",
                        postSubreddit = "r/aww",
                        postDate = 1609797126L,
                        postUser = "claudieta",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqixau.mp4"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/gifs/comments/kqdk42/browsing/",
                        postTitle = "Browsing",
                        postSubreddit = "r/gifs",
                        postDate = 1609782038L,
                        postUser = "jeandolly",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/gifs/comments/asd/browsing/",
                        postTitle = "Browsing",
                        postSubreddit = "r/gifs",
                        postDate = 1609782038L,
                        postUser = "jeandolly",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/gifs/comments/fds/browsing/",
                        postTitle = "Browsing",
                        postSubreddit = "r/gifs",
                        postDate = 1609782038L,
                        postUser = "jeandolly",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/gifs/comments/dsaf/browsing/",
                        postTitle = "Browsing",
                        postSubreddit = "r/gifs",
                        postDate = 1609782038L,
                        postUser = "jeandolly",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/gifs/comments/fewf/browsing/",
                        postTitle = "Browsing",
                        postSubreddit = "r/gifs",
                        postDate = 1609782038L,
                        postUser = "jeandolly",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/gifs/comments/qwr3r/browsing/",
                        postTitle = "Browsing",
                        postSubreddit = "r/gifs",
                        postDate = 1609782038L,
                        postUser = "jeandolly",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/gifs/comments/gtb/browsing/",
                        postTitle = "Browsing",
                        postSubreddit = "r/gifs",
                        postDate = 1609782038L,
                        postUser = "jeandolly",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/gifs/comments/a/browsing/",
                        postTitle = "Browsing",
                        postSubreddit = "r/gifs",
                        postDate = 1609782038L,
                        postUser = "jeandolly",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif"
                    )

                    postsDao.insert(post)

                    post = Posts(
                        url = "reddit.com/r/gifs/comments/erb/browsing/",
                        postTitle = "Browsing",
                        postSubreddit = "r/gifs",
                        postDate = 1609782038L,
                        postUser = "jeandolly",
                        filePath = "/storage/emulated/0/Android/data/com.reas.redditdownloaderkotlin/files/kqdk42.gif"
                    )


                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PostsRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): PostsRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        PostsRoomDatabase::class.java,
                        "posts_database"
                )
                    .addCallback(PostsDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}