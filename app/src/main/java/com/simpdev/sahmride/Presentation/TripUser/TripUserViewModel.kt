package com.simpdev.sahmride.Presentation.Trip

import Domain.Data.*
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
               it.children.forEachIndexed { index, dataSnapshot ->
                   if(index == 3){
                        state = state.copy(driverLng = dataSnapshot.value as Double)
                   }
                   else if(index == 2){
                       state = state.copy(driverLat = dataSnapshot.value as Double)
                   }
               }
            }
        }
        ref.addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("UserUid Upar",snapshot.value.toString())
                if(snapshot.value != null)
                {

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
        }
    }
    private fun waitForCompletion()
    {
        auth.currentUser?.let {
            db.collection("ridesDetail").document(it.uid).addSnapshotListener { value, error ->
                if(value != null){
                    if(value["request"] == "completed"){
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
}