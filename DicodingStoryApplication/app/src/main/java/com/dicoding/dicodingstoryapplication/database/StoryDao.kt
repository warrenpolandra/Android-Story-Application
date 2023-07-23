package com.dicoding.dicodingstoryapplication.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.dicodingstoryapplication.api.response.ListStoryItem

@Dao
interface StoryDao {


    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, ListStoryItem>
}