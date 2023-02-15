package com.simpdev.sahmride.Presentation.Navigation

import Domain.Data.*
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.simpdev.sahmride.Domain.Data.UserData
import java.io.File
import java.io.FileNotFoundException

class NavigationViewModel:ViewModel() {
    var state by mutableStateOf(NavigationState())
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
}