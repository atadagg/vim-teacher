package com.example.vimteacher.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vimteacher.databinding.QuestionItemLayoutBinding
import com.example.vimteacher.model.QuestionModel

class QuestionAdapter : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> (){

    private var questions = listOf<QuestionModel>()

    inner class QuestionViewHolder(private val binding: QuestionItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root){
            fun bind(question: QuestionModel){
                binding.questionId.text = question.questionId.toString()
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
        holder.bind(questions[position])
    }

    override fun getItemCount() = questions.size


    fun submitList(newQuestions: List<QuestionModel>){
        questions = newQuestions
        notifyDataSetChanged()
    }




}