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

    private class PostsDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

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