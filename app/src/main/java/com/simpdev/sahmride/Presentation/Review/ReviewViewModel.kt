package com.simpdev.sahmride.Presentation.Review

import Domain.Data.auth
import Domain.Data.db
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ReviewViewModel : ViewModel() {
    var state by mutableStateOf(ReviewState())

    fun onEvent(event: ReviewEvents){
        when(event){
            is ReviewEvents.ratingChanged -> {
                state = state.copy(rating = event.rating)
            }
            is ReviewEvents.reviewChange -> {
                state = state.copy(review = event.review)
            }
            is ReviewEvents.setUserUid -> {
                state = state.copy(userUid = event.userUid)
            }
            is ReviewEvents.submitReview -> {

                val reviewDetails = hashMapOf(
                    "rating" to state.rating,
                    "review" to state.review,
                )
                var ratingSum = 0.0
                auth.currentUser?.uid?.let {
                    state.userUid?.let { it1 ->
                        db.collection("users").document(it1).collection("reviews").document(auth.currentUser!!.uid)
                            .set(reviewDetails)
                            .addOnSuccessListener {
                                state = state.copy(reviewSubmitted = true)
                                db.collection("users").document(it1).collection("reviews").get().addOnSuccessListener {
                                    it.documents.forEach {
                                        ratingSum = (it.data?.get("rating").toString()).toDouble()
                                    }
                                    ratingSum /= it.documents.size
                                    db.collection("users").document(it1).update(
                                        "rating",ratingSum
                                    ).addOnSuccessListener {
                                        Log.d("Rating Updated", "Done")
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("User in DB Failure", "Error adding document", e)
                            }
                    }
                }
            }
        }
    }
}