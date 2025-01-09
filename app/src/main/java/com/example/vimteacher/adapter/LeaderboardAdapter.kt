package com.example.vimteacher.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vimteacher.databinding.LeaderboardItemLayoutBinding
import com.example.vimteacher.model.UserModel


class LeaderboardDiffCallback : DiffUtil.ItemCallback<UserModel>() {
    override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
        return oldItem == newItem
    }
}

class LeaderboardAdapter : ListAdapter<UserModel, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardDiffCallback()) {

    inner class LeaderboardViewHolder(private val binding: LeaderboardItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserModel, position: Int) {
            binding.apply {
                // Set ranking position
                rankTextView.text = (position + 1).toString()

                // Set username
                usernameTextView.text = user.email

                // Set number of questions solved
                questionsSolvedTextView.text = user.questions_solved.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        return LeaderboardViewHolder(
            LeaderboardItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}