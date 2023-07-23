package com.dicoding.dicodingstoryapplication.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.dicodingstoryapplication.model.UserModel
import com.dicoding.dicodingstoryapplication.model.UserPreference

class MainViewModel(private val pref: UserPreference) : ViewModel() {

    // Get user from datastore
    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }
}