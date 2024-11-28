package com.example.vimteacher.model

data class QuestionModel(
    val questionTitle: String,
    val questionBody: String,
    val options: List<OptionModel> = listOf(),
    val correctOptionId: Int = 0,
)