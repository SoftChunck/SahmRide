package com.simpdev.sahmride.Presentation.HomeScreen

sealed class HomeScreenEvents{
    object activeChange:HomeScreenEvents()
    object expandMenuChange:HomeScreenEvents()
    data class availableSeatsChange(val availableSeats:String):HomeScreenEvents()
}
