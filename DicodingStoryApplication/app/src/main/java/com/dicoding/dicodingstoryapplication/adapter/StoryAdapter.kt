package com.dicoding.dicodingstoryapplication.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.dicodingstoryapplication.api.response.ListStoryItem
import com.dicoding.dicodingstoryapplication.databinding.ItemListStoryBinding
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dicoding.dicodingstoryapplication.view.detailstory.DetailStoryActivity

class StoryAdapter :
    PagingDataAdapter<ListStoryItem, StoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((ListStoryItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
            holder.itemView.setOnClickListener{
                onItemClick?.invoke(story)
            }
        }
    }

    class ViewHolder(private val viewBinding: ItemListStoryBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        private val storyImg: ImageView = viewBinding.ivItemPhoto
        fun bind(story: ListStoryItem) {
            val author = "Story by " + story.name
            val date = "Created on " + story.createdAt.take(10)
            viewBinding.tvItemName.text = author
            viewBinding.tvItemDate.text = date
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(storyImg)

            val optionCompat: ActivityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    itemView.context as Activity,
                    Pair(viewBinding.ivItemPhoto, "image")
                )
            itemView.setOnClickListener{
                val intent = Intent(itemView.context, DetailStoryActivity::class.java)
                itemView.context.startActivity(intent, optionCompat.toBundle())
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }
}