package com.simpdev.sahmride.Presentation.HomeScreen

import Domain.Data.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeScreenViewModel: ViewModel() {

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
            is HomeScreenEvents.availableSeatsChange -> {
                database.reference.child("driversLocation").child(auth.currentUser!!.uid).child("availableSeats").setValue(event.availableSeats)
                state = state.copy(availableSeats = event.availableSeats,expandMenu = !state.expandMenu)
            }
            is HomeScreenEvents.expandMenuChange -> {
                state = state.copy(expandMenu = !state.expandMenu)
            }
        }
    }
}