package com.dicoding.dicodingstoryapplication.view.login

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import com.dicoding.dicodingstoryapplication.api.ApiConfig.getApiService
import com.dicoding.dicodingstoryapplication.api.response.LoginResponse
import com.dicoding.dicodingstoryapplication.model.UserLoginModel
import com.dicoding.dicodingstoryapplication.model.UserModel
import com.dicoding.dicodingstoryapplication.model.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val pref: UserPreference) : ViewModel(){

    private val _finishActivity = MutableLiveData<Boolean>()
    val finishActivity: LiveData<Boolean> = _finishActivity

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // User login
    fun loginUser(user: UserLoginModel, context: Context) {
        _isLoading.value = true
        val client = getApiService().loginUser(user)
        client.enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        responseBody.loginResult?.let { login(it.token) }
                        showResultDialog("Login Success!", "Login success! Now you can start viewing other people's story and post yours. Enjoy this app!", "Let\'s go!", context)
                    }
                } else {
                    showResultDialog("Login Failed", response.message(), "Okay", context)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                showResultDialog("Login Failed", t.message, "Okay", context)
            }
        })
    }

    // Show AlertDialog
    private fun showResultDialog(title: String, message: String?, posButton: String, context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(posButton) { _, _ ->
                if (posButton != "Okay"){
                    _finishActivity.value = true
                }
            }
            setCancelable(false)
            create()
            show()
        }
    }

    // Get user from datastore
    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    // Login to datastore
    fun login(token: String) {
        viewModelScope.launch {
            pref.login(token)
        }
    }
}