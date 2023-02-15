package com.simpdev.sahmride

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.UserProfile.UserProfileUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(context: Context, locationCallback: LocationListeningCallback)
{
    val viewModel = viewModel<NavigationViewModel>()
    val state = viewModel.state
    LaunchedEffect(key1 = 0){
        BroadcastLocation(context,locationCallback)
        if(!state.loadedProfilePic && !state.loadedUserData)
        {
            viewModel.loadUserData()
            viewModel.loadProfilePic()
        }
    }
    val navController = rememberNavController()
//    var aV = LottieAnimationView(context)
//    aV.setAnimationFromUrl("https://assets4.lottiefiles.com/packages/lf20_x62chJ.json")
//    aV.playAnimation()
//    aV.repeatCount = 999
    if(!state.loadedProfilePic && !state.loadedUserData)
    {
        Column(modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box( ){
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(id = R.drawable.sahmlogo),
                        contentDescription = "",
                    )
                    CircularProgressIndicator()
                }
//                AndroidView(

//                    modifier = Modifier
//                        .fillMaxSize()
//                    ,
//                    factory = {
//                        aV
//                    })
            }
        }
    }
    else{
        Scaffold(bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == NavigationEvent.MainScreen.route } == true,
                    onClick = {
                        navController.navigate(NavigationEvent.MainScreen.route)
                    },
                    icon = {
                        Icon(Icons.Filled.Home, contentDescription = "" )
                    },
                    label = { Text(text = "Home") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == NavigationEvent.RideScreen.route } == true,
                    onClick = {
                        navController.navigate(NavigationEvent.RideScreen.route)
                    },
                    icon = {
                        Icon(Icons.Filled.TimeToLeave, contentDescription = "" )
                    },
                    label = { Text(text = "Ride") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == NavigationEvent.ProfileScreen.route } == true,
                    onClick = {
                        navController.navigate(NavigationEvent.ProfileScreen.route)
                    },
                    icon = {
                        Icon(Icons.Filled.Person, contentDescription = "" )
                    },
                    label = { Text(text = "Profile") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == NavigationEvent.ConfigurationScreen.route } == true,
                    onClick = {
                        navController.navigate(NavigationEvent.ConfigurationScreen.route)
                    },
                    icon = {
                        Icon(Icons.Filled.Settings, contentDescription = "" )
                    },
                    label = { Text(text = "Config") }
                )
            }
        }) {
            val innerPadding = it
            NavHost(navController = navController, startDestination = NavigationEvent.MainScreen.route ){
                composable(route = NavigationEvent.MainScreen.route){
                    HomeFragment(navController)
                }
                composable(route = NavigationEvent.RideScreen.route){
                    Pickup(context,
                        defaultSelectedIndex = if(pickupLocation == null) 1 else if(destinationLocation == null) 2 else 3 )
                }
                composable(route = NavigationEvent.ProfileScreen.route){
                    UserProfileUI(innerPadding = innerPadding)
                }
                composable(route = NavigationEvent.ConfigurationScreen.route){

                }
            }
        }
    }
}