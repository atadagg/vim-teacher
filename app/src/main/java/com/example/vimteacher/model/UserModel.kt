package com.example.vimteacher.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserModel(
    val uid: String,
    val email: String,
    val questions_solved: Int = 0,
) : Parcelable
