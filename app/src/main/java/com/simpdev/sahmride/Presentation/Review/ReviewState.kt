package com.simpdev.sahmride.Presentation.Review

data class ReviewState(
    val userUid:String? = null,
    val rating:Int = 3,
    val review:String = "",
    val reviewSubmitted:Boolean = false
)
