package com.simpdev.sahmride.Presentation.HomeScreen

import com.simpdev.sahmride.Domain.Data.RideHistory

data class HomeScreenState(
    val rideSharing:Boolean = false,
    val active:Boolean = false,
    val isDriver:Boolean = false,
    val availableSeats:String = "0",
    val expandMenu:Boolean = false,
    var historyList: MutableList<RideHistory> = emptyList<RideHistory>().toMutableList(),
    val fetchingRideDetails:Boolean = false,
)
