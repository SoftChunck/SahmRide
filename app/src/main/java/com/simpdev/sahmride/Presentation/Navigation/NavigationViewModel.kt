package com.simpdev.sahmride.Presentation.Navigation

import Domain.Data.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mapbox.geojson.Point
import com.simpdev.sahmride.Domain.Data.UserData
import com.simpdev.sahmride.R
import java.io.File
import java.io.FileNotFoundException

class NavigationViewModel:ViewModel() {
    init{
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
                val ref = database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("users")
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
                state = state.copy( userAccepted = true,currentScreen = NavigationScreen.RideAcceptedScreen)
            }
            is NavigationEvent.rideCancelled -> {
                state = state.copy( userAccepted = false, rideStatus = "cancelled",currentScreen = NavigationScreen.AppMainScreen)
            }
            is NavigationEvent.changeCurrentScreen -> {
                state = state.copy(currentScreen = event.screen)
            }
            is NavigationEvent.userUidChange -> {
                state = state.copy(userUid = event.uid)
            }
        }
    }

    fun loadUserData(){
        //Reading Data from File
        readImageFromExternalStorage()
        if(userData.firstName == null){
            var uid  = auth.currentUser?.uid
            var ref = uid?.let { db.collection("users").document(it) }
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
                if (userData != null) {
                    userData.profilePic = result
                    state = state.copy(loadedProfilePic = true)
                }
            }
        }
    }

    fun updateRideDetails(it: DataSnapshot) {
        state = state.copy(userUid = it.value.toString())
        if(it.value != null)
        {
            state = state.copy(usersAvalible = true)
            try{
                val storageReference = storageRef.child("images/${it.value}/profile")
                val localFile = File.createTempFile("profile", "jpeg")
                storageReference.getFile(localFile).addOnSuccessListener {
                    Log.d("Write","Write From Navigation")
                    if(localFile.path != null)
                    {
                        val inputStream = context?.contentResolver?.openInputStream(
                            Uri.fromFile(
                                File(localFile.path)
                            ))
                        state = state.copy(userPic = BitmapFactory.decodeStream(inputStream)?.asImageBitmap(),loadingUserProfile = false)
                    }
                }.addOnFailureListener {
                    Log.d("jpeg","File Failed")
                }
                val refUserDetails = db.collection("users").document(it.value.toString())
                refUserDetails.get().addOnSuccessListener { result ->
                    if(result != null ){
                        state = state.copy(firstName = result.get("firstName") as String?)
                        state = state.copy(lastName = result.get("lastName") as String?)
                        state = state.copy(gender = result.get("gender") as String?)
                    }
                }.addOnFailureListener { exception ->
                    Log.w( "Error", "Error getting documents.", exception)
                }

                val ref = db.collection("ridesDetail").document(it.value.toString())
                ref.get().addOnSuccessListener { result ->
                    if(result != null ){
                        state = state.copy(pickup = Point.fromLngLat(result.get("pickupLng") as Double,result.get("pickupLat") as Double))
                        state = state.copy(destination = Point.fromLngLat(result.get("destinationLng") as Double,result.get("destinationLat") as Double))
                        state = state.copy(distance = result.get("distance") as String?)
                        state = state.copy(duration = result.get("duration") as String?)
                        state = state.copy(distanceFromDriver = result.get("distanceFromDriver") as String?)
                        state = state.copy(durationFromDriver = result.get("durationFromDriver") as String?)

                    }
                }.addOnFailureListener { exception ->
                    Log.w( "Error", "Error getting documents.", exception)
                }
                var notification = context?.let {
                    NotificationCompat.Builder(it, "CHANNEL ID")
                        .setSmallIcon(R.drawable.usernotification)
                        .setContentTitle(userData.firstName + " "+ userData.lastName)
                        .setContentText(state.distance +" "+ state.duration)
                        .setStyle(NotificationCompat.BigTextStyle())
                }
                if (notification != null && state.distance != null) {
                    with(context?.let { NotificationManagerCompat.from(it) }) {
                        this?.notify(1, notification.build())
                    }
                }
            }
            catch (e:Exception){
                Log.d(null,e.toString())
            }
        }
    }

    private fun listenForUsers(){
        val ref = database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("users")
        ref.get().addOnCompleteListener {
            it.addOnSuccessListener {
                if(it.value != null)
                {
                    updateRideDetails(it)
                }
            }
        }
        ref.addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("UserUid Upar",snapshot.value.toString())
                if(snapshot.value != null)
                {
                    updateRideDetails(snapshot)
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
            db.collection("ridesDetail").document(it.uid).addSnapshotListener { value, error ->
                if(value != null){
                    if(value["request"] == "accepted"){
                        Log.d("Accepted","Accepted")
                        extractRideDetails()
                    }
                    else if(value["request"] == "pending"){
                        Log.d("Request","Pending")
                        state = state.copy(currentScreen = NavigationScreen.WaitForRequest, rideStatus = "pending")
                    }
                }
            }
        }
    }
    private fun extractRideDetails(){
        val ref = db.collection("ridesDetail").document(auth.currentUser!!.uid)
        ref.get().addOnSuccessListener { result ->
            if(result != null ){
                state = state.copy(userUid = result.get("driverUid") as String)
                val ref = database.reference.child("driversLocation").child(state.userUid!!)
                ref.get().addOnCompleteListener {
                    var driverLng:Double = 0.0
                    var driverLat:Double = 0.0
                    it.addOnSuccessListener {
                        it.children.forEachIndexed { index, dataSnapshot ->
                            if(index == 1){
                                driverLng = dataSnapshot.value as Double
                                Log.d(driverLat.toString(),driverLng.toString())
                                driverCurrentLocation = Point.fromLngLat(driverLng,driverLat)
                            }
                            else if(index == 0){
                                driverLat = dataSnapshot.value as Double
                            }
                        }
                    }
                }
                state = state.copy(pickup = Point.fromLngLat(result.get("pickupLng") as Double,result.get("pickupLat") as Double))
                state = state.copy(destination = Point.fromLngLat(result.get("destinationLng") as Double,result.get("destinationLat") as Double))
                state = state.copy(distance = result.get("distance") as String?)
                state = state.copy(duration = result.get("duration") as String?)
                state = state.copy( distanceFromDriver = result.get("distanceFromDriver") as String?)
                state = state.copy(durationFromDriver = result.get("durationFromDriver") as String?)
                if(state.userUid != null){
                    val refUserDetails = db.collection("users").document(state.userUid!!)
                    refUserDetails.get().addOnSuccessListener { result ->
                        if(result != null ){
                            state = state.copy(firstName = result.get("firstName") as String?)
                            state = state.copy(lastName = result.get("lastName") as String?)
                            state = state.copy(gender = result.get("gender") as String?)
                            val storageReference = storageRef.child("images/${state.userUid}/profile")
                            val localFile = File.createTempFile("profile", "jpeg")
                            storageReference.getFile(localFile).addOnSuccessListener {
                                Log.d("Write","Write From Navigation")
                                if(localFile.path != null)
                                {
                                    val inputStream = context?.contentResolver?.openInputStream(
                                        Uri.fromFile(
                                            File(localFile.path)
                                        ))
                                    state = state.copy(rideStatus = "accepted", currentScreen = NavigationScreen.UserAcceptedScreen)
                                    state = state.copy(userPic = BitmapFactory.decodeStream(inputStream)?.asImageBitmap(),loadingUserProfile = false)
                                }
                            }.addOnFailureListener {
                                state = state.copy(rideStatus = "accepted",currentScreen = NavigationScreen.UserAcceptedScreen)
                                Log.d("jpeg","File Failed")
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