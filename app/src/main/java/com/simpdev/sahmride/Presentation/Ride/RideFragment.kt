@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package com.simpdev.sahmride

import Domain.Data.*
import DriverProfileUi
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
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
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.simpdev.sahmride.Domain.Data.AvalibleDriverData
import com.simpdev.sahmride.Domain.Data.DistanceLocation
import com.simpdev.sahmride.Domain.Data.UserPrice
import com.simpdev.sahmride.Domain.Data.mapStyle
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.Ride.RideEvent
import com.simpdev.sahmride.Presentation.Ride.RideScreen
import com.simpdev.sahmride.Presentation.Ride.RideViewModel
import com.simpdev.sahmride.Presentation.Trip.TripUi
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

var mapView: MapView? = null
var pickupLocation: Point? = null
var destinationLocation: Point? = null

//Route Line
lateinit var routeOptions: RouteOptions
lateinit var routeLineOptions : MapboxRouteLineOptions
lateinit var routeLineApi : MapboxRouteLineApi
lateinit var routeLineView : MapboxRouteLineView

//RouteLine Color
lateinit var customColorResources : RouteLineColorResources
lateinit var routeLineResources : RouteLineResources

lateinit var routesObserver : RoutesObserver
lateinit var naivgationRouterCallback : NavigationRouterCallback
lateinit var mapNav : MapboxNavigation

var displayDensity:Float = 0f

var distanceGlobal:Double = 0.0
var durationGlobal:Double = 0.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pickup(
    context: Context,
    defaultSelectedIndex: Int,
    navigationViewModel: NavigationViewModel,
    innerPadding: PaddingValues
){
    val viewModel = viewModel<RideViewModel>()
    val state = viewModel.state
    var selectedTabIndex by remember { mutableStateOf(defaultSelectedIndex) }
    val focusManager = LocalFocusManager.current
    var PickUpPlace by remember { mutableStateOf("") }
    var hideSuggestion by remember { mutableStateOf(true) }
    var searchSuggestions by remember { mutableStateOf(listOf<SearchSuggestion>()) }
    val searchEngine = SearchEngine.createSearchEngine(
        SearchEngineSettings(stringResource(id = R.string.mapbox_access_token))
    )
    val accessToken = stringResource(id = R.string.mapbox_access_token)

    when(state.currentRideScreen){
        is RideScreen.TripScreen -> {
            TripUi(
                context = context,
                pickup = pickupLocation,
                destination = destinationLocation,
                userUids = listOf<String>(
                    driverUid!!),
                userType = "User",
                usersInfo = navigationViewModel.state.rideSharingRideDetails,
                rideViewModel = viewModel,
            )
        }
        is RideScreen.RideHome -> {
            Box {
                AndroidView(modifier = Modifier
                    .fillMaxSize(),
                    factory = {
                        if(mapView == null || navigationViewModel.state.rideStatus == "cancelled")
                        {
                            mapView = MapView(it).apply {
                                getMapboxMap().loadStyleUri(
                                    if(mapStyle == "Dark")
                                        "mapbox://styles/softchunck/cladvz5h5000614l826zzjojd"
                                    else if(mapStyle == "Light")
                                        "mapbox://styles/softchunck/clbahksoh000214pd2umczega"
                                    else
                                        Style.MAPBOX_STREETS
                                ){
                                    cameraOptions {
                                        zoom(19.0)
                                    }
                                }
                            }
                            mapNav = MapboxNavigation(
                                NavigationOptions.Builder(it)
                                    .accessToken(accessToken)
                                    .build())
                        }
                        displayDensity = it.resources.displayMetrics.density
                        mapView!!
                    }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ){
                if(selectedTabIndex != 3)
                {
                    Box{
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = "current location",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            Box{
                Column(modifier = Modifier
                    .fillMaxWidth(),
                    verticalArrangement = Arrangement.Top) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTabIndex ==  1,
                            onClick = {
                                selectedTabIndex =  1
                            },
                            icon = {
                                Icon(
                                    if(selectedTabIndex == 1) Icons.Filled.EditLocation else Icons.Filled.Done,
                                    contentDescription = "" )
                            },
                            label = { Text(text = "PickUp") }
                        )
                        NavigationBarItem(
                            selected = selectedTabIndex ==  2,
                            onClick = {
                                if(pickupLocation != null)
                                {
                                    selectedTabIndex =  2
                                }
                            },
                            icon = {
                                Icon(
                                    if(selectedTabIndex == 2) Icons.Filled.EditLocation else if(selectedTabIndex == 3) Icons.Filled.Done else Icons.Filled.AddLocation,
                                    contentDescription = "" )
                            },
                            label = { Text(text = "Destination") }
                        )
                        NavigationBarItem(
                            selected = selectedTabIndex ==  3,
                            onClick = {
                                if(destinationLocation != null)
                                {
                                    selectedTabIndex = 3
                                }
                            },
                            icon = {
                                Icon(Icons.Filled.Map, contentDescription = "" )
                            },
                            label = { Text(text = "RouteInfo") }
                        )
                    }
                    if(selectedTabIndex != 3){
                        TextField(value = PickUpPlace,
                            onValueChange = {
                                PickUpPlace = it
                                searchEngine.search(
                                    PickUpPlace,
                                    SearchOptions(limit = 5),
                                    object : SearchSuggestionsCallback {

                                        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
//                            val suggestion = suggestions.firstOrNull()
                                            searchSuggestions = listOf()
                                            hideSuggestion = false
                                            suggestions.forEach {
                                                    suggestion ->
                                                searchSuggestions = searchSuggestions + suggestion
                                            }
                                        }

                                        override fun onError(e: Exception) {

                                        }
                                    }
                                )
                            },
                            shape = RoundedCornerShape(0.dp),
                            colors = TextFieldDefaults.textFieldColors(containerColor = MaterialTheme.colorScheme.background,focusedIndicatorColor = Color.Transparent,unfocusedIndicatorColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(40.dp)
                                .background(
                                    color = Color.White
                                ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            keyboardActions = KeyboardActions( onDone = {
                                focusManager.clearFocus(true)
                            }),
                            singleLine = true,
                            leadingIcon =  {
                                Icon(
                                    Icons.Filled.MyLocation,
                                    contentDescription = "Current Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable(enabled = true, onClick = {
                                            mapView?.let { trackCurrentLocation(it, displayDensity) }
                                        })
                                )
                            },
                            trailingIcon = {
                                Icon(Icons.Filled.Done, contentDescription = "",
                                    modifier = Modifier
                                        .clickable(enabled = true, onClick = {
                                            if(selectedTabIndex == 1)
                                            {
                                                pickupLocation = mapView?.getMapboxMap()?.cameraState?.center
                                                PickUpPlace = ""
                                                searchSuggestions = listOf()
                                                selectedTabIndex = 2
                                            }
                                            else{
                                                destinationLocation = mapView?.getMapboxMap()?.cameraState?.center
                                                PickUpPlace = ""
                                                searchSuggestions = listOf()
                                                selectedTabIndex = 3
                                            }
                                        }))
                            },
                            placeholder = {
                                if(selectedTabIndex == 1)
                                    Text(text = "Select Pickup ... ", color = Color.Gray)
                                else
                                    Text(text = "Select Destination ... ", color = Color.Gray)
                            }
                        )

                        searchSuggestions.forEach { suggestion ->
                            if(!hideSuggestion)
                            {
                                ListItem(
                                    headlineText = { Text(text = suggestion.name) },
                                    supportingText = { Text(text = suggestion.address?.region.toString()+","+suggestion.address?.country.toString())
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = "Location",
                                        )
                                    },
                                    trailingContent = {
                                        Icon(
                                            Icons.Filled.ArrowForward,
                                            contentDescription = "Select",
                                        )
                                    },
                                    colors = ListItemDefaults.colors(MaterialTheme.colorScheme.background),
                                    modifier = Modifier
                                        .clickable {
                                            searchEngine.select(suggestion,
                                                object : SearchSelectionCallback {
                                                    override fun onResult(
                                                        suggestion: SearchSuggestion,
                                                        result: SearchResult,
                                                        responseInfo: ResponseInfo
                                                    ) {
                                                        val coordinates = result.coordinate
//                                                            drawCircularAnnotation(mapView,coordinates)
                                                        mapView?.let { flytoLocation(it,coordinates) }
                                                        hideSuggestion = true
                                                    }

                                                    override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                                                    }

                                                    override fun onCategoryResult(
                                                        suggestion: SearchSuggestion,
                                                        results: List<SearchResult>,
                                                        responseInfo: ResponseInfo
                                                    ) {
                                                    }

                                                    override fun onError(e: Exception) {
                                                    }
                                                })
                                        }
                                )
                                Divider()
                            }
                        }
                    }
                    else{
                        Routeinfo(context = context,viewModel, navigationViewModel)
                    }
                }
            }
        }
        is RideScreen.DriverProfileScreen -> {
            state.selectedDriverData?.let {
                DriverProfileUi(
                    driverData = it,
                    rideViewModel = viewModel,
                    navigationViewModel = navigationViewModel,
                    distance = distanceGlobal,
                    duration = durationGlobal,
                ) }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Routeinfo(context: Context,viewModel: RideViewModel,navigationViewModel: NavigationViewModel)
{
    var driversSuggestion by remember { mutableStateOf(ArrayList<AvalibleDriverData>().toMutableStateList()) }
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
    var searchdriver by remember { mutableStateOf(false) }
    //Route Line
    routeOptions = RouteOptions.builder()
        .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
        .coordinatesList(listOf(
            pickupLocation.let { it?.let { it1 -> Point.fromLngLat(it1.longitude(),it.latitude()) } },
            destinationLocation.let { it?.let { it1 -> Point.fromLngLat(it1.longitude(),it.latitude()) } }))
        .build()
    customColorResources = RouteLineColorResources.Builder()
        .routeDefaultColor(android.graphics.Color.parseColor("#000000"))
        .build()
    routeLineResources = RouteLineResources.Builder()
        .routeLineColorResources(customColorResources)
        .build()
    routeLineOptions = MapboxRouteLineOptions.Builder(context)
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
            distanceGlobal = jsonObj.get("distance").asDouble
            durationGlobal = jsonObj.get("duration").asDouble
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
    mapNav.registerRouteProgressObserver(routeProgressObserver)
    mapNav.registerRoutesObserver(routesObserver)

    mapNav.requestRoutes(routeOptions,naivgationRouterCallback)
    mapView?.let { flytoLocation(it, pickupLocation) }
//                    drawCircularAnnotation(mapView, pickupLocation)
//                    drawCircularAnnotation(mapView, destinationLocation)
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                )
                .verticalScroll(state = rememberScrollState(), enabled = true),
            horizontalAlignment = Alignment.CenterHorizontally
            ){
            Text(
                modifier = Modifier.padding(vertical = 15.dp),
                text = "Ride Details",
                fontSize = 5.em,
                fontWeight = FontWeight.SemiBold
                )
            Row (
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 9.dp)
                    .fillMaxWidth(0.8f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                Image(
                    painter = painterResource(id = R.drawable.ridedetails),
                    modifier = Modifier
                        .width(32.dp)
                        .height(32.dp)
                    ,
                    contentScale = ContentScale.Crop,
                    contentDescription = ""
                )
                Text(modifier = Modifier.padding( vertical = 10.dp),text = (df.format(distance/1000)).toString()+"km  ", fontWeight = FontWeight.SemiBold )
                Text(text = ((duration/3600).toInt()).toString()+"hr "+(((duration % 3600) / 60).roundToInt()).toString()+"min", fontWeight = FontWeight.SemiBold )
            }
        if(!searchdriver){
            Button(
                onClick = {
                var driversRef = database.reference.child("driversLocation")
                driversRef.get()
                    .addOnSuccessListener {
                        it.children.forEach { driver ->
                            var avalibleDriver = AvalibleDriverData(driverUid = driver.key.toString())
                                if(driver.child("Active").value.toString() == "true" && driver.child("lng").value != null && driver.child("lat").value != null)
                                {
                                    if((driver.child("availableSeats").value.toString()).toInt() > driver.child("users").childrenCount)
                                    {
                                        val locations:MutableList<DistanceLocation> = emptyList<DistanceLocation>().toMutableList()

                                        var driverLocation = Point.fromLngLat(
                                            driver.child("lng").value.toString().toDouble(),
                                            driver.child("lat").value.toString().toDouble()
                                        )
                                        locations.add(DistanceLocation(driverLocation))
                                        locations.add(DistanceLocation(pickupLocation!!))
                                        locations.add(DistanceLocation(destinationLocation!!))

                                        mapNav.requestRoutes( RouteOptions.builder()
                                            .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                            .coordinatesList(
                                                listOf(driverLocation,
                                                pickupLocation,
                                                destinationLocation)
                                            )
                                            .build(),
                                            object  : NavigationRouterCallback {
                                                override fun onCanceled(
                                                    routeOptions: RouteOptions,
                                                    routerOrigin: RouterOrigin
                                                ) {}

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
                                                    avalibleDriver.price = ((df.format(jsonObj.get("distance").asDouble/1000)).toDouble() * fulePerKm * priceOfFule).roundToInt()
                                                    if(driver.child("users").childrenCount > 0)
                                                    {
                                                        var priceOtherUser:Int = 0
                                                        var additionlDistance:Double = 0.0
                                                        var additionlDuration:Double = 0.0
                                                        driver.child("users").children.forEach {
                                                            val ref = db.collection("ridesDetail").document(it.key!!)
                                                            avalibleDriver.otherUsers = UserPrice(userUid = it.key!!)
                                                            avalibleDriver.otherUsers = UserPrice(userUid = it.key!!)
                                                            var orref = db.collection("users").document(it.key!!)
                                                            orref.get()
                                                                .addOnSuccessListener { result ->
                                                                    if(result != null ) {
                                                                        storageRef.child("images/${it.key!!}/profile").downloadUrl.addOnSuccessListener {
                                                                            avalibleDriver.otherUsers!!.profilePic = it
                                                                        }
                                                                        avalibleDriver.otherUsers!!.firstName = result.get("firstName").toString()
                                                                        avalibleDriver.otherUsers!!.lastName = result.get("lastName").toString()
                                                                        avalibleDriver.otherUsers!!.gender = result.get("gender").toString()
                                                                        avalibleDriver.otherUsers!!.rating = (result.get("rating").toString()).toDouble()
                                                                    }
                                                                }
                                                            ref.get().addOnSuccessListener { result ->
                                                                if(result != null ){
                                                                    locations.add(
                                                                        DistanceLocation(
                                                                            locationPoint = Point.fromLngLat(result.get("pickupLng") as Double,result.get("pickupLat") as Double)
                                                                        )
                                                                    )
                                                                    locations.add(
                                                                        DistanceLocation(
                                                                            locationPoint = Point.fromLngLat(result.get("destinationLng") as Double,result.get("destinationLat") as Double)
                                                                        )
                                                                    )
                                                                    priceOtherUser = (result.get("price").toString()).toInt()
                                                                    val totalprice = avalibleDriver.price + priceOtherUser
                                                                    val percOther:Double = (priceOtherUser.toDouble()/totalprice.toDouble())
                                                                    val percCurrent:Double = (avalibleDriver.price.toDouble()/totalprice.toDouble())

                                                                    mapNav.requestRoutes( RouteOptions.builder()
                                                                        .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                                                        .coordinatesList(listOf(
                                                                            locations[0].locationPoint,locations[3].locationPoint,locations[4].locationPoint,
                                                                        ))
                                                                        .build(),
                                                                        object  : NavigationRouterCallback {
                                                                            override fun onCanceled(
                                                                                routeOptions: RouteOptions,
                                                                                routerOrigin: RouterOrigin
                                                                            ) {}
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
                                                                                GsonBuilder().setPrettyPrinting().create()
                                                                                val json = JsonParser.parseString(routes[0].directionsRoute.toJson())
                                                                                val jsonObj = json.asJsonObject
                                                                                additionlDistance = (df.format(jsonObj.get("distance").asDouble/1000)).toDouble()
                                                                                mapNav.requestRoutes( RouteOptions.builder()
                                                                                    .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                                                                    .coordinatesList(listOf(
                                                                                        locations[0].locationPoint,locations[1].locationPoint
                                                                                    ))
                                                                                    .build(),
                                                                                    object  : NavigationRouterCallback {
                                                                                        override fun onCanceled(
                                                                                            routeOptions: RouteOptions,
                                                                                            routerOrigin: RouterOrigin
                                                                                        ) {}
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
                                                                                            avalibleDriver.distance = (df.format(jsonObj.get("distance").asDouble/1000)).toString()
                                                                                            avalibleDriver.duration = ((jsonObj.get("duration").asDouble/3600).toInt()).toString()+"hr "+(((jsonObj.get("duration").asDouble % 3600) / 60).roundToInt()).toString()+"min"
                                                                                            locations[1].distance = (df.format(jsonObj.get("distance").asDouble/1000)).toDouble()
//                                                                    additionlDuration = jsonObj.get("duration").asDouble
                                                                                            mapNav.requestRoutes( RouteOptions.builder()
                                                                                                .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                                                                                .coordinatesList(listOf(
                                                                                                    locations[0].locationPoint,locations[2].locationPoint
                                                                                                ))
                                                                                                .build(),
                                                                                                object  : NavigationRouterCallback {
                                                                                                    override fun onCanceled(
                                                                                                        routeOptions: RouteOptions,
                                                                                                        routerOrigin: RouterOrigin
                                                                                                    ) {}
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

                                                                                                        locations[2].distance = (df.format(jsonObj.get("distance").asDouble/1000)).toDouble()
//                                                                    additionlDuration = jsonObj.get("duration").asDouble
                                                                                                        mapNav.requestRoutes( RouteOptions.builder()
                                                                                                            .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                                                                                            .coordinatesList(listOf(
                                                                                                                locations[0].locationPoint,locations[3].locationPoint
                                                                                                            ))
                                                                                                            .build(),
                                                                                                            object  : NavigationRouterCallback {
                                                                                                                override fun onCanceled(
                                                                                                                    routeOptions: RouteOptions,
                                                                                                                    routerOrigin: RouterOrigin
                                                                                                                ) {}
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

                                                                                                                    locations[3].distance = (df.format(jsonObj.get("distance").asDouble/1000)).toDouble()
                                                                                                                    mapNav.requestRoutes( RouteOptions.builder()
                                                                                                                        .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                                                                                                        .coordinatesList(listOf(
                                                                                                                            locations[0].locationPoint,locations[4].locationPoint
                                                                                                                        ))
                                                                                                                        .build(),
                                                                                                                        object  : NavigationRouterCallback {
                                                                                                                            override fun onCanceled(
                                                                                                                                routeOptions: RouteOptions,
                                                                                                                                routerOrigin: RouterOrigin
                                                                                                                            ) {}
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

                                                                                                                                locations[4].distance = (df.format(jsonObj.get("distance").asDouble/1000)).toDouble()
//                                                                    additionlDuration = jsonObj.get("duration").asDouble
                                                                                                                                locations.sortBy {
                                                                                                                                    it.distance
                                                                                                                                }
                                                                                                                                mapNav.requestRoutes( RouteOptions.builder()
                                                                                                                                    .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                                                                                                                    .coordinatesList(listOf(
                                                                                                                                        locations[0].locationPoint,locations[1].locationPoint,locations[2].locationPoint,locations[3].locationPoint,locations[4].locationPoint,
                                                                                                                                    ))
                                                                                                                                    .build(),
                                                                                                                                    object  : NavigationRouterCallback {
                                                                                                                                        override fun onCanceled(
                                                                                                                                            routeOptions: RouteOptions,
                                                                                                                                            routerOrigin: RouterOrigin
                                                                                                                                        ) {}
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

                                                                                                                                            additionlDistance = ((df.format(jsonObj.get("distance").asDouble/1000)).toDouble()-additionlDistance)
                                                                                                                                            //                                                                    additionlDuration = jsonObj.get("duration").asDouble
                                                                                                                                            Log.d("additionlDistance",additionlDistance.toString())
                                                                                                                                            if(additionlDistance < 8){
                                                                                                                                                var ref = db.collection("users").document(avalibleDriver.driverUid!!)
                                                                                                                                                ref.get()
                                                                                                                                                    .addOnSuccessListener { result ->
                                                                                                                                                        if(result != null ){
                                                                                                                                                            avalibleDriver.additionlDistance = additionlDistance
                                                                                                                                                            avalibleDriver.firstName = result.get("firstName") as String
                                                                                                                                                            avalibleDriver.vehicleModel = result.get("vehicleModel") as String
                                                                                                                                                            avalibleDriver.vehicleName = result.get("vehicleName") as String
                                                                                                                                                            avalibleDriver.lastName = result.get("lastName") as String
                                                                                                                                                            avalibleDriver.gender = result.get("gender") as String
                                                                                                                                                            avalibleDriver.rating = (result.get("rating").toString()).toDouble()
                                                                                                                                                            avalibleDriver.carType = (result.get("carType").toString()).toInt()
                                                                                                                                                            avalibleDriver.waypoint = locations

                                                                                                                                                            mapNav.requestRoutes( RouteOptions.builder()
                                                                                                                                                                .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                                                                                                                                                .coordinatesList(
                                                                                                                                                                    listOf(
                                                                                                                                                                        locations[0].locationPoint,
                                                                                                                                                                        locations[1].locationPoint,
                                                                                                                                                                        locations[2].locationPoint,
                                                                                                                                                                        locations[3].locationPoint,
                                                                                                                                                                        locations[4].locationPoint,
                                                                                                                                                                        )
                                                                                                                                                                )
                                                                                                                                                                .build(),
                                                                                                                                                                object  : NavigationRouterCallback {
                                                                                                                                                                    override fun onCanceled(
                                                                                                                                                                        routeOptions: RouteOptions,
                                                                                                                                                                        routerOrigin: RouterOrigin
                                                                                                                                                                    ) {}

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
                                                                                                                                                                        val newTotalPrice = ((df.format(jsonObj.get("distance").asDouble/1000)).toDouble() * fulePerKm * priceOfFule).roundToInt()
                                                                                                                                                                        priceOtherUser = (percOther * newTotalPrice).roundToInt()
                                                                                                                                                                        avalibleDriver.otherUsers!!.price = priceOtherUser
                                                                                                                                                                        avalibleDriver.price = (percCurrent * newTotalPrice).roundToInt()
                                                                                                                                                                        driversSuggestion.add(avalibleDriver)
                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                            )
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                    .addOnFailureListener { exception ->
                                                                                                                                                        Log.w( "Error", "Error getting documents.", exception)
                                                                                                                                                    }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                )
                                                                                                                            }
                                                                                                                        }
                                                                                                                    )
                                                                                                                }
                                                                                                            }
                                                                                                        )
                                                                                                    }
                                                                                                }
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                )
                                                                            }
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        avalibleDriver.location = driverLocation
                                                    }
                                                    else
                                                    {
                                                        locations[2].distance = (df.format(distance/1000)).toDouble()
                                                        avalibleDriver.location = driverLocation
                                                        mapNav.requestRoutes( RouteOptions.builder()
                                                            .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                                            .coordinatesList(listOf(
                                                                pickupLocation.let { it?.let { it1 -> Point.fromLngLat(it1.longitude(),it.latitude()) } },
                                                                driverLocation.let { it?.let { it1 -> Point.fromLngLat(it1.longitude(),it.latitude()) } }))
                                                            .build(),
                                                            object  : NavigationRouterCallback {
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

                                                                    avalibleDriver.distance = (df.format(jsonObj.get("distance").asDouble/1000)).toString()
                                                                    avalibleDriver.duration = ((jsonObj.get("duration").asDouble/3600).toInt()).toString()+"hr "+(((jsonObj.get("duration").asDouble % 3600) / 60).roundToInt()).toString()+"min"
                                                                    locations[1].distance = (df.format(jsonObj.get("distance").asDouble/1000)).toDouble()
                                                                    var ref = db.collection("users").document(avalibleDriver.driverUid!!)
                                                                    ref.get()
                                                                        .addOnSuccessListener { result ->
                                                                            if(result != null ){
                                                                                avalibleDriver.firstName = result.get("firstName") as String
                                                                                avalibleDriver.vehicleModel = result.get("vehicleModel") as String
                                                                                avalibleDriver.vehicleName = result.get("vehicleName") as String
                                                                                avalibleDriver.lastName = result.get("lastName") as String
                                                                                avalibleDriver.gender = result.get("gender") as String
                                                                                avalibleDriver.rating = (result.get("rating").toString()).toDouble()
                                                                                avalibleDriver.carType = (result.get("carType").toString()).toInt()
                                                                                avalibleDriver.waypoint = locations
                                                                                driversSuggestion.add(avalibleDriver)
                                                                            }
                                                                        }
                                                                        .addOnFailureListener { exception ->
                                                                            Log.w( "Error", "Error getting documents.", exception)
                                                                        }
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                        Log.d("ChildCount",driver.child("users").childrenCount.toString())

                                    }
                                }
                            }
                    }
                searchdriver = true
            }) {
                Text(text = "Search For Driver")
            }
        }
        else{
            driversSuggestion.forEach { suggestion ->
                ListItem(
                    headlineText = { suggestion.firstName?.let { Text(text = it) } },
                    supportingText = { Text(text = suggestion.distance.toString()+"km"+","+suggestion.duration.toString(),
                        color = Color.Gray)
                    },
                    leadingContent = {
                        Image(
                            painter = painterResource(id = when(suggestion.carType){
                                1 -> R.drawable.icon1
                                2 -> R.drawable.icon2
                                3 -> R.drawable.icon3
                                4 -> R.drawable.icon4
                                5 -> R.drawable.icon5
                                6 -> R.drawable.icon6
                                else -> {R.drawable.icon6}
                            }),
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp)
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(20.dp)
                                )
                            ,
                            contentScale = ContentScale.Crop,
                            contentDescription = ""
                        )
                    },
                    trailingContent = {
                                      Text(
                                          text = "PKR "+suggestion.price.toString(),
                                          fontSize = 4.em,
                                          fontWeight = FontWeight.SemiBold,
                                          color = MaterialTheme.colorScheme.onBackground
                                      )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .clickable(
                            enabled = true,
                            onClick = {
                                viewModel.onEvent(RideEvent.driverProfile(suggestion))
                            }
                        )
                        .background(
                            color = Color.Transparent
                        )
                )
            }
        }
        }
    }
}


