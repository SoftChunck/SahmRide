package com.simpdev.sahmride.Domain.Data

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap

data class UserData(
    var firstName:String? = null,
    var lastName:String? = null,
    var email:String? = null,
    var gender:String? = null,
    var active:Boolean = false,
    var profilePic: Uri? = null,
    var isDriver: Boolean = false,
    var profilePicBitmap: ImageBitmap? = null
)