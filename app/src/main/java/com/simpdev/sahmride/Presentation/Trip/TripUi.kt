package com.simpdev.sahmride.Presentation.Trip

import Domain.Data.ApiService
import Domain.Data.auth
import Domain.Data.database
import Domain.Data.db
import Domain.Data.drawCircularAnnotation
import Domain.Data.driverCurrentLocation
import Domain.Data.flytoLocation
import Domain.Data.storageRef
import Domain.Data.trackCurrentLocation
import Domain.Data.userData
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.NavigationRouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import com.simpdev.sahmride.Domain.RideDetails
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.Ride.RideEvent
import com.simpdev.sahmride.Presentation.Ride.RideScreen
import com.simpdev.sahmride.Presentation.Ride.RideViewModel
import com.simpdev.sahmride.Presentation.StreamChat.StreamChat
import com.simpdev.sahmride.R
import com.simpdev.sahmride.customColorResources
import com.simpdev.sahmride.displayDensity
import com.simpdev.sahmride.mapNav
import com.simpdev.sahmride.mapView
import com.simpdev.sahmride.naivgationRouterCallback
import com.simpdev.sahmride.routeLineApi
import com.simpdev.sahmride.routeLineOptions
import com.simpdev.sahmride.routeLineResources
import com.simpdev.sahmride.routeLineView
import com.simpdev.sahmride.routeOptions
import com.simpdev.sahmride.routesObserver
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun TripUi(
    context: Context,
    pickup: Point?,
    destination: Point?,
    userUids: List<String>,
    userType: String,
    navigationViewModel: NavigationViewModel? = null,
    rideViewModel: RideViewModel? = null,
    usersInfo: SnapshotStateList<RideDetails>,
){
    val viewModel = viewModel<TripViewModel>()
    val state = viewModel.state
    LaunchedEffect(key1 = 1, block = {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://gentle-patch-dinosaur.glitch.me/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        try {
            val response = apiService.getApiResponse(uid = auth.currentUser!!.uid)
            val token = response.token
            storageRef.child("images/${auth.currentUser?.uid}/profile").downloadUrl.addOnSuccessListener {
                val user = User(
                    id = auth.currentUser!!.uid,
                    extraData = mutableMapOf(
                        "name" to userData.firstName.toString(),
                        "image" to it.toString()
                    )
                )
                ChatClient.instance().connectUser(user,token,86400000) // Replace with a real token
                    .enqueue { result ->
                        if (result.isSuccess) {
                            Log.d("Chat","${token}")
                        } else {
                            Log.d("Chat",result.toString())
                            Log.d("Chat",token)
                        }
                    }
            }
        } catch (e: Exception) {
            // Handle the exception
        }
    })
    LaunchedEffect(key1 = state.refresh, block = {
        state.waypoints = emptyList<Point>().toMutableList()
        database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("waypoints").orderByKey().get().addOnSuccessListener {
            it.children.forEach {
                state.waypoints.add(Point.fromLngLat((it.child("lng").value.toString()).toDouble(),(it.child("lat").value.toString()).toDouble()))
            }
            routeOptions = RouteOptions.builder()
                .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                .coordinatesList(
                    state.waypoints
                )
                .build()

            mapNav.requestRoutes(routeOptions,naivgationRouterCallback)
        }

    })
    val accessToken = stringResource(id = R.string.mapbox_access_token)
    val tripProgressFormatter: TripProgressUpdateFormatter by lazy {
        val distanceFormatterOptions =
            DistanceFormatterOptions.Builder(context).build()

        TripProgressUpdateFormatter.Builder(context)
            .distanceRemainingFormatter(DistanceRemainingFormatter(distanceFormatterOptions))
            .timeRemainingFormatter(TimeRemainingFormatter(context))
            .estimatedTimeToArrivalFormatter(EstimatedTimeToArrivalFormatter(context))
            .build()
    }
    val tripProgressApi: MapboxTripProgressApi by lazy {
        MapboxTripProgressApi(tripProgressFormatter)
    }
    var distance by remember { mutableStateOf(0.0) }
    var duration  by remember { mutableStateOf(0.0) }
    //Route Line
    Log.d("Pickup",pickup.toString())
    Log.d("Destination",destination.toString())
    routeOptions = RouteOptions.builder()
        .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
        .coordinatesList(
            state.waypoints
        )
        .build()
    customColorResources = RouteLineColorResources.Builder()
        .routeDefaultColor(android.graphics.Color.parseColor("#000000"))
//        .inActiveRouteLegsColor(android.graphics.Color.parseColor("#FFCC00"))
        .build()

    routeLineResources = RouteLineResources.Builder()
        .routeLineColorResources(customColorResources)
        .build()
    routeLineOptions = MapboxRouteLineOptions.Builder(context)
//        .styleInactiveRouteLegsIndependently(true)
        .withRouteLineBelowLayerId("road-label")
        .withRouteLineResources(routeLineResources)
        .displaySoftGradientForTraffic(true)
        .softGradientTransition(30)
        .build()
    routeLineApi = MapboxRouteLineApi(routeLineOptions)
    routeLineView = MapboxRouteLineView(routeLineOptions)
    routesObserver = object : RoutesObserver {
        override fun onRoutesChanged(result: RoutesUpdatedResult) {
            val routeLines = result.navigationRoutes.map { NavigationRouteLine(it, null) }
            routeLineApi.setNavigationRouteLines(routeLines) { value ->
                mapView?.getMapboxMap()?.getStyle()
                    ?.let { routeLineView.renderRouteDrawData(it, value) }
            }
        }
    }
    naivgationRouterCallback = object  : NavigationRouterCallback {
        override fun onCanceled(
            routeOptions: RouteOptions,
            routerOrigin: RouterOrigin
        ) {

        }

        override fun onFailure(
            reasons: List<RouterFailure>,
            routeOptions: RouteOptions
        ) {
            Log.d("Failure : ",reasons.toString())
        }

        override fun onRoutesReady(
            routes: List<NavigationRoute>,
            routerOrigin: RouterOrigin
        ) {
            // GSON instance used only to print the response prettily
            GsonBuilder().setPrettyPrinting().create()
            val json = JsonParser.parseString(routes[0].directionsRoute.toJson())
            val jsonObj = json.asJsonObject
            distance = jsonObj.get("distance").asDouble
            duration = jsonObj.get("duration").asDouble
            Log.d("RouteDeatils : ",distance.toString())
            routeLineApi.setNavigationRoutes(routes) { value ->
                mapView?.getMapboxMap()?.getStyle()
                    ?.let { routeLineView.renderRouteDrawData(it, value) }
            }
        }

    }
    val routeProgressObserver =
        RouteProgressObserver { routeProgress ->
            tripProgressApi.getTripProgress(routeProgress).let { update ->
                distance = update.distanceRemaining
                duration = update.totalTimeRemaining
            }
            routeLineApi.updateWithRouteProgress(routeProgress) {
                mapView?.getMapboxMap()?.getStyle()
                    ?.let { it1 -> routeLineView.renderRouteLineUpdate(it1,it) }
            }
        }
    
    LaunchedEffect(key1 = 1){
        viewModel.listenForUsers(userUids[0])
        mapNav.registerRouteProgressObserver(routeProgressObserver)
        mapNav.registerRoutesObserver(routesObserver)
        mapNav.requestRoutes(routeOptions,naivgationRouterCallback)
        mapView?.let { trackCurrentLocation(it, displayDensity) }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        mapNav.startTripSession(true)

            mapView?.location?.apply {
                this.locationPuck = LocationPuck2D(
                    bearingImage = ContextCompat.getDrawable(context, R.drawable.arroww)
                )
            }
        mapView?.let { flytoLocation(it, driverCurrentLocation) }
    }


    Log.d("Pickup",pickup?.latitude().toString())
    Log.d("Destination",destination?.latitude().toString())
    Log.d("DriverLocation", driverCurrentLocation?.latitude().toString())

    if(state.usersAvalible){
        state.rideSharingRideDetails.forEach {
            if(it.request == "pending")
            {
                Column(
                    modifier = Modifier
                        .zIndex(7f)
                        .background(color = Color.Black)
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
                                color = MaterialTheme.colorScheme.background
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
                                    text = "${it.UserInfo.firstName} ${it.UserInfo.lastName}"
                                )
                                Row{
                                    Icon(
                                        imageVector = if (it.UserInfo.rating >= 1) Icons.Filled.Star else Icons.Filled.StarOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Icon(
                                        imageVector = if (it.UserInfo.rating >= 2) Icons.Filled.Star else Icons.Filled.StarOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Icon(
                                        imageVector = if (it.UserInfo.rating >= 3) Icons.Filled.Star else Icons.Filled.StarOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Icon(
                                        imageVector = if (it.UserInfo.rating >= 4) Icons.Filled.Star else Icons.Filled.StarOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Icon(
                                        imageVector = if (it.UserInfo.rating >= 5) Icons.Filled.Star else Icons.Filled.StarOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
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
                                .offset(0.dp,(-10.dp)),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row (
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(vertical = 9.dp)
                                    .fillMaxWidth(0.9f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ){
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ridedetails),
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(32.dp),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = ""
                                    )
                                    Text(
                                        color =MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(10.dp),
                                        text = " ${it.distance}km ",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        text = " ${it.duration}",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Image(
                                        painter = painterResource(id = R.drawable.money),
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(32.dp),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = ""
                                    )
                                    Text(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        text = "PKR ${it.price}",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ElevatedButton(onClick = {
                                    viewModel.onEvent(TripEvents.userRejected)
                                }) {
                                    Text(text = "Reject")
                                }
                                Button(onClick = {
                                    db.collection("ridesDetail").document(it.UserInfo.userUid!!)
                                        .update("request","accepted").addOnSuccessListener {
                                            Log.d("request","Accepted")
                                        }
                                    it.request = "accepted"
                                    viewModel.onEvent(TripEvents.userAccepted(it.UserInfo.userUid!!))
                                }) {
                                    Text(text = "Accept")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    when(state.currentScreen)
    {
        is TripScreen.ChatScreen -> {
//            ChatUi(tripViewModel = viewModel, userUids = userUids,userType = userType, tripUserViewModel = null,userInfo = usersInfo[0].UserInfo)
            ChatClient.instance().createChannel("messaging",auth.currentUser!!.uid, listOf(auth.currentUser!!.uid,userUids[0]),mutableMapOf<String, Any>(
                "name" to "Chat" ,
                "description" to "A channel for discussing various topics"
            )).enqueue { result ->
                if (result.isSuccess) {
                    Log.d("Channellll","User Connected To Chat")
                } else {
                    Log.d("Channellll",result.toString())
                }
            }
            ChatClient.instance().watchChannelAsState(
                "messaging:"+userUids[0].toString(),99
            )
            StreamChat(tripDriverViewModel = viewModel)
        }
        is TripScreen.TripHome -> {
            Box {
                AndroidView(modifier = Modifier
                    .zIndex(6f)
                    .fillMaxSize(),
                    factory = { it ->
                        if(mapView == null)
                        {
                            mapView = MapView(it).apply {
                                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS){
                                    cameraOptions {
                                        zoom(19.0)
                                    }
                                }
                            }
                            mapNav = MapboxNavigation(
                                NavigationOptions.Builder(it)
                                    .accessToken(accessToken)
                                    .build())
                            mapNav.registerRouteProgressObserver(routeProgressObserver)
                            mapNav.registerRoutesObserver(routesObserver)

                            mapNav.requestRoutes(routeOptions,naivgationRouterCallback)
                            mapView?.let { flytoLocation(it, driverCurrentLocation) }

                            if (pickup != null) drawCircularAnnotation(mapView!!, pickup)
                            if (destination != null) drawCircularAnnotation(mapView!!, destination)
                        }
                        displayDensity = it.resources.displayMetrics.density
                        mapView!!
                    }
                )
            }
            Box {
                Column(
                    modifier = Modifier
                        .padding(end = 40.dp, bottom = 40.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.onEvent(TripEvents.chatClicked)
                        },
                    ){
                        Icon(Icons.Filled.Chat,"")
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.DOWN
                ElevatedCard(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                        )
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    )
                    {
                        Column(modifier = Modifier
                            .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = (df.format(distance/1000)).toString()+"km", fontSize = 7.em )
                            Text(text = "Distance" )
                        }
                        Column(modifier = Modifier
                            .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = ((duration/3600).toInt()).toString()+"hr "+(((duration % 3600) / 60).roundToInt()).toString()+"min", fontSize = 7.em  )
                            Text(text = "Duration" )
                        }
                    }
                }
                Row()
                {
                    ElevatedButton(
                        onClick = {
                            db.collection("ridesDetail").document(userUids[0].toString())
                                .update("request","cancelled").addOnSuccessListener {
                                    Log.d("request","cancelled")
                                }
                            val ref = database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("users").child(userUids[0].toString())
                            ref.setValue(null)
                            navigationViewModel?.onEvent(NavigationEvent.rideCancelled)
                            rideViewModel?.onEvent(RideEvent.changeCurrentRideScreen(RideScreen.RideHome))

                        }
                    ){
                        Text(text = "Cancel Ride")
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun aaa(){
    Column(
        modifier = Modifier
            .zIndex(7f)
            .background(color = Color.Black)
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
                    color = MaterialTheme.colorScheme.background
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                        text = "Usama Muneeb"
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                    ){
                        Icon(imageVector = Icons.Filled.Male , contentDescription = null, modifier = Modifier.size(20.dp),tint = MaterialTheme.colorScheme.secondary)
                        Text(text = "Male ", fontSize = 14.sp)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .offset(0.dp,(-30.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row (
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(vertical = 9.dp)
                        .fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ridedetails),
                            modifier = Modifier
                                .width(32.dp)
                                .height(32.dp),
                            contentScale = ContentScale.Crop,
                            contentDescription = ""
                        )
                        Text(
                            color =MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(10.dp),
                            text = " 88km  ",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            color = MaterialTheme.colorScheme.onBackground,
                            text = "0hr " + " 40min",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Image(
                            painter = painterResource(id = R.drawable.money),
                            modifier = Modifier
                                .width(32.dp)
                                .height(32.dp),
                            contentScale = ContentScale.Crop,
                            contentDescription = ""
                        )
                        Text(
                            color = MaterialTheme.colorScheme.onBackground,
                            text = "PKR 700",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ElevatedButton(onClick = {

                    }) {
                        Text(text = "Reject")
                    }
                    Button(onClick = {

                    }) {
                        Text(text = "Accept")
                    }
                }
            }
        }
    }
}