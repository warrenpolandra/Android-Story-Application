package com.dicoding.dicodingstoryapplication.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstoryapplication.di.Injection
import com.dicoding.dicodingstoryapplication.model.UserPreference
import com.dicoding.dicodingstoryapplication.view.addstory.AddStoryViewModel
import com.dicoding.dicodingstoryapplication.view.liststory.ListStoryViewModel
import com.dicoding.dicodingstoryapplication.view.login.LoginViewModel
import com.dicoding.dicodingstoryapplication.view.main.MainViewModel
import com.dicoding.dicodingstoryapplication.view.map.MapViewModel
import com.dicoding.dicodingstoryapplication.view.signup.SignUpViewModel

class ViewModelFactory(private val pref: UserPreference, private val context: Context) : ViewModelProvider.NewInstanceFactory() {

    // Suppress 'as T' casting
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                SignUpViewModel() as T
            }
            modelClass.isAssignableFrom(ListStoryViewModel::class.java) -> {
                ListStoryViewModel(pref, Injection.provideRepository()) as T
            }
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> {
                AddStoryViewModel(pref) as T
            }
            modelClass.isAssignableFrom(MapViewModel::class.java) -> {
                MapViewModel(pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}