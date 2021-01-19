package com.reas.redditdownloaderkotlin

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class PostsViewModel(private val repository: PostsRepository) : ViewModel() {
    val allPosts: LiveData<List<Posts>> = repository.allPosts.asLiveData()

    fun insert(post: Posts) = viewModelScope.launch {
        repository.insert(post)
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