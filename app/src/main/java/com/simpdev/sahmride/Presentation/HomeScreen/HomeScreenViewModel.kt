package com.simpdev.sahmride.Presentation.HomeScreen

import Domain.Data.auth
import Domain.Data.checkActiveStatus
import Domain.Data.database
import Domain.Data.db
import Domain.Data.readUserDataToFile
import Domain.Data.startLocationBroadcastService
import Domain.Data.userData
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.simpdev.sahmride.Domain.Data.RideHistory
import java.text.SimpleDateFormat

class HomeScreenViewModel: ViewModel() {

    val simpleDateFormat = SimpleDateFormat("EEEE , dd/MM")

    var state by mutableStateOf(HomeScreenState())

    init {

        val refActive = database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("Active")
        refActive.get().addOnSuccessListener {
            state = state.copy(active = if(it.value.toString() == "true") true else false)
        }

        val refSeats = database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("availableSeats")
        refSeats.get().addOnSuccessListener {
            state = state.copy(availableSeats = it.value.toString())
        }
        FetchRideHistory()

    }

    fun checkDriverStatus(){
        state = if(userData.firstName == null) {
            readUserDataToFile()
            state.copy(isDriver = userData.isDriver, active = checkActiveStatus())
        } else{
            state.copy(isDriver = userData.isDriver, active = checkActiveStatus())
        }
        if(state.active)
        {
            startLocationBroadcastService()
        }
    }
    fun onEvent(event: HomeScreenEvents){
        when(event)
        {
            is HomeScreenEvents.activeChange -> {
                userData.active = !state.active
                database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("Active").setValue(!state.active)
                state = state.copy(active = !state.active)
//                updateActiveStatus(state.active)
                if(state.active)
                {
                    startLocationBroadcastService()
                }
            }
            is HomeScreenEvents.rideSharingChange -> {
                state = state.copy(rideSharing = !state.rideSharing)
            }
            is HomeScreenEvents.availableSeatsChange -> {
                database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("availableSeats").setValue(event.availableSeats)
                state = state.copy(availableSeats = event.availableSeats,expandMenu = !state.expandMenu)
            }
            is HomeScreenEvents.expandMenuChange -> {
                state = state.copy(expandMenu = !state.expandMenu)
            }
        }
    }

    private fun FetchRideHistory(){
        state = state.copy( fetchingRideDetails = true)
        val ref = db.collection("users").document(auth.currentUser!!.uid).collection("rideHistory").orderBy("timeStamp",Query.Direction.DESCENDING)
        ref.get().addOnSuccessListener {
            it.documents.forEach {
                state.historyList.add(RideHistory(
                    dayData =  simpleDateFormat.format(it.data?.get("timeStamp")).toString(),
                    distance = it.data?.get("distance").toString(),
                    duration = it.data?.get("duration").toString(),
                    price = "Rs."+it.data?.get("price").toString(),
                ))
            }
            state = state.copy( fetchingRideDetails = false)
            }
            .addOnFailureListener {
                state = state.copy( fetchingRideDetails = false)
            }
    }
}