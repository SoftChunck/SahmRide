package com.simpdev.sahmride.Presentation.Profile

import Domain.Data.db
import Domain.Data.storageRef
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.simpdev.sahmride.Domain.Data.ReviewData

class ProfileViewModel : ViewModel() {
    var state by mutableStateOf(ProfileState())

    fun resetState(){
        state = state.copy(
            suggestion = null,
            profilePic =null,
            vehiclePic0 = null,
            vehiclePic1 = null,
            vehiclePic2 = null,
            vehiclePic3 = null,
            vehiclePic4 = null,
            vehiclePic5 = null,
            fetchingReviews = false,
            reviewList = emptyList<ReviewData>().toMutableList(),
        )
    }
    fun loadProfilePic(uid:String){
        storageRef.child("images/${uid}/profile").downloadUrl.addOnSuccessListener {
            state = state.copy(profilePic = it)
        }
    }
    fun loadVehiclePics(uid:String){
        storageRef.child("images/${uid}/car/0").downloadUrl.addOnSuccessListener {
            state = state.copy(vehiclePic0 = it)
        }
        storageRef.child("images/${uid}/car/1").downloadUrl.addOnSuccessListener {
            state = state.copy(vehiclePic1 = it)
        }
        storageRef.child("images/${uid}/car/2").downloadUrl.addOnSuccessListener {
            state = state.copy(vehiclePic2 = it)
        }
        storageRef.child("images/${uid}/car/3").downloadUrl.addOnSuccessListener {
            state = state.copy(vehiclePic3 = it)
        }
        storageRef.child("images/${uid}/car/4").downloadUrl.addOnSuccessListener {
            state = state.copy(vehiclePic4 = it)
        }
        storageRef.child("images/${uid}/car/5").downloadUrl.addOnSuccessListener {
            state = state.copy(vehiclePic5 = it)
        }
    }
    fun loadReviews(uid:String){
        state = state.copy( fetchingReviews = true )
        val ref = db.collection("users").document(uid).collection("reviews")
        ref.get().addOnSuccessListener {
            it.documents.forEach {
                var reviewData = ReviewData(
                    byUid = it.id ,
                    review = it.data?.get("review").toString(),
                    rating = (it.data?.get("rating").toString()).toInt(),
                )
                db.collection("users").document(it.id).get().addOnSuccessListener {
                    reviewData.firstName = it.get("firstName") as String
                    reviewData.lastName = it.get("lastName") as String
                    storageRef.child("images/${it.id}/profile").downloadUrl.addOnSuccessListener {
                        reviewData.profilePic = it
                        state.reviewList.add(
                            reviewData
                        )
                    }
                }
            }
            state = state.copy( fetchingReviews = false)
        }
            .addOnFailureListener {
                state = state.copy( fetchingReviews = false)
            }
    }
    fun onEvent(){

    }
}