package com.simpdev.sahmride.Presentation.UserView

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class UserViewViewModel:ViewModel() {
    var state by mutableStateOf(UserViewState())

    fun onEvent(event: UserViewEvents)
    {
        when(event)
        {
            is UserViewEvents.HomeClicked -> {
                state = state.copy(currentScreen = UserViewScreens.HomeScreen)
            }
            is UserViewEvents.RideClicked -> {
                state = state.copy(currentScreen = UserViewScreens.RideScreen)
            }
            is UserViewEvents.ProfileClicked -> {
                state = state.copy(currentScreen = UserViewScreens.ProfileScreen)
            }
            is UserViewEvents.ConfigurationClicked -> {
                state = state.copy(currentScreen = UserViewScreens.ConfigurationScreen)
            }
        }
    }
}