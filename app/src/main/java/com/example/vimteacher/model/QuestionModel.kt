package com.example.vimteacher.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuestionModel(
    val questionId: Int,
    val difficulty: DifficultyLevel,
    val options: List<OptionModel>,
    val correctOptionId: Int,
    val isDone: Boolean = false,
) : Parcelable