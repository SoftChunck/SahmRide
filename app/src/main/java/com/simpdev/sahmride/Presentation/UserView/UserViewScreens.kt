package com.simpdev.sahmride.Presentation.UserView

sealed class UserViewScreens
{
    object HomeScreen:UserViewScreens()
    object RideScreen:UserViewScreens()
    object ProfileScreen:UserViewScreens()
    object ConfigurationScreen:UserViewScreens()
}
