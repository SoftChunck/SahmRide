package com.simpdev.sahmride.Domain.Data

import android.net.Uri

data class ReviewData(
    val byUid:String? = null,
    val review:String? = null,
    val rating:Int = 0,
    var firstName:String? = null,
    var lastName:String? = null,
    var profilePic: Uri? = null
)
