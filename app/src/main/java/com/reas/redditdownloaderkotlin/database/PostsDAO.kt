package com.reas.redditdownloaderkotlin.database

import androidx.room.*
import com.reas.redditdownloaderkotlin.models.AllPosts
import com.reas.redditdownloaderkotlin.models.InstagramPosts
import com.reas.redditdownloaderkotlin.models.Posts
import com.reas.redditdownloaderkotlin.models.RedditPosts
import kotlinx.coroutines.flow.Flow

@Dao
interface PostsDAO {
    @Transaction
    @Query("SELECT * FROM posts_table ORDER BY downloaded_at DESC")
    fun getAll(): Flow<List<AllPosts>>

    suspend fun deleteAll() {
        deletePosts()
        deleteRedditPosts()
        deleteInstagramPosts()
    }

    suspend fun deleteWithUrl(url: String) {
        deletePostsWithUrl(url)
        deleteRedditPostsWithUrl(url)
        deleteInstagramPostsWithUrl(url)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: Posts, redditPosts: RedditPosts)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: Posts, instagramPosts: InstagramPosts)

    @Delete
    suspend fun delete(posts: Posts, redditPosts: RedditPosts)

    @Delete
    suspend fun delete(posts: Posts, instagramPosts: InstagramPosts)

    @Query("DELETE FROM posts_table")
    suspend fun deletePosts()

    @Query("DELETE FROM posts_reddit_table")
    suspend fun deleteRedditPosts()

    @Query("DELETE FROM posts_instagram_table")
    suspend fun deleteInstagramPosts()

    @Query("DELETE FROM posts_table WHERE url = :url")
    suspend fun deletePostsWithUrl(url: String)

    @Query("DELETE FROM posts_reddit_table WHERE url = :url")
    suspend fun deleteRedditPostsWithUrl(url: String)

    @Query("DELETE FROM posts_instagram_table WHERE url = :url")
    suspend fun deleteInstagramPostsWithUrl(url: String)


}