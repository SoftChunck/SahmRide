package com.simpdev.sahmride.Presentation.Profile

import android.net.Uri
import com.simpdev.sahmride.Domain.Data.AvalibleDriverData
import com.simpdev.sahmride.Domain.Data.ReviewData

data class ProfileState(
    val suggestion:AvalibleDriverData? = null,
    val profilePic: Uri? = null,
    val vehiclePic0:Uri? = null,
    val vehiclePic1:Uri? = null,
    val vehiclePic2:Uri? = null,
    val vehiclePic3:Uri? = null,
    val vehiclePic4:Uri? = null,
    val vehiclePic5:Uri? = null,
    val fetchingReviews:Boolean = false,
    val reviewList:MutableList<ReviewData> = emptyList<ReviewData>().toMutableList(),
)
