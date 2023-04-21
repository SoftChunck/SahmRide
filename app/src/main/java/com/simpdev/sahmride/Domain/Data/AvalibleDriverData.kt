package com.simpdev.sahmride.Domain.Data

import com.mapbox.geojson.Point

data class AvalibleDriverData(
    var firstName:String? = null,
    var lastName:String? = null,
    var gender:String? = null,
    var rating:Double = 0.0,
    var vehicleName:String? = null,
    var vehicleModel:String? = null,
    var driverUid:String? = null,
    var distance:String? = null,
    var duration:String? = null,
    var location: Point? = null,
    var waypoint:List<DistanceLocation>? = null,
    var otherUsers:UserPrice? = null,
    var price:Int = 0,
    var carType:Int = 1,
    var additionlDistance:Double = 0.0
)
