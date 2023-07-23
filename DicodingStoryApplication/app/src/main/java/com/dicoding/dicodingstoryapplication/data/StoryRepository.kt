package com.dicoding.dicodingstoryapplication.data

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.dicodingstoryapplication.api.ApiService
import com.dicoding.dicodingstoryapplication.api.response.ListStoryItem

class StoryRepository(private val apiService: ApiService) {

    private var token = ""

    fun setToken(token: String) {
        this.token = token
    }

    fun getStory(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, token)
            }
        ).liveData
    }
}