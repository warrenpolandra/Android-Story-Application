package com.dicoding.dicodingstoryapplication.view.liststory

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.dicodingstoryapplication.api.response.ListStoryItem
import com.dicoding.dicodingstoryapplication.data.StoryRepository
import com.dicoding.dicodingstoryapplication.model.UserModel
import com.dicoding.dicodingstoryapplication.model.UserPreference
import kotlinx.coroutines.launch

class ListStoryViewModel(private val pref: UserPreference?, private val storyRepository: StoryRepository) : ViewModel(){

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Set token to repository
    fun setToken(token: String){
        storyRepository.setToken(token)
    }

    // Get all stories With Paging
    fun getAllStories(): LiveData<PagingData<ListStoryItem>> =
        storyRepository.getStory().cachedIn(viewModelScope)

    // Get user from datastore
    fun getUser(): LiveData<UserModel>? {
        return pref?.getUser()?.asLiveData()
    }

    // User logout
    fun logout() {
        viewModelScope.launch {
            pref?.logout()
        }
    }
}