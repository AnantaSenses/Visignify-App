package com.bangkit.capstone.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LearnSign(
    var alphabet: String,
    val photo: Int
): Parcelable