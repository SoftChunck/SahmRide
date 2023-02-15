package com.simpdev.sahmride.Presentation.UserView

sealed class UserViewEvents
{
    object HomeClicked:UserViewEvents()
    object RideClicked:UserViewEvents()
    object ProfileClicked:UserViewEvents()
    object ConfigurationClicked:UserViewEvents()
}
