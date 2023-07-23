package com.dicoding.dicodingstoryapplication.view.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.dicodingstoryapplication.api.ApiConfig
import com.dicoding.dicodingstoryapplication.api.response.ListStoriesResponse
import com.dicoding.dicodingstoryapplication.api.response.ListStoryItem
import com.dicoding.dicodingstoryapplication.model.UserModel
import com.dicoding.dicodingstoryapplication.model.UserPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewModel(private val pref: UserPreference): ViewModel() {

    private val _location = MutableLiveData<List<ListStoryItem>>()
    val location: MutableLiveData<List<ListStoryItem>> = _location

    // Get all stories
    fun getAllStories(token: String) {
        val client = ApiConfig.getApiService().getAllStoriesWithLocation(token, 1)
        client.enqueue(object : Callback<ListStoriesResponse> {
            override fun onResponse(call: Call<ListStoriesResponse>, response: Response<ListStoriesResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _location.value = responseBody.listStory
                    }
                } else {
                    onFailureLog(response.message())
                }
            }

            override fun onFailure(call: Call<ListStoriesResponse>, t: Throwable) {
                onFailureLog(t.message)
            }
        })
    }

    // Show failure
    private fun onFailureLog(message: String?){
        Log.e(TAG, "onFailure: $message")
    }

    // Get user from datastore
    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    // Companion object
    companion object {
        private const val TAG = "MapViewModel"
    }
}