package com.dicoding.dicodingstoryapplication.api

import android.location.Location
import com.dicoding.dicodingstoryapplication.api.response.BasicResponse
import com.dicoding.dicodingstoryapplication.api.response.ListStoriesResponse
import com.dicoding.dicodingstoryapplication.api.response.LoginResponse
import com.dicoding.dicodingstoryapplication.model.UserLoginModel
import com.dicoding.dicodingstoryapplication.model.UserRegisterModel
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    // Register New User
    @POST("register")
    fun registerUser(
        @Body user: UserRegisterModel
    ): Call<BasicResponse>

    // User Login
    @POST("login")
    fun loginUser(
        @Body user: UserLoginModel
    ): Call<LoginResponse>

    // Get All Stories with location
    @GET("stories")
    fun getAllStoriesWithLocation(
        @Header("Authorization") token: String,
        @Query("location") location: Int
    ): Call<ListStoriesResponse>

    // Get All Stories With Paging
    @GET("stories")
    suspend fun getAllStoriesWithPaging(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): ListStoriesResponse

    // Add New Story Without Location
    @Multipart
    @POST("stories")
    fun uploadStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: String,
    ): Call<BasicResponse>

    // Add New Story With Location
    @Multipart
    @POST("stories")
    fun uploadStoryWithLocation(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: String,
        @Part("lat") lat: Float,
        @Part("lon") lon: Float
    ): Call<BasicResponse>
}