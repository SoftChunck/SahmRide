package com.simpdev.sahmride

sealed class MainScreenEvents{
    data class ChangeScreen(val currentScreen: CurrentScreen):MainScreenEvents()
}
