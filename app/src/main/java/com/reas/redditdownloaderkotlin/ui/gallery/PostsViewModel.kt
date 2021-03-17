package com.reas.redditdownloaderkotlin

import androidx.lifecycle.*
import com.reas.redditdownloaderkotlin.models.AllPosts
import com.reas.redditdownloaderkotlin.models.InstagramPosts
import com.reas.redditdownloaderkotlin.models.Posts
import com.reas.redditdownloaderkotlin.models.RedditPosts
import com.reas.redditdownloaderkotlin.repository.PostsRepository
import kotlinx.coroutines.launch

class PostsViewModel(private val repository: PostsRepository) : ViewModel() {
    val allPosts: LiveData<List<AllPosts>> = repository.allPosts.asLiveData()

    fun insert(post: Posts, redditPosts: RedditPosts) = viewModelScope.launch {
        repository.insert(post, redditPosts)
    }

    fun insert(post: Posts, instagramPosts: InstagramPosts) = viewModelScope.launch {
        repository.insert(post, instagramPosts)
    }
}

class PostsViewModelFactory(private val repository: PostsRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}