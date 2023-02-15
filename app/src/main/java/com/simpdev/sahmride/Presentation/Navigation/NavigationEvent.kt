package com.simpdev.sahmride.Presentation.Navigation

sealed class NavigationEvent(val route:String){
    object MainScreen : NavigationEvent("mainScreen")
    object RideScreen : NavigationEvent("rideScreen")
    object ProfileScreen : NavigationEvent("profileScreen")
    object ConfigurationScreen : NavigationEvent("configurationScreen")
}
