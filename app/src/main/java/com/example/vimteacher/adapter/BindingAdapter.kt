package com.example.vimteacher.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vimteacher.model.QuestionModel

object BindingAdapters {
    @BindingAdapter("questionList")
    @JvmStatic
    fun setQuestionList(recyclerView: RecyclerView, questions: List<QuestionModel>?) {
        questions?.let {
            (recyclerView.adapter as? QuestionAdapter)?.submitList(it)
        }
    }
}