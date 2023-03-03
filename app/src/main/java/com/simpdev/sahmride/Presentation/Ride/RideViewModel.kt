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
            is RideEvent.currentRideScreenChange -> {
                if (state.currentRideScreen == RideScreen.RideHome) {
                    state = state.copy(currentRideScreen = RideScreen.TripScreen)
                } else if (state.currentRideScreen == RideScreen.TripScreen) {
                    state = state.copy(currentRideScreen = RideScreen.RideHome)
                }
            }
        }
    }
}