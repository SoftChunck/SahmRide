@file:Suppress("PackageName")

package com.simpdev.sahmride.Presentation.Navigation

import Domain.Data.*
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mapbox.geojson.Point
import com.simpdev.sahmride.Domain.Data.KeyStatus
import com.simpdev.sahmride.Domain.Data.UserData
import com.simpdev.sahmride.Domain.RideDetails
import com.simpdev.sahmride.R
import io.getstream.chat.android.client.ChatClient
import java.io.File
import java.io.FileNotFoundException


class NavigationViewModel:ViewModel() {
    init{
        startLocationBroadcastService()
        waitForResponse()
        listenForUsers()
    }
    var state by mutableStateOf(NavigationState())

    fun onEvent(event: NavigationEvent)
    {
        when(event)
        {
            is NavigationEvent.userRejected -> {
                db.collection("ridesDetail").document(state.userUid!!)
                    .update("request","rejected").addOnSuccessListener {
                        Log.d("request","Accepted")
                    }
                val ref = database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("users").child(
                    state.userUid!!
                )
                ref.setValue(null)
                state = state.copy(
                    usersAvalible = false,
                    loadingUserProfile = true,
                    firstName = null,
                    lastName = null,
                    distance = null,
                    duration = null,
                    distanceFromDriver = null,
                    durationFromDriver = null,
                    userPic = null,
                    userUid = null,
                )
            }
            is NavigationEvent.userAccepted -> {
                database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("genratedWaypoints").get().addOnSuccessListener {
                    database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("waypoints").setValue(null)
                    it.children.forEach {
                        database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("waypoints").child(it.key.toString()).child("lng").setValue(it.child("lng").value)
                        database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("waypoints").child(it.key.toString()).child("lat").setValue(it.child("lat").value)
                    }
                }
                database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("users").get().addOnSuccessListener {
                    ChatClient.instance().channel("messaging:"+auth.currentUser!!.uid).addMembers(listOf(it.key.toString())).enqueue { result ->
                        if (result.isSuccess) {
                            Log.d("Channel","User Added")
                        } else {
                            Log.d("Channel",result.toString())
                        }
                    }
                }
                state = state.copy( userAccepted = true,currentScreen = NavigationScreen.RideAcceptedScreen)

            }
            is NavigationEvent.rideCancelled -> {
                database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("users").child(
                    state.userUid!!
                ).setValue("cancelled")
                state = state.copy( userAccepted = false, rideStatus = "cancelled",currentScreen = NavigationScreen.AppMainScreen)
            }
            is NavigationEvent.rideCompleted -> {
//                database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("users").child(
//                    state.userUid!!
//                ).setValue("completed")
                state = state.copy( userAccepted = false, rideStatus = "cancelled",currentScreen = NavigationScreen.AppMainScreen)
            }
            is NavigationEvent.changeCurrentScreen -> {
                state = state.copy(currentScreen = event.screen)
            }
            is NavigationEvent.userUidChange -> {
                state = state.copy(userUid = event.uid)
            }
            else -> {}
        }
    }

    fun loadUserData(){
        //Reading Data from File
        readImageFromExternalStorage()
        if(userData.firstName == null){
            val uid  = auth.currentUser?.uid
            val ref = uid?.let { db.collection("users").document(it) }
            ref?.get()?.addOnSuccessListener { result ->
                if(result != null ){
                    userData = UserData(
                        firstName = result.get("firstName") as String,
                        lastName = result.get("lastName") as String,
                        gender = result.get("gender") as String,
                        email = auth.currentUser?.email.toString(),
                        isDriver = result.get("driver") as Boolean,
                    )
                    state = state.copy(loadedUserData = true)
                }
            }?.addOnFailureListener { exception ->
                state = state.copy(loadedUserData = true)
                Log.w( "Error", "Error getting documents.", exception)
            }
        }
    }
    fun loadProfilePic()
    {
        try {
            if(pathToProfilePic != null && userData.profilePicBitmap == null) {
                Log.d("Read", "Read From Navigation")
                userData.profilePicBitmap = readImageFromExternalStorage()?.asImageBitmap()
            }
            else if(userData.profilePicBitmap == null)
            {
                val storageReference = storageRef.child("images/${auth.currentUser?.uid}/profile")
                val localFile = File.createTempFile("profile", "jpeg")
                storageReference.getFile(localFile).addOnSuccessListener {
                    Log.d("Write","Write From Navigation")
                    pathToProfilePic = localFile.path
                    userData.profilePicBitmap = readImageFromExternalStorage()?.asImageBitmap()
                }.addOnFailureListener {
                    Log.d("jpeg","File Failed")
                }
            }
        }catch (e: FileNotFoundException){
            Log.d("Not Found","Profile Image Not Found")
            val riversRef = storageRef.child("images/${auth.currentUser?.uid}/profile")
            val taskRef = riversRef.downloadUrl
            taskRef.addOnFailureListener {
                state = state.copy(loadedProfilePic = true)
            }.addOnSuccessListener { result ->
                userData.profilePic = result
                state = state.copy(loadedProfilePic = true)
            }
        }
    }
    fun updateRideDetails(keys: MutableList<KeyStatus>) {
        Log.d("Keys ",keys.toString())
        state.waypoints = emptyList<Point>().toMutableList()
        keys.forEach { key ->
            Log.d("fetching Data of ",key.key.toString())
            var userDetails = RideDetails()
            userDetails.UserInfo.userUid = key.key
            val storageReference = storageRef.child("images/${key.key}/profile")
            val localFile = File.createTempFile("profile", "jpeg")
            storageReference.getFile(localFile).addOnSuccessListener {
                val inputStream = context?.contentResolver?.openInputStream(
                    Uri.fromFile(
                        File(localFile.path)
                    ))
                userDetails.UserInfo.userPic = BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            }.addOnFailureListener {
                Log.d("jpeg","File Failed")
            }
            val refUserDetails = db.collection("users").document(key.key)
            refUserDetails.get().addOnSuccessListener { result ->
                if(result != null ){
                    userDetails.UserInfo.firstName = result.get("firstName") as String?
                    userDetails.UserInfo.lastName = result.get("lastName") as String?
                    userDetails.UserInfo.gender = result.get("gender") as String?
                    state = state.copy(usersAvalible = true, userUid = key.key)
                    val ref = db.collection("ridesDetail").document(key.key)
                    ref.get().addOnSuccessListener { result ->
                        if(result != null && result.get("pickupLng").toString() != null){
                            Log.d(key.key,result.get("pickupLng").toString())
                            userDetails.request =  result.get("request").toString()
                            userDetails.pickup = Point.fromLngLat(result.get("pickupLng") as Double,result.get("pickupLat") as Double)
                            userDetails.destination = Point.fromLngLat(result.get("destinationLng") as Double,result.get("destinationLat") as Double)
                            userDetails.distance = result.get("distance").toString()
                            userDetails.duration = result.get("duration").toString()
                            userDetails.distanceFromDriver = result.get("distanceFromDriver").toString()
                            userDetails.durationFromDriver = result.get("durationFromDriver").toString()
                            userDetails.request =  result.get("request").toString()
                            userDetails.price = (result.get("price").toString()).toInt()
                            state.rideSharingRideDetails.add(userDetails)
                            if(userDetails.request.toString() == "pending"){
                                state = state.copy(usersAvalible = true)
                            }
                            else if(userDetails.request.toString() == "accepted"){
                                state = state.copy(userAccepted = true, currentScreen = NavigationScreen.RideAcceptedScreen)
                            }
                            Log.d("request",userDetails.request.toString())
                            Log.d("fetching Data of ",state.rideSharingRideDetails.size.toString())
                        }
                    }.addOnFailureListener { exception ->
                        Log.w( "Error", "Error getting documents.", exception)
                    }
                    val notification = context?.let {
                        NotificationCompat.Builder(it, "CHANNEL ID")
                            .setSmallIcon(R.drawable.usernotification)
                            .setContentTitle(userData.firstName + " "+ userData.lastName)
                            .setContentText(state.distance +" "+ state.duration)
                            .setStyle(NotificationCompat.BigTextStyle())
                    }
                }
            }.addOnFailureListener { exception ->
                Log.w( "Error", "Error getting documents.", exception)
            }
        }
    }
    private fun listenForUsers(){
        val keys:MutableList<KeyStatus> = emptyList<KeyStatus>().toMutableList()
        val refMyLocation = database.reference.child("driversLocation").child(auth.currentUser!!.uid)
        refMyLocation.get().addOnCompleteListener {
            it.addOnSuccessListener {
                if(it.value != null){
                    if(it.child("lng").value != null && it.child("lat").value != null)
                    {
                        driverCurrentLocation = Point.fromLngLat(it.child("lng").value.toString().toDouble(),it.child("lat").value.toString().toDouble())
                    }
                }
            }
        }
        val ref = database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("users")
        ref.addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Userr",snapshot.toString())
                snapshot.children.forEach {
                    Log.d("Userr",it.key.toString())
                    if(it.key != null)
                    {
                        if(it.key.toString() != auth.currentUser!!.uid)
                        {
                            Log.d("Userrr",it.key.toString())
                            keys.add(KeyStatus(key = it.key.toString(), status = it.value.toString()))
                            if(it.key == snapshot.children.last().key)
                            {
                                updateRideDetails(keys)
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("UserUid Error",error.message)
            }
        })
    }
    private fun waitForResponse()
    {
        auth.currentUser?.let {
            val keys:MutableList<KeyStatus> = emptyList<KeyStatus>().toMutableList()
            keys.add(KeyStatus(key = "request"))
            db.collection("ridesDetail").document(it.uid).get().addOnSuccessListener {
                val driverUid = it["driverUid"].toString()
                state = state.copy(userUid = it["driverUid"].toString())
                val refMyLocation = database.reference.child("driversLocation").child(driverUid).child("users")
                refMyLocation.get().addOnCompleteListener {
                    it.addOnSuccessListener {
                        it.children.forEach {
                            if(it.key.toString() != auth.currentUser!!.uid.toString())
                            {
                                keys.add(KeyStatus(key = it.key.toString()))
                            }
                        }
                        db.collection("ridesDetail").document(auth.currentUser!!.uid).addSnapshotListener { value, error ->
                            if(value != null){
                                keys.forEach {
                                    it.status = value[it.key].toString()
                                }
                                val notAccepted = keys.any { it.status == "pending" }
                                if(keys.all { it.status == "pending" || it.status == "accepted"} || keys.any{ it.status == "pending" || it.status == "accepted"})
                                {
                                    if(notAccepted)
                                    {
                                        Log.d("Request Not Accepted","Pending")
                                        state = state.copy(currentScreen = NavigationScreen.WaitForRequest, rideStatus = "pending")
                                    }
                                    else
                                    {
                                        state = state.copy(currentScreen = NavigationScreen.LoadingScreen)
                                        Log.d("Request Accepted","Extracting Details")
                                        extractRideDetails(keys = keys)
                                    }
                                }
                                else{
                                    Log.d("No Request","0 ")
                                }

//                    if(value["request"] == "accepted"){
//                        Log.d("Accepted","Accepted")
//                        extractRideDetails()
//                    }
//                    else if(value["request"] == "pending"){
//                        Log.d("Request","Pending")
//                        state = state.copy(currentScreen = NavigationScreen.WaitForRequest, rideStatus = "pending")
//                    }
                            }
                        }
                    }
                }
            }
        }
    }
    @SuppressLint("LongLogTag")
    private fun extractRideDetails(keys:MutableList<KeyStatus>){
        Log.d("Keys",keys.toString())
        state.waypoints = emptyList<Point>().toMutableList()
        val ref = db.collection("ridesDetail").document(auth.currentUser!!.uid)
        ref.get().addOnSuccessListener { result ->
            if(result != null ){
                state = state.copy(userUid = result.get("driverUid").toString())
                Log.d("Driver Found",state.userUid.toString())
                val ref = database.reference.child("driversLocation").child(state.userUid!!)
                ref.get().addOnCompleteListener {
                    it.addOnSuccessListener {
                        if(it.value != null){
                            if(it.child("lng").value != null && it.child("lat").value != null)
                            {
                                driverCurrentLocation = Point.fromLngLat(it.child("lng").value.toString().toDouble(),it.child("lat").value.toString().toDouble())
                            }
                        }
                    }
                }
                database.reference.child("driversLocation").child(state.userUid!!).child("waypoints").orderByKey().get().addOnSuccessListener {
                    it.children.forEach {
                        state.waypoints.add(Point.fromLngLat((it.child("lng").value.toString()).toDouble(),(it.child("lat").value.toString()).toDouble()))
                    }
                }
                state = state.copy(pickup = Point.fromLngLat(result.get("pickupLng") as Double,result.get("pickupLat") as Double))
                state = state.copy(destination = Point.fromLngLat(result.get("destinationLng") as Double,result.get("destinationLat") as Double))
                state = state.copy(distance = result.get("distance") as String?)
                state = state.copy(duration = result.get("duration") as String?)
                state = state.copy( distanceFromDriver = result.get("distanceFromDriver") as String?)
                state = state.copy(durationFromDriver = result.get("durationFromDriver") as String?)
//                if(state.userUid != null){
//                    val refUserDetails = db.collection("users").document(state.userUid!!)
//                    refUserDetails.get().addOnSuccessListener { result ->
//                        if(result != null ){
//                            state = state.copy(firstName = result.get("firstName") as String?)
//                            state = state.copy(lastName = result.get("lastName") as String?)
//                            state = state.copy(gender = result.get("gender") as String?)
//                            val storageReference = storageRef.child("images/${state.userUid}/profile")
//                            val localFile = File.createTempFile("profile", "jpeg")
//                            storageReference.getFile(localFile).addOnSuccessListener {
//                                Log.d("Write","Write From Navigation")
//                                if(localFile.path != null)
//                                {
//                                    val inputStream = context?.contentResolver?.openInputStream(
//                                        Uri.fromFile(
//                                            File(localFile.path)
//                                        ))
//                                    state = state.copy(rideStatus = "accepted", currentScreen = NavigationScreen.UserAcceptedScreen)
//                                    state = state.copy(userPic = BitmapFactory.decodeStream(inputStream)?.asImageBitmap(),loadingUserProfile = false)
//                                }
//                            }.addOnFailureListener {
//                                state = state.copy(rideStatus = "accepted",currentScreen = NavigationScreen.UserAcceptedScreen)
//                                Log.d("jpeg","File Failed")
//                            }
//                        }
//                    }.addOnFailureListener { exception ->
//                        Log.w( "Error", "Error getting documents.", exception)
//                    }
//                }
                keys.forEach { currentkey ->
                    val rideSharingUser:userInfo = userInfo( userUid = if(currentkey.key == "request") state.userUid else currentkey.key)
                    val refUserDetails = db.collection("users").document(rideSharingUser.userUid!!)
                    Log.d("Users Details",rideSharingUser.userUid.toString())
                    refUserDetails.get().addOnSuccessListener { result ->
                        if(result != null ){
                            rideSharingUser.firstName = result.get("firstName") as String?
                            rideSharingUser.lastName = result.get("lastName") as String?
                            rideSharingUser.gender = result.get("gender") as String?
                            val storageReference = storageRef.child("images/${rideSharingUser.userUid!!}/profile")
                            val localFile = File.createTempFile("profilerideshare", "jpeg")
                            storageReference.getFile(localFile).addOnSuccessListener {
                                val inputStream = context?.contentResolver?.openInputStream(
                                    Uri.fromFile(
                                        File(localFile.path)
                                    ))
                                rideSharingUser.userPic = BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                                state.rideSharingUsers.add(rideSharingUser)
                                if(currentkey.key == keys.get(keys.lastIndex).key)
                                {
                                    Log.d("Users","Completed")
                                    state = state.copy(rideStatus = "accepted", currentScreen = NavigationScreen.UserAcceptedScreen)
                                }
                            }.addOnFailureListener {
                                if(currentkey.key == keys.get(keys.lastIndex).key)
                                {
                                    Log.d("Usxtraction","Completed")
                                    state = state.copy(rideStatus = "accepted", currentScreen = NavigationScreen.UserAcceptedScreen)
                                }
                                Log.d("User Profile Loading Error","File Failed")
                                state.rideSharingUsers.add(rideSharingUser)
                            }
                        }
                    }.addOnFailureListener { exception ->
                        Log.w( "Error", "Error getting documents.", exception)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.w( "Error", "Error getting documents.", exception)
        }
    }
}