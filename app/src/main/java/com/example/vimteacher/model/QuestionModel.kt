package com.example.vimteacher.model

data class QuestionModel(
    val questionId: Int,
    val difficulty: DifficultyLevel,
    val options: List<OptionModel>,
    val correctOptionId: Int,
    val isDone: Boolean = false,
)