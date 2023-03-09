package com.simpdev.sahmride.Presentation.Ride

import com.simpdev.sahmride.Domain.Data.AvalibleDriverData

sealed class RideEvent{
    data class changeCurrentRideScreen(val rideScreen: RideScreen):RideEvent()
    data class driverProfile(val driverData: AvalibleDriverData):RideEvent()
}
