package com.example.mydiary.models

import java.util.*

data class NoteBM(
    val id:String,
    val date:String,
    val title:String,
    val content:String,
    val hasImage:Boolean,
    val hasVoiceRecording:Boolean,
    val moodRate : Int
)

