package com.simpdev.sahmride.Presentation.TripUser

import Domain.Data.*
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.trip.model.RouteProgress
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
import com.simpdev.sahmride.Presentation.Chat.ChatUi
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.Navigation.userInfo
import com.simpdev.sahmride.Presentation.Trip.TripUserEvents
import com.simpdev.sahmride.Presentation.Trip.TripUserScreen
import com.simpdev.sahmride.Presentation.Trip.TripUserViewModel
import com.simpdev.sahmride.R
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun TripUser(
    context: Context,
    pickup: Point?,
    destination: Point?,
    userUids: List<String>,
    userType: String,
    userInfo: userInfo,
    navigationViewModel: NavigationViewModel,
){
    val viewModel = viewModel<TripUserViewModel>()
    val state = viewModel.state
    
    LaunchedEffect(key1 = 1){
        viewModel.trackDriver(userUids[0])
    }

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
    routeOptions = RouteOptions.builder()
        .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
        .coordinatesList(listOf(
            driverCurrentLocation,
            pickup,
            destination
        ))
        .waypointNamesList(listOf("Driver", "Pickup", "Destination"))
        .build()
    customColorResources = RouteLineColorResources.Builder()
        .routeDefaultColor(android.graphics.Color.parseColor("#FF0000"))
        .routeCasingColor(android.graphics.Color.parseColor("#000000"))
        .inActiveRouteLegsColor(android.graphics.Color.parseColor("#FFCC00"))
        .build()

    routeLineResources = RouteLineResources.Builder()
        .routeLineColorResources(customColorResources)
        .build()
    routeLineOptions = MapboxRouteLineOptions.Builder(context)
        .styleInactiveRouteLegsIndependently(true)
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
    val routeProgressObserver = object : RouteProgressObserver {
        override fun onRouteProgressChanged(routeProgress: RouteProgress) {
            tripProgressApi.getTripProgress(routeProgress).let { update ->
                distance = update.distanceRemaining
                duration = update.totalTimeRemaining
            }
        }
    }


    LaunchedEffect(key1 = 1){
        Log.d("TripUser","LaunchedEffect")
        mapNav.registerRouteProgressObserver(routeProgressObserver)
        mapNav.registerRoutesObserver(routesObserver)
        mapNav.requestRoutes(routeOptions,naivgationRouterCallback)
//        mapView?.let { trackCurrentLocation(it, displayDensity) }
//        mapNav.startTripSession(true)
//        mapView?.let { flytoLocation(it, driverCurrentLocation) }
    }

    when(state.currentScreen)
    {
        is TripUserScreen.ChatScreen -> {
            ChatUi(
                tripViewModel = null,
                tripUserViewModel = viewModel,
                userUids = userUids,
                userType = userType,
                userInfo = userInfo
            )
        }
        is TripUserScreen.TripHome -> {
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

                            circleAnnotationManager = mapView!!.annotations.createCircleAnnotationManager()
                            drawCircularAnnotation(mapView!!, driverCurrentLocation!!)
                            mapNav.requestRoutes(routeOptions,naivgationRouterCallback)
                            mapView?.let { flytoLocation(it, driverCurrentLocation) }
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
                            viewModel.onEvent(TripUserEvents.chatClicked)
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
                            db.collection("ridesDetail").document(auth.currentUser!!.uid)
                                .update("request","cancelled").addOnSuccessListener {
                                    Log.d("request","cancelled")
                                }
                            val ref = database.reference.child("driversLocation").child(userUids[0]!!).child("users")
                            ref.setValue(null)
                            mapView?.onDestroy()
                            mapNav.onDestroy()
                            pickupLocation = null
                            destinationLocation = null
                            navigationViewModel.onEvent(NavigationEvent.rideCancelled)
                        }
                    ){
                       Text(text = "Cancel Ride")
                    }
                }
            }
        }
    }
}