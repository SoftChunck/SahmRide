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
import com.simpdev.sahmride.Presentation.HomeScreen.HomeScreenUi
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.Presentation.Navigation.NavigationScreen
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.Navigation.userInfo
import com.simpdev.sahmride.Presentation.Trip.TripUi
import com.simpdev.sahmride.Presentation.TripUser.TripUser
import com.simpdev.sahmride.Presentation.UserProfile.UserProfileUI

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
                            painterResource(id = R.drawable.sahmlogo),
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
                            database.reference.child("driversLocation").child(state.userUid!!).child("users").setValue(null)
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
                                painterResource(id = R.drawable.sahmlogo),
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
            Log.d("TripUser","Driver Accepted")
            TripUi(
                context = context, pickup = state.pickup, destination = state.destination,
                listOf<String>(state.userUid!!),
                userType = "Driver",
                userInfo = userInfo(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    userUid = state.userUid,
                    userPic = state.userPic
                ),
                navigationViewModel = viewModel
            )
        }
        is NavigationScreen.UserAcceptedScreen -> {

            Log.d("TripUser","User Accepted")
            TripUser(context = context,pickup = state.pickup,destination = state.destination,
                listOf<String>(state.userUid!!),
                userType = "User",
                userInfo = userInfo(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    userUid = state.userUid,
                    userPic = state.userPic
                ),
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
                if(state.usersAvalible && !state.userAccepted && (state.rideStatus == null || state.rideStatus == "cancelled") ){
                    Column(
                        modifier = Modifier
                            .zIndex(7f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.35f)
                                .background(
                                    shape = RoundedCornerShape(
                                        topStart = 25.dp,
                                        topEnd = 25.dp
                                    ),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ),
                            verticalArrangement = if(state.loadingUserProfile) Arrangement.Center else Arrangement.Top,
                            horizontalAlignment = if(state.loadingUserProfile) Alignment.CenterHorizontally else Alignment.Start
                        ) {
                            if(state.loadingUserProfile)
                            {
                                CircularProgressIndicator()
                            }
                            else
                            {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if(state.userPic != null)
                                        Image(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(100.dp)
                                                .offset(25.dp, (-45).dp)
                                                .clip(shape = CircleShape),
                                            contentScale = ContentScale.Crop,
                                            bitmap = state.userPic!!,
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
                                            text = state.firstName+ " " + state.lastName
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                        ){
                                            Icon(imageVector = Icons.Filled.Male, contentDescription = null, modifier = Modifier.size(20.dp),tint = MaterialTheme.colorScheme.secondary)
                                            Text(text = "Male", fontSize = 14.sp)
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
                                            text = state.distance +"km "+ state.duration
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
                                            text =  state.distanceFromDriver + "km " + state.durationFromDriver
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
                                            style = MaterialTheme.typography.bodySmall,
                                            text =  (state.distance!!.toDouble()  * priceOfFule * fulePerKm).toString() + " Rs"
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
                                            db.collection("ridesDetail").document(state.userUid!!)
                                                .update("request","accepted").addOnSuccessListener {
                                                    Log.d("request","Accepted")
                                                }
                                            viewModel.onEvent(NavigationEvent.userAccepted)
                                        }) {
                                            Text(text = "Accept")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                NavHost(navController = navController, startDestination = NavigationEvent.MainScreen.route ){
                    composable(route = NavigationEvent.MainScreen.route){
                        HomeScreenUi(navController)
                    }
                    composable(route = NavigationEvent.RideScreen.route){
                        Pickup(context,defaultSelectedIndex = if(pickupLocation == null) 1 else if(destinationLocation == null) 2 else 3,viewModel)
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
}
