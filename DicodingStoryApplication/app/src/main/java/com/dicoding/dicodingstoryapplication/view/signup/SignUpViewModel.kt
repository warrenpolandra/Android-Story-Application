package com.dicoding.dicodingstoryapplication.view.signup

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.dicodingstoryapplication.api.ApiConfig.getApiService
import com.dicoding.dicodingstoryapplication.api.response.BasicResponse
import com.dicoding.dicodingstoryapplication.model.UserRegisterModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpViewModel: ViewModel() {

    private val _finishActivity = MutableLiveData<Unit>()
    val finishActivity: LiveData<Unit> = _finishActivity

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // User signup
    fun signupUser(user: UserRegisterModel, context: Context) {
        _isLoading.value = true
        val client = getApiService().registerUser(user)
        client.enqueue(object : Callback<BasicResponse> {
            override fun onResponse(
                call: Call<BasicResponse>,
                response: Response<BasicResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        showResultDialog("Account Created", "Your account has been created. Login now to use this app!", context)
                    }
                } else {
                    showResultDialog("Signup Failed", response.message(), context)
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                _isLoading.value = false
                showResultDialog("Signup Failed", t.message, context)
            }
        })
    }

    // Show AlertDialog
    private fun showResultDialog(title: String, message: String?, context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("Okay") { _, _ ->
                _finishActivity.value = Unit
            }
            setCancelable(false)
            create()
            show()
        }
    }
}