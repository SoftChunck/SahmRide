package com.simpdev.sahmride.Domain

import androidx.compose.ui.graphics.ImageBitmap
import com.mapbox.geojson.Point

data class RideDetails(
    val userUid:String? = null,
    var firstName:String? = null,
    var lastName:String? = null,
    var gender:String? = null,
    val vehicleNumber:String? = null,
    var duration:String? = null,
    var distance:String? = null,
    val review:String? = null,
    val rating:Int? = null,
    var pickup: Point? = null,
    var destination: Point? = null,
    var userPic: ImageBitmap? = null,
    var distanceFromDriver:String? = null,
    var durationFromDriver:String? = null
)
