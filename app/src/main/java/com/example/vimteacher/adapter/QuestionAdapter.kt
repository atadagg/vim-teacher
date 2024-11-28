package com.example.vimteacher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vimteacher.R
import com.example.vimteacher.model.QuestionModel

class QuestionAdapter(
    private val questions: List<QuestionModel>,
    private val onQuestionClick: (QuestionModel) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.questionTitle)
        private val bodyTextView: TextView = itemView.findViewById(R.id.questionBody)

        fun bind(question: QuestionModel, onQuestionClick: (QuestionModel) -> Unit) {
            titleTextView.text = question.questionTitle
            bodyTextView.text = question.questionBody
            itemView.setOnClickListener { onQuestionClick(question) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], onQuestionClick)
    }

    override fun getItemCount() = questions.size
}