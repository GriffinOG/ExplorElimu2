package com.example.explorelimu.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Model(val id: Int, val name: String, val fileName: String, val fileSize: Long, val description: String): Parcelable {
}