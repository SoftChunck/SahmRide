package com.simpdev.sahmride.Presentation.TripUser

import Domain.Data.ApiService
import Domain.Data.auth
import Domain.Data.circleAnnotationManager
import Domain.Data.database
import Domain.Data.db
import Domain.Data.drawCircularAnnotation
import Domain.Data.driverCurrentLocation
import Domain.Data.flytoLocation
import Domain.Data.storageRef
import Domain.Data.userData
import android.content.Context
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
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.Navigation.userInfo
import com.simpdev.sahmride.Presentation.Review.ReviewUi
import com.simpdev.sahmride.Presentation.StreamChat.StreamChat
import com.simpdev.sahmride.Presentation.Trip.TripUserEvents
import com.simpdev.sahmride.Presentation.Trip.TripUserScreen
import com.simpdev.sahmride.Presentation.Trip.TripUserViewModel
import com.simpdev.sahmride.R
import com.simpdev.sahmride.customColorResources
import com.simpdev.sahmride.destinationLocation
import com.simpdev.sahmride.displayDensity
import com.simpdev.sahmride.mapNav
import com.simpdev.sahmride.mapView
import com.simpdev.sahmride.naivgationRouterCallback
import com.simpdev.sahmride.pickupLocation
import com.simpdev.sahmride.routeLineApi
import com.simpdev.sahmride.routeLineOptions
import com.simpdev.sahmride.routeLineResources
import com.simpdev.sahmride.routeLineView
import com.simpdev.sahmride.routeOptions
import com.simpdev.sahmride.routesObserver
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    navigationViewModel: NavigationViewModel,
    usersInfo: MutableList<userInfo>,
    waypoints: MutableList<Point>,
){
    val viewModel = viewModel<TripUserViewModel>()
    val state = viewModel.state
    
    LaunchedEffect(key1 = 1){

        state.driverUid = userUids[0]
        viewModel.addListinerForUsers()
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
        .coordinatesList(waypoints)
//        .waypointNamesList(listOf("Driver", "Pickup", "Destination"))
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
                        Log.d("Chat","Error")

                    }
                }
            }
        } catch (e: Exception) {
            // Handle the exception
        }
    })

    LaunchedEffect(key1 = 1){
        Log.d("TripUser","LaunchedEffect")
        mapNav.registerRouteProgressObserver(routeProgressObserver)
        mapNav.registerRoutesObserver(routesObserver)
        mapNav.requestRoutes(routeOptions,naivgationRouterCallback)
//        mapView?.let { trackCurrentLocation(it, displayDensity) }
//        mapNav.startTripSession(true)
//        mapView?.let { flytoLocation(it, driverCurrentLocation) }
    }
    if(state.usersAvalible){
        state.rideSharingRideDetails.forEach {
            if(it.request == "pending" || it.request == "accepted")
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
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                ){
                                    Icon(imageVector = if(it.UserInfo.gender == "Male") Icons.Filled.Male else Icons.Filled.Female, contentDescription = null, modifier = Modifier.size(20.dp),tint = MaterialTheme.colorScheme.secondary)
                                    Text(text = it.UserInfo.gender.toString(), fontSize = 14.sp)
                                }
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
                            }
                        }
                        Column(
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
                                        text = "${it.distance}km" ,
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
                                    viewModel.onEvent(TripUserEvents.userRejected)
                                }) {
                                    Text(text = "Reject")
                                }
                                Button(onClick = {
                                    db.collection("ridesDetail").document(it.UserInfo.userUid!!)
                                        .update(auth.currentUser!!.uid,"accepted").addOnSuccessListener {
                                            Log.d("request","Accepted")
                                        }
                                    db.collection("ridesDetail").document(it.UserInfo.userUid!!).get().addOnSuccessListener {req ->
                                        it.request = req.get("request").toString()
                                        viewModel.onEvent(TripUserEvents.userAccepted(it.request))
                                    }
                                }) {
                                    Text(text = "Accept")
                                }
                            }
                        }
                    }
                }
            }
//            {
//                Column(
//                    modifier = Modifier
//                        .zIndex(7f)
//                        .fillMaxSize(),
//                    verticalArrangement = Arrangement.Bottom
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .fillMaxHeight(0.35f)
//                            .background(
//                                shape = RoundedCornerShape(
//                                    topStart = 25.dp,
//                                    topEnd = 25.dp
//                                ),
//                                color = MaterialTheme.colorScheme.secondaryContainer
//                            ),
//                        verticalArrangement = Arrangement.Top,
//                        horizontalAlignment = Alignment.Start
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            if(it.UserInfo.userPic != null)
//                                Image(
//                                    modifier = Modifier
//                                        .width(100.dp)
//                                        .height(100.dp)
//                                        .offset(25.dp, (-45).dp)
//                                        .clip(shape = CircleShape),
//                                    contentScale = ContentScale.Crop,
//                                    bitmap = it.UserInfo.userPic!!,
//                                    contentDescription = null
//                                )
//                            else
//                                Image(
//                                    modifier = Modifier
//                                        .width(100.dp)
//                                        .height(100.dp)
//                                        .offset(25.dp, (-45).dp)
//                                        .clip(shape = CircleShape),
//                                    contentScale = ContentScale.Crop,
//                                    painter = painterResource(id = R.drawable.man),
//                                    contentDescription = null,
//                                )
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(top = 20.dp)
//                                ,
//                                horizontalAlignment = Alignment.CenterHorizontally
//                            ) {
//                                Text(
//                                    fontWeight = FontWeight.SemiBold,
//                                    fontFamily = FontFamily.SansSerif,
//                                    fontSize = 3.8.em,
//                                    text = it.UserInfo.firstName+ " " + it.UserInfo.lastName
//                                )
//                                Row(
//                                    horizontalArrangement = Arrangement.End,
//                                ){
//                                    Icon(imageVector = if(it.UserInfo.gender == "Male") Icons.Filled.Male else Icons.Filled.Female, contentDescription = null, modifier = Modifier.size(20.dp),tint = MaterialTheme.colorScheme.secondary)
//                                    Text(text = it.UserInfo.gender.toString(), fontSize = 14.sp)
//                                }
//                            }
//                        }
//                        Column(
//                            modifier = Modifier
//                                .offset(0.dp,(-30.dp))
//                        ) {
//                            Row(
//                                modifier = Modifier
//                                    .padding(horizontal = 20.dp)
//                                    .fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                Text(
//                                    fontWeight = FontWeight.SemiBold,
//                                    fontFamily = FontFamily.SansSerif,
//                                    fontSize = 3.4.em,
//                                    text = "Pickup --- Destination"
//                                )
//                                Text(
//                                    style = MaterialTheme.typography.bodySmall,
//                                    text = it.distance +"km "+ it.duration
//                                )
//                            }
//                            Row(
//                                modifier = Modifier
//                                    .padding(horizontal = 20.dp)
//                                    .fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                Text(
//                                    fontWeight = FontWeight.SemiBold,
//                                    fontFamily = FontFamily.SansSerif,
//                                    fontSize = 3.4.em,
//                                    text = "CurrentLocation --- Pickup"
//                                )
//                                Text(
//                                    style = MaterialTheme.typography.bodySmall,
//                                    text =  it.distanceFromDriver + "km " + it.durationFromDriver
//                                )
//                            }
//                            Row(
//                                modifier = Modifier
//                                    .padding(horizontal = 20.dp)
//                                    .fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                Text(
//                                    fontWeight = FontWeight.SemiBold,
//                                    fontFamily = FontFamily.SansSerif,
//                                    fontSize = 4.em,
//                                    text = "Price"
//                                )
//                                Text(
//                                    style = MaterialTheme.typography.bodySmall,
//                                    text =  "${it.price} Rs"
//                                )
//                            }
//                            Row(
//                                modifier = Modifier
//                                    .padding(top = 20.dp)
//                                    .fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceEvenly
//                            ) {
//                                ElevatedButton(onClick = {
//                                    viewModel.onEvent(TripUserEvents.userRejected)
//                                }) {
//                                    Text(text = "Reject")
//                                }
//                                Button(onClick = {
//                                    db.collection("ridesDetail").document(it.UserInfo.userUid!!)
//                                        .update(auth.currentUser!!.uid,"accepted").addOnSuccessListener {
//                                            Log.d("request","Accepted")
//                                        }
//                                    it.request = "accepted"
//                                    viewModel.onEvent(TripUserEvents.userAccepted(it.request))
//                                }) {
//                                    Text(text = "Accept")
//                                }
//                            }
//                        }
//                    }
//                }
//            }
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
        is TripUserScreen.ReviewScreen -> {
            viewModel.saveRideDetailsToHistory()
            ReviewUi(
                usersInfo[0],
                navigationViewModel
            )
        }
        is TripUserScreen.ChatScreen -> {
//            ChatUi(
//                tripViewModel = null,
//                tripUserViewModel = viewModel,
//                userUids = userUids,
//                userType = userType,
//                userInfo = usersInfo[0]
//            )
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
                "messaging:"+ auth.currentUser!!.uid.toString(),99
            )

            ChatClient.instance().getCurrentUser()?.let {
                ChatTheme {
                    UserAvatar(user = it)
                }
            }
            StreamChat(tirpuserViewModel = viewModel)
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
                
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    ElevatedButton(
                        onClick = {
                            db.collection("ridesDetail").document(auth.currentUser!!.uid)
                                .update("request","cancelled").addOnSuccessListener {
                                    Log.d("request","cancelled")
                                }
                            db.collection("ridesDetail").document(auth.currentUser!!.uid)
                                .get().addOnSuccessListener { ride->
                                    val ref = database.reference.child("driversLocation").child(ride.getString("driverUid").toString()).child("users").child(
                                        auth.currentUser!!.uid)
                                    ref.setValue(null)
                                }
                            mapView?.onDestroy()
                            mapNav.onDestroy()
                            pickupLocation = null
                            destinationLocation = null
                            navigationViewModel.onEvent(NavigationEvent.rideCancelled)
                        }
                    ){
                       Text(text = "Cancel Ride")
                    }
                    ElevatedButton(
                        onClick = {
                            db.collection("ridesDetail").document(auth.currentUser!!.uid)
                                .update("request","completed").addOnSuccessListener {
                                    Log.d("request","Completed")
                                }
                            mapView?.onDestroy()
                            mapNav.onDestroy()
                            pickupLocation = null
                            destinationLocation = null
                        }
                    ){
                        Text(text = "Completed")
                    }
                }
            }
        }
    }
}