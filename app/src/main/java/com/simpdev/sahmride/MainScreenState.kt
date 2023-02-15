package com.simpdev.sahmride

data class MainScreenState(
  val currentScreen:CurrentScreen
)

sealed class CurrentScreen{
  object HomeScreen:CurrentScreen()
  object SignIn:CurrentScreen()
  object SignUp:CurrentScreen()
  object ForgotPassword:CurrentScreen()
}