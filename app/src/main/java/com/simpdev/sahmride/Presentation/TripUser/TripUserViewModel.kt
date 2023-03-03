package com.simpdev.sahmride.Presentation.Trip

import Domain.Data.database
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TripUserViewModel : ViewModel() {
    var state by mutableStateOf(TripUserState())

    fun trackDriver(driverUid:String){
        val ref = database.reference.child("driversLocation").child(driverUid)
        ref.get().addOnCompleteListener {
            it.addOnSuccessListener {
               it.children.forEachIndexed { index, dataSnapshot ->
                   if(index == 1){
                        state = state.copy(driverLng = dataSnapshot.value as Double)
                   }
                   else if(index == 0){
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
}