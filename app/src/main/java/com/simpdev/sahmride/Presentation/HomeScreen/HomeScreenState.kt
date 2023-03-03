package com.simpdev.sahmride.Presentation.HomeScreen

data class HomeScreenState(
    val active:Boolean = false,
    val isDriver:Boolean = false,
    val availableSeats:String = "0",
    val expandMenu:Boolean = false,
)
