package com.simpdev.sahmride.Presentation.Trip

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
import com.simpdev.sahmride.Domain.Data.KeyStatus
import com.simpdev.sahmride.Domain.RideDetails
import com.simpdev.sahmride.R
import java.io.File
import kotlin.math.roundToInt

class TripViewModel : ViewModel() {
    var state by mutableStateOf(TripState())

    fun onEvent(event:TripEvents){
        when(event)
        {
            is TripEvents.chatClicked -> {
                state = state.copy(currentScreen = TripScreen.ChatScreen)
            }
            is TripEvents.tripHomeClicked -> {
                state = state.copy(currentScreen = TripScreen.TripHome)
            }
            is TripEvents.userAccepted -> {
                database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("genratedWaypoints").get().addOnSuccessListener {
                    database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("waypoints").setValue(null)
                    it.children.forEach {
                        database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("waypoints").child(it.key.toString()).child("lng").setValue(it.child("lng").value)
                        database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("waypoints").child(it.key.toString()).child("lat").setValue(it.child("lat").value)
                    }
                }
                database.reference.child("driversLocation").child(auth.currentUser?.uid.toString()).child("users").child(
                    state.userUid!!
                ).setValue((event.distance!!.toDouble()  * priceOfFule * fulePerKm).roundToInt())
                state = state.copy( usersAvalible = false)
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
                        if(result != null ){
                            Log.d(key.key,result.get("pickupLng").toString())
                            userDetails.request =  result.get("request").toString()
                            userDetails.pickup = Point.fromLngLat(result.get("pickupLng") as Double,result.get("pickupLat") as Double)
                            userDetails.destination = Point.fromLngLat(result.get("destinationLng") as Double,result.get("destinationLat") as Double)
                            userDetails.distance = result.get("distance") as String?
                            userDetails.duration = result.get("duration") as String?
                            userDetails.distanceFromDriver = result.get("distanceFromDriver") as String?
                            userDetails.durationFromDriver = result.get("durationFromDriver") as String?
                            userDetails.request =  result.get("request").toString()
                            state.rideSharingRideDetails.add(userDetails)
                            if(userDetails.request.toString() == "pending"){
                                state = state.copy(usersAvalible = true)
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
                    if (notification != null && state.distance != null) {
                        with(context?.let { NotificationManagerCompat.from(it) }) {
                            this?.notify(1, notification.build())
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                Log.w( "Error", "Error getting documents.", exception)
            }
        }
    }
    fun listenForUsers(acceptedUserUid: String){
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
                snapshot.children.forEach {
                    if(it.key != null)
                    {
                        if((it.key.toString() != auth.currentUser!!.uid) || (it.key.toString() != acceptedUserUid) )
                        {
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
}