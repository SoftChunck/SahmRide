package com.simpdev.sahmride.Presentation.Trip

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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
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
import com.simpdev.sahmride.*
import com.simpdev.sahmride.Domain.RideDetails
import com.simpdev.sahmride.Presentation.Chat.ChatUi
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.Ride.RideEvent
import com.simpdev.sahmride.Presentation.Ride.RideScreen
import com.simpdev.sahmride.Presentation.Ride.RideViewModel
import com.simpdev.sahmride.R
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
                                    text =  it.distanceFromDriver + "km " + it.durationFromDriver
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
                                    text =  (it.distance!!.toDouble()  * priceOfFule * fulePerKm).roundToInt().toString() + " Rs"
                                )
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
                                    viewModel.onEvent(TripEvents.userAccepted(it.distance))
                                }) {
                                    Text(text = "Accept")
                                }
                            }
                        }
                    }
                }
            }
        }

//        Column(
//            modifier = Modifier
//                .zIndex(7f)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.Bottom
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .fillMaxHeight(0.35f)
//                    .background(
//                        shape = RoundedCornerShape(
//                            topStart = 25.dp,
//                            topEnd = 25.dp
//                        ),
//                        color = MaterialTheme.colorScheme.secondaryContainer
//                    ),
//                verticalArrangement = if(state.loadingUserProfile) Arrangement.Center else Arrangement.Top,
//                horizontalAlignment = if(state.loadingUserProfile) Alignment.CenterHorizontally else Alignment.Start
//            ) {
//                if(state.loadingUserProfile)
//                {
//                    CircularProgressIndicator()
//                }
//                else
//                {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        if(state.userPic != null)
//                            Image(
//                                modifier = Modifier
//                                    .width(100.dp)
//                                    .height(100.dp)
//                                    .offset(25.dp, (-45).dp)
//                                    .clip(shape = CircleShape),
//                                contentScale = ContentScale.Crop,
//                                bitmap = state.userPic!!,
//                                contentDescription = null
//                            )
//                        else
//                            Image(
//                                modifier = Modifier
//                                    .width(100.dp)
//                                    .height(100.dp)
//                                    .offset(25.dp, (-45).dp)
//                                    .clip(shape = CircleShape),
//                                contentScale = ContentScale.Crop,
//                                painter = painterResource(id = R.drawable.man),
//                                contentDescription = null,
//                            )
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 20.dp)
//                            ,
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(
//                                fontWeight = FontWeight.SemiBold,
//                                fontFamily = FontFamily.SansSerif,
//                                fontSize = 3.8.em,
//                                text = state.firstName+ " " + state.lastName
//                            )
//                            Row(
//                                horizontalArrangement = Arrangement.End,
//                            ){
//                                Icon(imageVector = if(state.gender == "Male") Icons.Filled.Male else Icons.Filled.Female, contentDescription = null, modifier = Modifier.size(20.dp),tint = MaterialTheme.colorScheme.secondary)
//                                Text(text = state.gender.toString(), fontSize = 14.sp)
//                            }
//                        }
//                    }
//                    Column(
//                        modifier = Modifier
//                            .offset(0.dp,(-30.dp))
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .padding(horizontal = 20.dp)
//                                .fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                fontWeight = FontWeight.SemiBold,
//                                fontFamily = FontFamily.SansSerif,
//                                fontSize = 3.4.em,
//                                text = "Pickup --- Destination"
//                            )
//                            Text(
//                                style = MaterialTheme.typography.bodySmall,
//                                text = state.distance +"km "+ state.duration
//                            )
//                        }
//                        Row(
//                            modifier = Modifier
//                                .padding(horizontal = 20.dp)
//                                .fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                fontWeight = FontWeight.SemiBold,
//                                fontFamily = FontFamily.SansSerif,
//                                fontSize = 3.4.em,
//                                text = "CurrentLocation --- Pickup"
//                            )
//                            Text(
//                                style = MaterialTheme.typography.bodySmall,
//                                text =  state.distanceFromDriver + "km " + state.durationFromDriver
//                            )
//                        }
//                        Row(
//                            modifier = Modifier
//                                .padding(horizontal = 20.dp)
//                                .fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                fontWeight = FontWeight.SemiBold,
//                                fontFamily = FontFamily.SansSerif,
//                                fontSize = 4.em,
//                                text = "Price"
//                            )
//                            Text(
//                                style = MaterialTheme.typography.bodySmall,
//                                text =  (state.distance!!.toDouble()  * priceOfFule * fulePerKm).roundToInt().toString() + " Rs"
//                            )
//                        }
//                        Row(
//                            modifier = Modifier
//                                .padding(top = 20.dp)
//                                .fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceEvenly
//                        ) {
//                            ElevatedButton(onClick = {
//                                viewModel.onEvent(TripEvents.userRejected)
//                            }) {
//                                Text(text = "Reject")
//                            }
//                            Button(onClick = {
//                                db.collection("ridesDetail").document(state.userUid!!)
//                                    .update("request","accepted").addOnSuccessListener {
//                                        Log.d("request","Accepted")
//                                    }
//
//                                viewModel.onEvent(TripEvents.userAccepted)
//                            }) {
//                                Text(text = "Accept")
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }
    when(state.currentScreen)
    {
        is TripScreen.ChatScreen -> {
            ChatUi(tripViewModel = viewModel, userUids = userUids,userType = userType, tripUserViewModel = null,userInfo = usersInfo[0].UserInfo)
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
                        .fillMaxWidth()
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
                            Icon(
                                Icons.Filled.Timeline, contentDescription = "Ride",
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(50.dp)
                            )
                            Text(text = (df.format(distance/1000)).toString()+"km" )
                        }
                        Column(modifier = Modifier
                            .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Timer, contentDescription = "Ride",
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(50.dp)
                            )
                            Text(text = ((duration/3600).toInt()).toString()+"hr "+(((duration % 3600) / 60).roundToInt()).toString()+"min" )
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