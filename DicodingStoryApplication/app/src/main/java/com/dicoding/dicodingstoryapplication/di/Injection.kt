package com.dicoding.dicodingstoryapplication.di

import com.dicoding.dicodingstoryapplication.api.ApiConfig
import com.dicoding.dicodingstoryapplication.data.StoryRepository

object Injection {
    fun provideRepository(): StoryRepository {
        val apiService = ApiConfig.getApiService()
        return StoryRepository(apiService)
    }
}