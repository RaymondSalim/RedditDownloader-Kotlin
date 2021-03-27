package com.reas.redditdownloaderkotlin

import androidx.lifecycle.*
import com.reas.redditdownloaderkotlin.models.AllPosts
import com.reas.redditdownloaderkotlin.models.InstagramPosts
import com.reas.redditdownloaderkotlin.models.Posts
import com.reas.redditdownloaderkotlin.models.RedditPosts
import com.reas.redditdownloaderkotlin.repository.PostsRepository
import kotlinx.coroutines.launch

class GalleryViewModel(private val repository: PostsRepository) : ViewModel() {
    private val _allPosts = repository.allPosts.asLiveData()
    val allPosts = _allPosts

    val recyclerViewSelectedPosts: MutableLiveData<MutableMap<Int, AllPosts>> by lazy {
        MutableLiveData<MutableMap<Int, AllPosts>>()
    }


    fun insert(post: Posts, redditPosts: RedditPosts) = viewModelScope.launch {
        repository.insert(post, redditPosts)
    }

    fun insert(post: Posts, instagramPosts: InstagramPosts) = viewModelScope.launch {
        repository.insert(post, instagramPosts)
    }
}

class GalleryViewModelFactory(private val repository: PostsRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GalleryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}