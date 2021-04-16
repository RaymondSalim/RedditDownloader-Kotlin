package com.reas.redditdownloaderkotlin.database

import android.util.Log
import androidx.room.*
import com.reas.redditdownloaderkotlin.models.AllPosts
import com.reas.redditdownloaderkotlin.models.InstagramPosts
import com.reas.redditdownloaderkotlin.models.Posts
import com.reas.redditdownloaderkotlin.models.RedditPosts
import kotlinx.coroutines.flow.Flow

private const val TAG = "PostsDAO"

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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(posts: Posts)

    /**
     * Checks if an existing Posts with the same fileUri exists, ignore if it exists, inserts new if it does not
     */
    @Transaction
    fun insertOrIgnore(posts: Posts) {
        Log.d(TAG, "insertOrIgnore: ${posts.fileUri}")
        Log.d(TAG, "insertOrIgnore: ${countByUri(posts.fileUri)}")
        if (countByUri(posts.fileUri) == 0) {
            insert(posts)
        }
    }

    @Query("SELECT COUNT(*) FROM posts_table WHERE file_uri = :fileUri")
    fun countByUri(fileUri: String): Int

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

    @Query("DELETE FROM posts_table WHERE file_uri = :fileUri")
    suspend fun deletePostsWithURI(fileUri: String)

    @Query("DELETE FROM posts_table WHERE url = :url")
    suspend fun deletePostsWithUrl(url: String)

    @Query("DELETE FROM posts_reddit_table WHERE url = :url")
    suspend fun deleteRedditPostsWithUrl(url: String)

    @Query("DELETE FROM posts_instagram_table WHERE url = :url")
    suspend fun deleteInstagramPostsWithUrl(url: String)

    @Query("UPDATE posts_table SET is_favorite = :isFav WHERE url = :url")
    suspend fun setFavoriteWithUrl(isFav: Int, url: String)

    @Query("UPDATE posts_table SET is_favorite = :isFav WHERE file_uri = :fileUri")
    suspend fun setFavoriteWithURI(isFav: Int, fileUri: String)
}