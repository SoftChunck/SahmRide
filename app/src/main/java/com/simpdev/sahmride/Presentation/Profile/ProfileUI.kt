
import Domain.Data.*
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.simpdev.sahmride.Domain.Data.AvalibleDriverData
import com.simpdev.sahmride.Domain.RideDetails
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.Presentation.Navigation.NavigationScreen
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.Navigation.userInfo
import com.simpdev.sahmride.Presentation.Profile.ProfileViewModel
import com.simpdev.sahmride.Presentation.Ride.RideEvent
import com.simpdev.sahmride.Presentation.Ride.RideScreen
import com.simpdev.sahmride.Presentation.Ride.RideViewModel
import com.simpdev.sahmride.R
import com.simpdev.sahmride.destinationLocation
import com.simpdev.sahmride.pickupLocation
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

@OptIn(ExperimentalCoilApi::class)
@Composable
fun DriverProfileUi(
    driverData: AvalibleDriverData,
    rideViewModel: RideViewModel,
    navigationViewModel: NavigationViewModel,
    distance: Double,
    duration: Double,
    innerPadding: PaddingValues
){
    val viewModel = viewModel<ProfileViewModel>()
    val state = viewModel.state

    LaunchedEffect(key1 = 1, block = {
//        viewModel.resetState()
        viewModel.loadProfilePic(driverData.driverUid!!)
        viewModel.loadVehiclePics(driverData.driverUid!!)
        viewModel.loadReviews(driverData.driverUid!!)
    })
    Box(
        modifier = Modifier
            .zIndex(7f)
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                FloatingActionButton(onClick = {
                    rideViewModel.onEvent(RideEvent.changeCurrentRideScreen(RideScreen.RideHome))
                }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                }
                FloatingActionButton(onClick =
                {
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN

                    val usersKeys:MutableList<String> = emptyList<String>().toMutableList()

                    auth.currentUser?.uid?.let {
                        driverUid =driverData.driverUid
                        database.reference.child("driversLocation").child(driverData.driverUid!!).child("users").get()
                            .addOnSuccessListener {
                                if(it.childrenCount > 0)
                                {
                                    it.children.forEach {
                                        usersKeys.add(it.key.toString())
                                    }
                                    database.reference.child("driversLocation").child(driverData.driverUid!!).child("users").child(auth.currentUser!!.uid).setValue("0.0")
                                    database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("genratedWaypoints").setValue(null)
                                    driverData.waypoint?.forEachIndexed { index, distanceLocation ->
                                        database.reference.child("driversLocation").child(driverData.driverUid!!).child("genratedWaypoints").child(
                                            index.toString()
                                        ).child("lng").setValue(distanceLocation.locationPoint.longitude())
                                        database.reference.child("driversLocation").child(driverData.driverUid!!).child("genratedWaypoints").child(
                                            index.toString()
                                        ).child("lat").setValue(distanceLocation.locationPoint.latitude())
                                    }
                                }
                                else{
                                    database.reference.child("driversLocation").child(driverData.driverUid!!).child("users").child(auth.currentUser!!.uid).setValue("0.0")
                                    database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("genratedWaypoints").setValue(null)
                                    driverData.waypoint?.forEachIndexed { index, distanceLocation ->
                                        database.reference.child("driversLocation").child(driverData.driverUid!!).child("genratedWaypoints").child(
                                            index.toString()
                                        ).child("lng").setValue(distanceLocation.locationPoint.longitude())
                                        database.reference.child("driversLocation").child(driverData.driverUid!!).child("genratedWaypoints").child(
                                            index.toString()
                                        ).child("lat").setValue(distanceLocation.locationPoint.latitude())
                                    }
                                }
                                val rideDetails = hashMapOf(
                                    "request" to "pending",
                                    "driverUid" to driverUid,
                                    "pickupLat" to pickupLocation?.latitude(),
                                    "pickupLng" to pickupLocation?.longitude(),
                                    "destinationLat" to destinationLocation?.latitude(),
                                    "destinationLng" to destinationLocation?.longitude(),
                                    "distanceFromDriver" to driverData.distance,
                                    "durationFromDriver" to driverData.duration,
                                    "distance" to (df.format(distance/1000)).toString(),
                                    "duration" to ((duration/3600).toInt()).toString()+"hr "+(((duration % 3600) / 60).roundToInt()).toString()+"min",
                                )
                                if(usersKeys.size > 0){
                                    usersKeys.forEach {
                                        rideDetails.put(it,"pending")
                                    }
                                }
                                auth.currentUser?.uid?.let {
                                    db.collection("ridesDetail").document(it)
                                        .set(rideDetails)
                                        .addOnSuccessListener { documentReference ->
                                            navigationViewModel.onEvent(
                                                NavigationEvent.userUidChange(
                                                    driverData.driverUid!!
                                                ))
                                            navigationViewModel.onEvent(
                                                NavigationEvent.changeCurrentScreen(
                                                    NavigationScreen.WaitForRequest))
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("User in DB Failure", "Error adding document", e)
                                        }
                                }
                            }
                    }
                    currentRideDetails = RideDetails(
                        UserInfo = userInfo(userUid = driverData.driverUid),
                        pickup = pickupLocation,
                        destination = destinationLocation,
                        distance = driverData.distance,
                        duration = driverData.duration,
                    )

                }
                ) {
                    Icon(imageVector = Icons.Filled.Done, contentDescription = null)
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(vertical = 10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .zIndex(1f)
                .width(100.dp)
                .height(100.dp)
                .clip(shape = CircleShape),
            contentScale = ContentScale.Crop,
            painter = rememberImagePainter(data = state.profilePic),
            contentDescription = null,
        )
        Text(text = "${driverData.firstName} ${driverData.lastName}", fontWeight = FontWeight.SemiBold)
        Row{
            Icon(
                imageVector = if (driverData.rating >= 1) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = if (driverData.rating >= 2) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = if (driverData.rating >= 3) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = if (driverData.rating >= 4) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = if (driverData.rating >= 5) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = if(driverData.gender == "Male") Icons.Filled.Male else Icons.Filled.Female , contentDescription = null)
            Text(text = "${driverData.gender}")
        }

        Text(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp),text = " Vehicle Pics : ", fontWeight = FontWeight.SemiBold)
        Divider(modifier = Modifier.padding(10.dp))
        Column(modifier = Modifier
            .fillMaxWidth()) {
            Row( modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Image(modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                    contentScale = ContentScale.Crop,
                    painter = rememberImagePainter(data = state.vehiclePic0),
                    contentDescription = null)
                Image(modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                    contentScale = ContentScale.Crop,
                    painter = rememberImagePainter(data = state.vehiclePic1),
                    contentDescription = null)
                Image(modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                    contentScale = ContentScale.Crop,
                    painter = rememberImagePainter(data = state.vehiclePic2),
                    contentDescription = null)
            }
            Row( modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Image(modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                    contentScale = ContentScale.Crop,
                    painter = rememberImagePainter(data = state.vehiclePic3),
                    contentDescription = null)
                Image(modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                    contentScale = ContentScale.Crop,
                    painter = rememberImagePainter(data = state.vehiclePic4),
                    contentDescription = null)
                Image(modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                    contentScale = ContentScale.Crop,
                    painter = rememberImagePainter(data = state.vehiclePic5),
                    contentDescription = null)
            }
        }
        Text(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 10.dp),text = " Reviews : ", fontWeight = FontWeight.SemiBold)
        Divider(modifier = Modifier.padding(10.dp))
        Column(modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(enabled = true, state = rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            if(state.fetchingReviews){
                CircularProgressIndicator()
            }
            else {
                state.reviewList.forEach {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(20.dp)
                            ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                        .clip(shape = CircleShape),
                                    contentScale = ContentScale.Crop,
                                    painter = if (it.profilePic == null) painterResource(id = R.drawable.man) else rememberImagePainter(
                                        data = it.profilePic
                                    ),
                                    contentDescription = null
                                )
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    text = "${it.firstName} ${it.lastName}",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row {
                                Icon(
                                    imageVector = if (it.rating >= 1) Icons.Filled.Star else Icons.Filled.StarOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Icon(
                                    imageVector = if (it.rating >= 2) Icons.Filled.Star else Icons.Filled.StarOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Icon(
                                    imageVector = if (it.rating >= 3) Icons.Filled.Star else Icons.Filled.StarOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Icon(
                                    imageVector = if (it.rating >= 4) Icons.Filled.Star else Icons.Filled.StarOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Icon(
                                    imageVector = if (it.rating >= 5) Icons.Filled.Star else Icons.Filled.StarOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
                            text = it.review.toString()
                        )
                    }
                }
            }
            }
    }

}

@Composable
fun UserProfileUi(){
    val rating = 4
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(vertical = 10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .zIndex(1f)
                .width(100.dp)
                .height(100.dp)
                .clip(shape = CircleShape),
            contentScale = ContentScale.Crop,
            painter = painterResource(id = R.drawable.man),
            contentDescription = null,
        )
        Text(text = " Usama Muneeb ", fontWeight = FontWeight.SemiBold)
        Row{
            Icon(
                imageVector = if (rating >= 1) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = if (rating >= 2) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = if (rating >= 3) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = if (rating >= 4) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = if (rating >= 5) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Male , contentDescription = null)
            Text(text = "Male")
        }
        Text(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 10.dp),text = " Reviews : ", fontWeight = FontWeight.SemiBold)
        Divider(modifier = Modifier.padding(10.dp))
        Column(modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(enabled = true, state = rememberScrollState())){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(modifier = Modifier
                        .width(50.dp)
                        .height(50.dp),painter = painterResource(id = R.drawable.man), contentDescription = null)
                    Text(modifier = Modifier.padding(start=10.dp),text = "Hamza Muneeb", fontWeight = FontWeight.SemiBold)
                }
                Row{
                    Icon(
                        imageVector = if (rating >= 1) Icons.Filled.Star else Icons.Filled.StarOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = if (rating >= 2) Icons.Filled.Star else Icons.Filled.StarOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = if (rating >= 3) Icons.Filled.Star else Icons.Filled.StarOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = if (rating >= 4) Icons.Filled.Star else Icons.Filled.StarOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = if (rating >= 5) Icons.Filled.Star else Icons.Filled.StarOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),text = "Review ..... sdjk fa aaaa aaaaaaaa aaaaaaaa aa aaa aaaaaaah")
        }
    }
}