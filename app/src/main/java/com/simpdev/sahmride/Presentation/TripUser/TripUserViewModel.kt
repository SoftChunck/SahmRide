package com.simpdev.sahmride.Presentation.Trip

import Domain.Data.auth
import Domain.Data.context
import Domain.Data.database
import Domain.Data.db
import Domain.Data.fulePerKm
import Domain.Data.priceOfFule
import Domain.Data.userData
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mapbox.geojson.Point
import com.simpdev.sahmride.Domain.RideDetails
import com.simpdev.sahmride.R
import kotlin.math.roundToInt

class TripUserViewModel : ViewModel() {
    init {
        waitForCompletion()
    }
    var state by mutableStateOf(TripUserState())

    fun trackDriver(driverUid:String){
        val ref = database.reference.child("driversLocation").child(driverUid)
        ref.get().addOnCompleteListener {
            it.addOnSuccessListener {
                state = state.copy(driverLng = (it.child("lng").value.toString()).toDouble(), driverLat = (it.child("lat").value.toString()).toDouble())
            }
        }
        ref.addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child("lng").value != null){
                    state = state.copy(driverLng = (snapshot.child("lng").value.toString()).toDouble(), driverLat = (snapshot.child("lat").value.toString()).toDouble())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("UserUid Error",error.message)
            }
        })
    }
    fun onEvent(event:TripUserEvents){
        when(event)
        {
            is TripUserEvents.chatClicked -> {
                state = state.copy(currentScreen = TripUserScreen.ChatScreen)
            }
            is TripUserEvents.tripHomeClicked -> {
                state = state.copy(currentScreen = TripUserScreen.TripHome)
            }
            is TripUserEvents.userAccepted -> {
                if(event.distance == "accepted")
                {
                    database.reference.child("driversLocation").child(state.driverUid!!).child("genratedWaypoints").get().addOnSuccessListener {
                        database.reference.child("driversLocation").child(state.driverUid!!).child("waypoints").setValue(null)
                        it.children.forEach {
                            database.reference.child("driversLocation").child(state.driverUid!!).child("waypoints").child(it.key.toString()).child("lng").setValue(it.child("lng").value)
                            database.reference.child("driversLocation").child(state.driverUid!!).child("waypoints").child(it.key.toString()).child("lat").setValue(it.child("lat").value)
                        }
                    }
                    database.reference.child("driversLocation").child(state.driverUid!!).child("genratedPrices").get().addOnSuccessListener {
                        database.reference.child("driversLocation").child(state.driverUid!!).child("users").setValue(null)
                        it.children.forEach {
                            database.reference.child("driversLocation").child(state.driverUid!!).child("users").child(it.key.toString()).setValue(it.value)
                        }
                    }
                    state = state.copy( usersAvalible = false, refresh = state.refresh+1)
                }
            }
        }
    }
    private fun waitForCompletion()
    {
        auth.currentUser?.let {
            db.collection("ridesDetail").document(it.uid).addSnapshotListener { value, error ->
                if(value != null){
                    if(value["request"] == "completed"){
                        database.reference.child("driversLocation").child(value["driverUid"].toString()).child("users").child(it.uid).removeValue()
                        state = state.copy(currentScreen = TripUserScreen.ReviewScreen)
                    }
                }
            }
        }
    }
    fun saveRideDetailsToHistory(){
        val ref = db.collection("ridesDetail").document(auth.currentUser!!.uid)
        ref.get().addOnSuccessListener { result ->
            if(result != null ) {
                val rideDetails = hashMapOf(
                    "price" to ((result.get("distance")as String).toDouble()  * priceOfFule * fulePerKm).roundToInt().toString(),
                    "driverUid" to result.get("driverUid") as String,
                    "pickupLng" to result.get("pickupLng") as Double,
                    "pickupLat" to result.get("pickupLat") as Double,
                    "destinationLng" to result.get("destinationLng") as Double,
                    "destinationLat" to result.get("destinationLat") as Double,
                    "distance" to result.get("distance") as String?,
                    "duration" to result.get("duration") as String?,
                    "distanceFromDriver" to result.get("distanceFromDriver") as String?,
                    "durationFromDriver" to result.get("durationFromDriver") as String?,
                    "timeStamp" to System.currentTimeMillis()
                )
                auth.currentUser?.uid?.let {
                    db.collection("users").document(it).collection("rideHistory")
                        .add(rideDetails)
                        .addOnSuccessListener {
                            ref.delete()
                            Log.d("History ", "Updated")
                        }
                        .addOnFailureListener { e ->
                            Log.w("User in DB Failure", "Error adding document", e)
                        }
                }
            }
        }.addOnFailureListener { exception ->
            Log.w( "Error", "Error getting documents.", exception)
        }

    }
    fun addListinerForUsers(){
        database.reference.child("driversLocation").child(state.driverUid!!).child("users").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if(it.key != auth.currentUser!!.uid){
                        fetchUserDetails(it.key!!)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun fetchUserDetails(key: String) {
        var userDetails = RideDetails()
        userDetails.UserInfo.userUid = key
        val refUserDetails = db.collection("users").document(key)
        refUserDetails.get().addOnSuccessListener { result ->
            if(result != null ){
                userDetails.UserInfo.firstName = result.get("firstName") as String?
                userDetails.UserInfo.lastName = result.get("lastName") as String?
                userDetails.UserInfo.gender = result.get("gender") as String?
                val ref = db.collection("ridesDetail").document(key)
                ref.get().addOnSuccessListener { result ->
                    if(result != null && result.get("pickupLng") != null){
                        userDetails.request =  result.get("request").toString()
                        userDetails.pickup = Point.fromLngLat(result.get("pickupLng") as Double,result.get("pickupLat") as Double)
                        userDetails.destination = Point.fromLngLat(result.get("destinationLng") as Double,result.get("destinationLat") as Double)
                        userDetails.distance = result.get("distance") as String?
                        userDetails.duration = result.get("duration") as String?
                        userDetails.distanceFromDriver = result.get("distanceFromDriver") as String?
                        userDetails.durationFromDriver = result.get("durationFromDriver") as String?
                        userDetails.request =  result.get("request").toString()
                        userDetails.price = (result.get("price").toString()).toInt()
                        state.rideSharingRideDetails.add(userDetails)
                        if(result.get(auth.currentUser!!.uid).toString() == "pending"){
                            database.reference.child("driversLocation").child(state.driverUid!!).child("extraDistance").get()
                                .addOnSuccessListener {
                                    userDetails.distance = String.format("%.3f",(it.value.toString().toDouble()))
                                    database.reference.child("driversLocation").child(state.driverUid!!).child("genratedPrices").child(
                                        auth.currentUser!!.uid).get().addOnSuccessListener {newPrice ->
                                        database.reference.child("driversLocation").child(state.driverUid!!).child("users").child(
                                            auth.currentUser!!.uid).get().addOnSuccessListener {
                                            userDetails.price =  (it.value.toString().toDouble() - newPrice.value.toString().toDouble()).roundToInt()
                                            state = state.copy(usersAvalible = true)
                                        }
                                    }
                                }

                        }
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
                        if (ActivityCompat.checkSelfPermission(
                                context!!,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return@addOnSuccessListener
                        }
                        this?.notify(1, notification.build())
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.w( "Error", "Error getting documents.", exception)
        }
    }
}