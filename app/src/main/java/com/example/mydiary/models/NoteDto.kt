package com.example.mydiary.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
@Parcelize
data class NoteDto(
    val id:String,
    val date:Date,
    val title:String,
    val content:String,
    val hasImage:Boolean,
    val hasVoiceRecording:Boolean,
    val moodRate : Int,
):Parcelable

