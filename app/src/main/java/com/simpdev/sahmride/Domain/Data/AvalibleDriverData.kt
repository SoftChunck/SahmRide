package com.simpdev.sahmride.Domain.Data

import com.mapbox.geojson.Point

data class AvalibleDriverData(
    var firstName:String? = null,
    var lastName:String? = null,
    var gender:String? = null,
    var rating:Double = 0.0,
    var driverUid:String? = null,
    var distance:String? = null,
    var duration:String? = null,
    var location: Point? = null,
    var waypoint:List<DistanceLocation>? = null
)
