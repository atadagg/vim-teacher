package com.example.vimteacher.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuestionModel(
    val questionId: Int = 0,
    val questionBody: String = "",
    val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val options: List<OptionModel> = emptyList(),
    val correctOptionId: Int = 0,
    val isDone: Boolean = false,
) : Parcelable