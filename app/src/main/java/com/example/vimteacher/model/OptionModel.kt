package com.example.vimteacher.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OptionModel(
    val optionId: Int = 0,
    val optionBody: String = "",
    val optionDescription: String = "",
) : Parcelable
