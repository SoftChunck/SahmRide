package com.simpdev.sahmride.Domain

import com.mapbox.geojson.Point
import com.simpdev.sahmride.Presentation.Navigation.userInfo

data class RideDetails(
    var UserInfo: userInfo = userInfo(),
//    val userUid:String? = null,
//    var firstName:String? = null,
//    var lastName:String? = null,
//    var gender:String? = null,
    val vehicleNumber:String? = null,
    var duration:String? = null,
    var distance:String? = null,
    val review:String? = null,
    val rating:Int? = null,
    var pickup: Point? = null,
    var destination: Point? = null,
    var price: Int = 0,
//    var userPic: ImageBitmap? = null,
    var distanceFromDriver:String? = null,
    var durationFromDriver:String? = null,
    var request:String? = null
)
