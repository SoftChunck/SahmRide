package com.simpdev.sahmride

import Domain.Data.*
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.simpdev.sahmride.Presentation.Config.ConfigUi
import com.simpdev.sahmride.Presentation.HomeScreen.HomeScreenUi
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.Presentation.Navigation.NavigationScreen
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.Trip.TripUi
import com.simpdev.sahmride.Presentation.TripUser.TripUser
import com.simpdev.sahmride.Presentation.UserProfile.UserProfileUI
import com.simpdev.sahmride.Presentation.Wallet.WalletUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    context: Context,
)
{
    val viewModel = viewModel<NavigationViewModel>()
    val state = viewModel.state
    LaunchedEffect(key1 = 0){
        if(!state.loadedProfilePic && !state.loadedUserData)
        {
            viewModel.loadUserData()
            viewModel.loadProfilePic()
        }
    }
    val navController = rememberNavController()

    if(state.rideSharingRideDetails.size > 0 && state.rideSharingRideDetails.all { it.request == "accpted" })
    {
        viewModel.onEvent(NavigationEvent.changeCurrentScreen(NavigationScreen.RideAcceptedScreen))
    }
    when(state.currentScreen){
        is NavigationScreen.WaitForRequest -> {
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
                            modifier = Modifier.padding(horizontal = 50.dp, vertical = 20.dp),
                            painter = painterResource(id = R.drawable.logov),
                            contentDescription = "",
                        )
                        CircularProgressIndicator()
                        Button(
                            modifier = Modifier
                                .padding(top = 20.dp),
                            onClick = {
                            db.collection("ridesDetail").document(auth.currentUser!!.uid)
                                .update("request","cancelled").addOnSuccessListener {
                                    Log.d("request","cancelled")
                                }
                            database.reference.child("driversLocation").child(state.userUid!!).child("users").child(auth.currentUser!!.uid).setValue(null)
                            viewModel.onEvent(NavigationEvent.changeCurrentScreen(NavigationScreen.LoadingScreen))
                        }) {
                            Text("Cancel Request")
                        }
                    }
                }
            }
        }
        is NavigationScreen.LoadingScreen -> {
            if(!state.loadedProfilePic && !state.loadedUserData && state.rideStatus == null)
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
                                modifier = Modifier.padding(horizontal = 50.dp, vertical = 20.dp),
                                painter = painterResource(id = R.drawable.logov),
                                contentDescription = "",
                            )
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            else{
                viewModel.onEvent(NavigationEvent.changeCurrentScreen(NavigationScreen.AppMainScreen))
            }
        }
        is NavigationScreen.RideAcceptedScreen -> {
            Log.d("TripDriver","Driver Accepted")
            TripUi(
                context = context, pickup = state.pickup, destination = state.destination,
                listOf<String>(state.userUid!!),
                userType = "Driver",
                usersInfo = state.rideSharingRideDetails,
                navigationViewModel = viewModel
            )
        }
        is NavigationScreen.UserAcceptedScreen -> {
            Log.d("TripUser","User Accepted")
            TripUser(context = context,pickup = state.pickup,destination = state.destination,
                listOf<String>(state.userUid!!),
                userType = "User",
                usersInfo = state.rideSharingUsers,
                waypoints = state.waypoints,
                navigationViewModel = viewModel,
            )
        }
        is NavigationScreen.AppMainScreen -> {
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
                        selected = currentDestination?.hierarchy?.any { it.route == NavigationEvent.WalletScreen.route } == true,
                        onClick = {
                            navController.navigate(NavigationEvent.WalletScreen.route)
                        },
                        icon = {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "" )
                        },
                        label = { Text(text = "Wallet") }
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
                }
            }) {
                val innerPadding = it
//                if(state.usersAvalible && !state.userAccepted && (state.rideStatus == null || state.rideStatus == "cancelled") ){
                if(state.usersAvalible)
                    state.rideSharingRideDetails.forEach {
                        if(it.request == "pending")
                        {
                            Column(
                                modifier = Modifier
                                    .zIndex(7f)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.3f)
                                        .background(
                                            shape = RoundedCornerShape(
                                                topStart = 25.dp,
                                                topEnd = 25.dp
                                            ),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if(it.UserInfo.userPic != null)
                                            Image(
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(100.dp)
                                                    .offset(25.dp, (-45).dp)
                                                    .clip(shape = CircleShape),
                                                contentScale = ContentScale.Crop,
                                                bitmap = it.UserInfo.userPic!!,
                                                contentDescription = null
                                            )
                                        else
                                            Image(
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(100.dp)
                                                    .offset(25.dp, (-45).dp)
                                                    .clip(shape = CircleShape),
                                                contentScale = ContentScale.Crop,
                                                painter = painterResource(id = R.drawable.man),
                                                contentDescription = null,
                                            )
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 20.dp)
                                            ,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 3.8.em,
                                                text = it.UserInfo.firstName+ " " + it.UserInfo.lastName
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.End,
                                            ){
                                                Icon(imageVector = if(it.UserInfo.gender == "Male") Icons.Filled.Male else Icons.Filled.Female, contentDescription = null, modifier = Modifier.size(20.dp),tint = MaterialTheme.colorScheme.secondary)
                                                Text(text = it.UserInfo.gender.toString(), fontSize = 14.sp)
                                            }
                                        }
                                    }
                                    Column(
                                        modifier = Modifier
                                            .offset(0.dp,(-30.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 20.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 3.4.em,
                                                text = "Pickup --- Destination"
                                            )
                                            Text(
                                                style = MaterialTheme.typography.bodySmall,
                                                text = it.distance +"km "+ it.duration
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 20.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 3.4.em,
                                                text = "CurrentLocation --- Pickup"
                                            )
                                            Text(
                                                style = MaterialTheme.typography.bodySmall,
                                                text =  it.distanceFromDriver + "km " + it.durationFromDriver,
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 20.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 4.em,
                                                text = "Price"
                                            )
                                            Text(
                                                text = "${it.price} Rs",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .padding(top = 20.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            ElevatedButton(onClick = {
                                                viewModel.onEvent(NavigationEvent.userRejected)
                                            }) {
                                                Text(text = "Reject")
                                            }
                                            Button(onClick = {
                                                db.collection("ridesDetail").document(it.UserInfo.userUid!!)
                                                    .update("request","accepted").addOnSuccessListener {
                                                        Log.d("request","Accepted")
                                                    }
                                                it.request = "accepted"
                                                viewModel.onEvent(NavigationEvent.userAccepted(it.distance))
                                            }) {
                                                Text(text = "Accept")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
//                }


                NavHost(navController = navController, startDestination = NavigationEvent.MainScreen.route ){
                    composable(route = NavigationEvent.MainScreen.route){
                        HomeScreenUi(navController)
                    }
                    composable(route = NavigationEvent.RideScreen.route){
                        Pickup(context,defaultSelectedIndex = if(pickupLocation == null) 1 else if(destinationLocation == null) 2 else 3,viewModel,innerPadding)
                    }
                    composable(route = NavigationEvent.ProfileScreen.route){
                        UserProfileUI(innerPadding = innerPadding)
                    }
                    composable(route = NavigationEvent.ConfigurationScreen.route){
                        ConfigUi()
                    }
                    composable(route = NavigationEvent.WalletScreen.route){
                        WalletUI()
                    }
                }
            }
        }
    }
}
