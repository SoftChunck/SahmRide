package com.simpdev.sahmride.Presentation.Review

sealed class ReviewEvents{
    data class ratingChanged(val rating:Int):ReviewEvents()
    data class reviewChange(val review:String):ReviewEvents()
    data class  setUserUid(val userUid:String?):ReviewEvents()
    object submitReview:ReviewEvents()
}
