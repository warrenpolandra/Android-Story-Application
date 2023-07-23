package com.dicoding.dicodingstoryapplication.view.addstory

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.dicodingstoryapplication.api.ApiConfig.getApiService
import com.dicoding.dicodingstoryapplication.api.response.BasicResponse
import com.dicoding.dicodingstoryapplication.model.UserModel
import com.dicoding.dicodingstoryapplication.model.UserPreference
import com.google.android.gms.location.FusedLocationProviderClient
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddStoryViewModel(private val pref: UserPreference): ViewModel() {

    private val _finishActivity = MutableLiveData<Boolean>()
    val finishActivity: LiveData<Boolean> = _finishActivity

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    // Upload Story
    fun uploadStory(token: String, context: Context, imageMultipart: MultipartBody.Part, description: String, lat: Float?, lon: Float?) {
        _isLoading.value = true
        val client = if (lat != null && lon != null)
            getApiService().uploadStoryWithLocation(token, imageMultipart, description, lat, lon)
        else getApiService().uploadStory(token, imageMultipart, description)
        client.enqueue(object : Callback<BasicResponse> {

            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        showResultDialog("Upload Success!", "Your story has been posted! Go to the main page to view your story.", "Let\'s go!", context)
                    }
                } else {
                    showResultDialog("Upload Failed", response.message(), "Okay", context)
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                _isLoading.value = false
                showResultDialog("Upload Failed", t.message, "Okay", context)
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

    // Check location permission
    private fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Get current location
    fun getLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        if (checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _location.value = location
                }
            }
        }
    }


    // Get user from datastore
    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }
}