package com.simpdev.sahmride.Presentation.HomeScreen

import Domain.Data.auth
import Domain.Data.context
import Domain.Data.database
import Domain.Data.userData
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.R
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun HomeScreenUi(navController: NavController)
{
    val viewModel = viewModel<HomeScreenViewModel>()
    val state = viewModel.state


    LaunchedEffect(key1 = 1){
        viewModel.checkDriverStatus()
    }
    val resumed = remember { mutableStateOf(false) }

    // Observe lifecycle events using LocalLifecycleOwner
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> resumed.value = true
                Lifecycle.Event.ON_PAUSE -> resumed.value = false
                else -> {}
            }
        }
        lifecycle.addObserver(observer)

    }

    Column(
        modifier = Modifier
            .padding(bottom = 70.dp)
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(enabled = true, state = rememberScrollState())
    ){
    // Check if location is enabled
        Text("SahmRide",fontSize=7.em, fontWeight = FontWeight.SemiBold,modifier = Modifier.padding(horizontal = 15.dp))
        val images = listOf(
           R.drawable.slide,R.drawable.slidee,R.drawable.slideee
        )
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            AutoSlidingCarousel(
                itemsCount = images.size,
                itemContent = { index ->
                    Image(
                        painterResource(id = images[index]),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.height(200.dp)
                    )

                }
            )
        }
        Tools(navController)
        if (resumed.value && ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .fillMaxWidth()
                ){
                    Row(
                        modifier = Modifier
                            .padding(vertical = 17.dp)
                    ){
                        Column (
                            modifier = Modifier
                                .size(60.dp)
                                .padding(10.dp)
                                .background(color = MaterialTheme.colorScheme.secondaryContainer,shape= CircleShape),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Icon(imageVector = Icons.Filled.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text(text = "Turn on location services.", fontWeight = FontWeight.SemiBold)
                            Text(text = "Please enable location for better experience.", fontWeight = FontWeight.Light,fontSize = 3.4.em, lineHeight = 1.em,modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 5.dp))
                            Button(onClick = {
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                context!!.startActivity(intent)
                            }) {
                                Text(text = "Enable Location")
                            }
                        }
                    }
                }
            } else {
                if(state.isDriver)
                    ActiveWidget(viewModel = viewModel)
            }
        }
        else {
            // Permission not granted
            Text(text = "Location permission not granted.")
        }

        Column(
            modifier = Modifier
                .padding(10.dp)
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(4.dp))
                .fillMaxWidth()
                .clickable(enabled = true, onClick = {
                    navController.navigate(NavigationEvent.RideScreen.route)
                })
        ){
            Text(text = "Book Ride", fontSize = 4.em, fontWeight = FontWeight.SemiBold,modifier = Modifier.padding(15.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp), // inner padding
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocation,
                    contentDescription = "Favorite icon",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(text = "Enter your pickup location ...")
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Favorite icon",
                    tint = Color.DarkGray
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    onClick = {}) {
                    Text(text = "Add Pickup")
                }
            }
        }
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 10.dp, vertical = 20.dp)
//                    .background(
//                        color = MaterialTheme.colorScheme.primaryContainer,
//                        shape = RoundedCornerShape(20.dp)
//                    ),
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 10.dp, vertical = 20.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ){
//                    Text(
//                        text = "Active",
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = 20.sp
//                    )
//                    Switch(checked = state.active, onCheckedChange = {
//                        viewModel.onEvent(HomeScreenEvents.activeChange)
//                    })
//                }
//            }
//        if(state.active)
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 10.dp, vertical = 10.dp)
//                    .background(
//                        color = MaterialTheme.colorScheme.secondaryContainer,
//                        shape = RoundedCornerShape(20.dp)
//                    ),
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 10.dp, vertical = 20.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ){
//                    Text(
//                        text = "Available Seats",
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = 20.sp
//                    )
//                    TextButton(onClick = {
//                        viewModel.onEvent(HomeScreenEvents.expandMenuChange)
//                    }) {
//                        Text(text = state.availableSeats)
//                    }
//                    DropdownMenu(
//                        expanded = state.expandMenu,
//                        onDismissRequest = { },
//                        offset = DpOffset(200.dp,(-40).dp)
//                    ) {
//                        DropdownMenuItem(
//                            text = { Text("0") },
//                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("0")) })
//                        DropdownMenuItem(
//                            text = { Text("1") },
//                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("1"))  })
//                        DropdownMenuItem(
//                            text = { Text("2") },
//                            onClick = {viewModel.onEvent(HomeScreenEvents.availableSeatsChange("2"))  })
//                        DropdownMenuItem(
//                            text = { Text("3") },
//                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("3"))  })
//                        DropdownMenuItem(
//                            text = { Text("4") },
//                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("4"))  })
//                        DropdownMenuItem(
//                            text = { Text("5") },
//                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("5"))  })
//                    }
//                }
//            }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                )
        ){
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
                    ){
                Text(
                    text = "History",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 5.em
                )
                Icon(imageVector = Icons.Filled.History, contentDescription = null)
            }
            if(state.fetchingRideDetails){
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            else {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 70.dp)
                )
                state.historyList.forEach {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = it.dayData.toString(),
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(text = it.price.toString())
                    }
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 10.dp),
                        text = "${it.distance}km ${it.duration}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                }
            }

        }
    }
}

@Composable
fun HomeUi(){
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        ActiveWidget()
        BookRideWidget()
    }
}
@SuppressLint("UnrememberedMutableState")
@Composable
fun ActiveWidget(
    viewModel: HomeScreenViewModel
){
    var seatsList = remember {
        listOf("Avalible","Avalible","Avalible","Avalible").toMutableStateList()    }
    val state = viewModel.state
    var expandMenu by remember { mutableStateOf(false) }
    val totalSeats:Int = state.availableSeats.toInt()
    LaunchedEffect(key1 = 1, block = {
        if(state.isDriver){
            val setDetails = database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("seatsDetail")
            setDetails.get().addOnSuccessListener { dataSnapshot ->
                if(dataSnapshot.exists()){
                    dataSnapshot.children.forEach {
                        seatsList.set(it.key.toString().toInt(),it.value.toString())
                    }
                    seatsList.set(1, userData.gender.toString())
                }
            }
        }
    })
    var currentIndex by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier
            .padding(10.dp)
            .border(border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(4.dp))
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(4.dp)
            )
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = "Active", fontSize = 5.em, fontWeight = FontWeight.SemiBold)
            Switch(checked = state.active, onCheckedChange = {
                viewModel.onEvent(HomeScreenEvents.activeChange)
            })
        }
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.Start
                    ){
                        Row{
                            Button(onClick = {}, modifier = Modifier
                                .size(20.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                            }
                            Text("Male")
                        }
                        Row{
                            Button(onClick = {}, modifier = Modifier
                                .size(20.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                            }
                            Text("Female")
                        }
                        Row{
                            Button(onClick = {}, modifier = Modifier
                                .size(20.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                            }
                            Text("Available Seats")
                        }
                    }
                    Column(
                        modifier = Modifier
                            .border(border = BorderStroke(1.dp,Color.Gray), shape = RoundedCornerShape(4.dp))
                    ) {
                        Row(){
                            Button(onClick = {
                                currentIndex = 0
                                expandMenu = !expandMenu
                            }, modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(seatsList[0] == "Avalible")
                                                        MaterialTheme.colorScheme.tertiary
                                                    else if(seatsList[0] == "Male")
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.secondary ),
                            ) {
                                Text("0",color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Button(onClick = {
                            }, modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(seatsList[1] == "Avalible")
                                        MaterialTheme.colorScheme.tertiary
                                    else if(seatsList[1] == "Male")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondary ),
                            ) {
                                Text("1",color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Row(){
                            Button(onClick = {
                                currentIndex = 2
                                expandMenu = !expandMenu
                            }, modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(seatsList[2] == "Avalible")
                                        MaterialTheme.colorScheme.tertiary
                                    else if(seatsList[2] == "Male")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondary ),
                            ) {
                                Text("2",color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Button(onClick = {
                                currentIndex = 3
                                expandMenu = !expandMenu
                            }, modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor =if(seatsList[3] == "Avalible")
                                        MaterialTheme.colorScheme.tertiary
                                    else if(seatsList[3] == "Male")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondary ),
                            ) {
                                Text("3",color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        DropdownMenu(
                            expanded = expandMenu,
                            onDismissRequest = { },
//                            offset = DpOffset(200.dp,(-40).dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Male") },
                                onClick = {
                                    seatsList[currentIndex] = "Male"
                                    expandMenu = false
                                    seatsList.forEachIndexed { index, value ->
                                        database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("seatsDetail").child(index.toString()).setValue(value)
                                    }
                                })
                            DropdownMenuItem(
                                text = { Text("Female") },
                                onClick = {
                                    seatsList[currentIndex] = "Female"
                                    expandMenu = false
                                    seatsList.forEachIndexed { index, value ->
                                        database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("seatsDetail").child(index.toString()).setValue(value)
                                    }
                                })
                            DropdownMenuItem(
                                text = { Text("Avalible") },
                                onClick = {
                                    seatsList[currentIndex] = "Avalible"
                                    expandMenu = false
                                    seatsList.forEachIndexed { index, value ->
                                        database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("seatsDetail").child(index.toString()).setValue(value)
                                    }
                                })
                        }
                    }
                }
            }
        }

    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun ActiveWidgetT(
){
    var expandMenu by remember { mutableStateOf(false) }
    var seatsList = remember {
        listOf("Avalible","Avalible","Avalible","Avalible").toMutableStateList()
    }
    var currentIndex by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier
            .padding(10.dp)
            .border(border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(4.dp))
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(4.dp)
            )
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = "Active", fontSize = 5.em, fontWeight = FontWeight.SemiBold)
            Switch(checked = true, onCheckedChange = {})
        }
        Row(modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Button(onClick = {}, modifier = Modifier
                        .size(20.dp),
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                    }
                    Text("Male")
                    Button(onClick = {}, modifier = Modifier
                        .size(20.dp),
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                    }
                    Text("Female")
                    Button(onClick = {}, modifier = Modifier
                        .size(20.dp),
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                    }
                    Text("Available Seats")
                }
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Image(
                        painter = painterResource(id = when(4){
                            1 -> R.drawable.icon1
                            2 -> R.drawable.icon2
                            3 -> R.drawable.icon3
                            4 -> R.drawable.icon4
                            5 -> R.drawable.icon5
                            6 -> R.drawable.icon6
                            else -> {
                                R.drawable.icon6}
                        }),
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .border(
                                border = BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(4.dp)
                            )
                        ,
                        contentScale = ContentScale.Crop,
                        contentDescription = ""
                    )
                    Column(
                        modifier = Modifier
                            .border(border = BorderStroke(1.dp,Color.Gray), shape = RoundedCornerShape(4.dp))
                    ) {
                        Row(){
                            Button(onClick = {
                                currentIndex = 0
                                expandMenu = true
                            }, modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(seatsList[0] == "Avalible")
                                        MaterialTheme.colorScheme.tertiary
                                    else if(seatsList[0] == "Male")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondary ),
                            ) {
                                Text("0",color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Button(onClick = {
                                currentIndex = 1
                                expandMenu = true
                            }, modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(seatsList[1] == "Avalible")
                                        MaterialTheme.colorScheme.tertiary
                                    else if(seatsList[1] == "Male")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondary ),
                            ) {
                                Text("1",color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Row(){
                            Button(onClick = {
                                currentIndex = 2
                                expandMenu = true
                            }, modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(seatsList[2] == "Avalible")
                                        MaterialTheme.colorScheme.tertiary
                                    else if(seatsList[2] == "Male")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondary ),
                            ) {
                                Text("2",color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Button(onClick = {
                                currentIndex = 3
                                expandMenu = true
                            }, modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor =if(seatsList[3] == "Avalible")
                                        MaterialTheme.colorScheme.tertiary
                                    else if(seatsList[3] == "Male")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondary ),
                            ) {
                                Text("3",color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        DropdownMenu(
                            expanded = expandMenu,
                            onDismissRequest = { },
//                            offset = DpOffset(200.dp,(-40).dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Male") },
                                onClick = {
                                    seatsList[currentIndex] = "Male"
                                    expandMenu = false
                                })
                            DropdownMenuItem(
                                text = { Text("Female") },
                                onClick = {
                                    seatsList[currentIndex] = "Female"
                                    expandMenu = false
                                })
                        }
                    }
                }
            }
        }

    }
}
@Composable
fun Tools(navController: NavController) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(enabled = true, onClick = {navController.navigate(NavigationEvent.RideScreen.route)})
        ) {
            Image(painter = painterResource(id = R.drawable.sedan), contentDescription = null,modifier = Modifier.size(32.dp))
            Text(text = "Ride", fontSize =  2.8.em)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(enabled = true, onClick = {navController.navigate(NavigationEvent.WalletScreen.route)})
        ) {
            Image(painter = painterResource(id = R.drawable.wallet), contentDescription = null,modifier = Modifier.size(32.dp))
            Text(text = "Wallet", fontSize =  2.8.em)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(enabled = true, onClick = {navController.navigate(NavigationEvent.WalletScreen.route)})
        ) {
            Image(painter = painterResource(id = R.drawable.send), contentDescription = null,modifier = Modifier.size(32.dp))
            Text(text = "Send", fontSize =  2.8.em)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(enabled = true, onClick = {navController.navigate(NavigationEvent.WalletScreen.route)})
        ) {
            Image(painter = painterResource(id = R.drawable.deposit), contentDescription = null,modifier = Modifier.size(32.dp))
            Text(text = "Deposit", fontSize =  2.8.em)
        }
    }
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = R.drawable.user), contentDescription = null,modifier = Modifier.size(32.dp))
            Text(text = "Profile", fontSize =  2.8.em)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(enabled = true, onClick = {navController.navigate(NavigationEvent.ConfigurationScreen.route)})
        ) {
            Image(painter = painterResource(id = R.drawable.settings), contentDescription = null,modifier = Modifier.size(32.dp))
            Text(text = "Settings", fontSize =  2.8.em)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.size(32.dp)){}
            Text(text = "", fontSize =  2.8.em)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.size(32.dp)){}
            Text(text = "", fontSize =  2.8.em)
        }
    }
}
@Composable
fun BookRideWidget(){
    Column(
        modifier = Modifier
            .padding(15.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .border(border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(4.dp))
            .fillMaxWidth()
            .clickable(enabled = true, onClick = {})
    ){
        Text(text = "Book Ride", fontSize = 4.em, fontWeight = FontWeight.SemiBold,modifier = Modifier.padding(15.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp), // inner padding
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AddLocation,
                contentDescription = "Favorite icon",
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(text = "Enter your pickup location ...")
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Favorite icon",
                tint = Color.DarkGray
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = {}) {
                Text(text = "Add Pickup")
            }
        }
    }
}

@Composable
fun IndicatorDot(
    modifier: Modifier = Modifier,
    size: Dp,
    color: Color
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun DotsIndicator(
    modifier: Modifier = Modifier,
    totalDots: Int,
    selectedIndex: Int,
    selectedColor: Color = MaterialTheme.colorScheme.primary /* Color.Yellow */,
    unSelectedColor: Color = MaterialTheme.colorScheme.secondaryContainer /* Color.Gray */,
    dotSize: Dp
) {
    LazyRow(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
    ) {
        items(totalDots) { index ->
            IndicatorDot(
                color = if (index == selectedIndex) selectedColor else unSelectedColor,
                size = dotSize
            )

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AutoSlidingCarousel(
    modifier: Modifier = Modifier,
    autoSlideDuration: Long = 4000,
    pagerState: PagerState = remember { PagerState() },
    itemsCount: Int,
    itemContent: @Composable (index: Int) -> Unit,
) {
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(pagerState.currentPage) {
        delay(autoSlideDuration)
        pagerState.scrollToPage(((pagerState.currentPage + 1) % itemsCount))
    }

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalPager(count = itemsCount, state = pagerState) { page ->
            itemContent(page)
        }
        Surface(
            modifier = Modifier
                .padding(bottom = 8.dp, start = 25.dp)
                .align(Alignment.BottomStart),
            shape = CircleShape,
            color = Color.Transparent
        ) {
            DotsIndicator(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                totalDots = itemsCount,
                selectedIndex = if (isDragged) pagerState.currentPage else pagerState.targetPage,
                dotSize = 8.dp
            )
        }
    }
}