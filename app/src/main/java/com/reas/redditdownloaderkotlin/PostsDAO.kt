package com.reas.redditdownloaderkotlin

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostsDAO {
    @Query("SELECT * FROM posts_table ORDER BY created_at DESC")
    fun getAll(): Flow<List<Posts>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: Posts)

    @Delete
    suspend fun delete(posts: Posts)

    @Query("DELETE FROM posts_table")
    suspend fun deleteAll()

    @Query("DELETE FROM posts_table WHERE url = :url")
    suspend fun deleteWithUrl(url: String)
}