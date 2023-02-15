package com.simpdev.sahmride.Presentation.UserView

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simpdev.sahmride.Presentation.UserProfile.UserProfileUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserViewUi(){
    val viewModel = viewModel<UserViewViewModel>()
    val state = viewModel.state


    Scaffold(bottomBar = {
        NavigationBar {
            NavigationBarItem(
                selected = state.currentScreen == UserViewScreens.HomeScreen,
                onClick = {
                    viewModel.onEvent(UserViewEvents.HomeClicked)
                },
                icon = {
                    Icon(Icons.Filled.Home, contentDescription = "" )
                },
                label = { Text(text = "Home") }
            )
            NavigationBarItem(
                selected = state.currentScreen == UserViewScreens.RideScreen,
                onClick = {
                    viewModel.onEvent(UserViewEvents.RideClicked)
                },
                icon = {
                    Icon(Icons.Filled.TimeToLeave, contentDescription = "" )
                },
                label = { Text(text = "Ride") }
            )
            NavigationBarItem(
                selected = state.currentScreen == UserViewScreens.ProfileScreen,
                onClick = {
                    viewModel.onEvent(UserViewEvents.ProfileClicked)
                },
                icon = {
                    Icon(Icons.Filled.Person, contentDescription = "" )
                },
                label = { Text(text = "Profile") }
            )
            NavigationBarItem(
                selected = state.currentScreen == UserViewScreens.ConfigurationScreen,
                onClick = {
                    viewModel.onEvent(UserViewEvents.ConfigurationClicked)
                },
                icon = {
                    Icon(Icons.Filled.Settings, contentDescription = "" )
                },
                label = { Text(text = "Config") }
            )
        }
    }) {
        if(state.currentScreen == UserViewScreens.ProfileScreen)
            UserProfileUI(it)
    }
}