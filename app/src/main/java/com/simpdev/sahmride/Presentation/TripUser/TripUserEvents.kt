package com.simpdev.sahmride.Presentation.Trip

sealed class TripUserEvents {
    object chatClicked:TripUserEvents()
    object tripHomeClicked:TripUserEvents()
}