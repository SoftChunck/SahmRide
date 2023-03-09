package com.simpdev.sahmride.Domain.Data

import com.mapbox.geojson.Point

data class DistanceLocation(
    val locationPoint:Point,
    var distance:Double = 0.0
)
