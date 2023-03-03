package com.simpdev.sahmride.Presentation.Navigation

sealed class NavigationEvent(val route:String){
    object MainScreen : NavigationEvent("mainScreen")
    object RideScreen : NavigationEvent("rideScreen")
    object ProfileScreen : NavigationEvent("profileScreen")
    object ConfigurationScreen : NavigationEvent("configurationScreen")
    data class userUidChange(val uid:String):NavigationEvent("")
    object userAccepted:NavigationEvent("")
    object userRejected:NavigationEvent("")
    object rideCancelled:NavigationEvent("")
    data class changeCurrentScreen(val screen:NavigationScreen):NavigationEvent("")
}
