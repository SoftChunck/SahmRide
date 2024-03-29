package com.simpdev.sahmride.Presentation.Navigation

sealed class NavigationEvent(val route:String){
    object MainScreen : NavigationEvent("mainScreen")
    object RideScreen : NavigationEvent("rideScreen")
    object ProfileScreen : NavigationEvent("profileScreen")
    object ConfigurationScreen : NavigationEvent("configurationScreen")
    object WalletScreen:NavigationEvent("walletScreen")
    data class userUidChange(val uid:String):NavigationEvent("")
    data class userAccepted(val distance:String?):NavigationEvent("")
    object userRejected:NavigationEvent("")
    object rideCancelled:NavigationEvent("")
    object rideCompleted:NavigationEvent("")
    data class changeCurrentScreen(val screen:NavigationScreen):NavigationEvent("")
}
