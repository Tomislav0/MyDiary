package com.example.mydiary.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
@Parcelize
data class NoteAdapterModel(
    val id:String,
    val date:Date,
    val title:String,
    val content:String,
    val hasImage:Boolean,
    val hasVoiceRecording:Boolean,
    val imageBitmap:Bitmap
):Parcelable

