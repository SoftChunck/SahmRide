package com.simpdev.sahmride.Presentation.Ride

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RideViewModel: ViewModel()
{
    var state by mutableStateOf(RideState())

    fun onEvent(event: RideEvent){
        when(event){
            is RideEvent.changeCurrentRideScreen -> {
                    state = state.copy(currentRideScreen = event.rideScreen)
            }
            is RideEvent.driverProfile -> {
                state = state.copy(selectedDriverData = event.driverData, currentRideScreen = RideScreen.DriverProfileScreen)
            }
        }
    }
}