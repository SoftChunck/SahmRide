package com.simpdev.sahmride.Presentation.Trip

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TripViewModel : ViewModel() {
    var state by mutableStateOf(TripState())

    fun onEvent(event:TripEvents){
        when(event)
        {
            is TripEvents.chatClicked -> {
                state = state.copy(currentScreen = TripScreen.ChatScreen)
            }
            is TripEvents.tripHomeClicked -> {
                state = state.copy(currentScreen = TripScreen.TripHome)
            }
        }
    }
}