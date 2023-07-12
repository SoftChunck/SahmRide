package com.simpdev.sahmride.Domain.Data

import android.net.Uri

data class UserPrice(
    val userUid:String,
    var price: Int = 0,
    var firstName:String? = null,
    var lastName:String? = null,
    var gender:String? = null,
    var rating:Double = 0.0,
    var profilePic: Uri? = null
)
