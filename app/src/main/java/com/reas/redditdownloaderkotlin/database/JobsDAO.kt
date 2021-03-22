package com.reas.redditdownloaderkotlin.database

import androidx.room.*
import com.reas.redditdownloaderkotlin.models.*
import com.reas.redditdownloaderkotlin.util.downloader.JobStatus

@Dao
interface JobsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(jobs: Jobs): Long

    @Delete
    fun delete(jobs: Jobs)

    @Query("DELETE FROM posts_table WHERE url = :url")
    fun deleteJobsWithUrl(url: String)

    @Query("UPDATE jobs_table SET status = :jobStatus WHERE url = :url")
    fun updateJobStatus(jobStatus: JobStatus, url: String)

    @Query("UPDATE jobs_table SET progress_percentage = :progress WHERE url = :url")
    fun updateProgress(progress: Float, url: String)
}