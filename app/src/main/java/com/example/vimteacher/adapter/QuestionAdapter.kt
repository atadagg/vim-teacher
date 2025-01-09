package com.example.vimteacher.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vimteacher.R
import com.example.vimteacher.databinding.QuestionItemLayoutBinding
import com.example.vimteacher.model.QuestionModel

class QuestionDiffCallback : DiffUtil.ItemCallback<QuestionModel>() {
    override fun areItemsTheSame(oldItem: QuestionModel, newItem: QuestionModel): Boolean {
        return oldItem.questionId == newItem.questionId
    }

    override fun areContentsTheSame(oldItem: QuestionModel, newItem: QuestionModel): Boolean {
        return oldItem == newItem
    }
}

class QuestionAdapter( private val onItemClick: (QuestionModel) -> Unit, private var solvedQuestionIds: Set<Int> = emptySet() ) : ListAdapter<QuestionModel, QuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()){

    fun setSolvedQuestions(newSolvedIds: Set<Int>) {
        solvedQuestionIds = newSolvedIds
        notifyDataSetChanged()
    }

    inner class QuestionViewHolder(private val binding: QuestionItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(question: QuestionModel) {
            binding.questionId.text = question.questionId.toString()
            binding.root.setCardBackgroundColor(
                binding.root.context.getColor(
                    if (solvedQuestionIds.contains(question.questionId)) {
                        R.color.question_solved
                    } else {
                        R.color.question_unsolved
                    }
                )
            )

            itemView.setOnClickListener {
                onItemClick(question)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        return QuestionViewHolder(
            QuestionItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}