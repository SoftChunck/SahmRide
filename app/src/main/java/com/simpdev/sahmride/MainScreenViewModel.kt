package com.simpdev.sahmride

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainScreenViewModel:ViewModel() {
    var state  by mutableStateOf(MainScreenState(currentScreen = CurrentScreen.StartingPage))
    fun onEvent(event:MainScreenEvents){
        when(event){
            is MainScreenEvents.ChangeScreen -> {
                state = state.copy(currentScreen = event.currentScreen)
            }
        }
    }
}