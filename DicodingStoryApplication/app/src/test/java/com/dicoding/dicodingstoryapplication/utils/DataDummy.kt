package com.dicoding.dicodingstoryapplication.utils

import com.dicoding.dicodingstoryapplication.api.response.ListStoryItem

object DataDummy {
    fun generateDummyStoryEntity(size: Int): List<ListStoryItem> {
        val storyList = ArrayList<ListStoryItem>()
        for (i in 1..size) {
            val story = ListStoryItem(
                "https://help.dicoding.com/wp-content/uploads/2021/01/dicoding-edit-1.jpg",
                "2022-05-08",
                "Nex",
                "This is a picture",
                113.9213,
                i.toString(),
                0.7893
            )
            storyList.add(story)
        }
        return storyList
    }
}