package com.simpdev.sahmride.Presentation.UserProfile

import Domain.Data.*
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.simpdev.sahmride.Domain.Data.UserData
import com.simpdev.sahmride.Domain.UseCase.ErrorMessage
import com.simpdev.sahmride.Domain.UseCase.NameValidator
import com.simpdev.sahmride.Domain.UseCase.PasswordValidator
import java.io.File
import java.io.FileNotFoundException

class UserProfileViewModel(
    private val passwordValidator: PasswordValidator = PasswordValidator(),
    private val nameValidator: NameValidator = NameValidator()
):ViewModel() {
    var state by mutableStateOf(UserProfileState())
    fun setUserDetails(){
        Log.d(null, userData.toString())
        state = state.copy(
            firstName = userData.firstName?:"",
            lastName = userData.lastName?:"",
            gender = userData.gender?:"",
            email = userData.email?:"",
            isDriver = userData.isDriver
        )
    }
    fun loadUserData(){

        readUserDataToFile()
        if(userData.firstName == null)
        {
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
                    setUserDetails()
                }
            }?.addOnFailureListener { exception ->
                state = state.copy(loadedUserData = true)
                Log.w( "Error", "Error getting documents.", exception)
            }
        }
        else {
            setUserDetails()
        }
    }
    fun loadProfilePic()
    {
        if(userData.profilePic == null)
        {
            try {
                if(pathToProfilePic != null && state.profileImageBitmap == null) {
                    Log.d("Read", "Read From Profile")
                    state =
                        state.copy(profileImageBitmap = readImageFromExternalStorage()?.asImageBitmap())
                }
                else
                {
                    val storageReference = storageRef.child("images/${auth.currentUser?.uid}/profile")
                    val localFile = File.createTempFile("profile", "jpeg")
                    storageReference.getFile(localFile).addOnSuccessListener {
                        Log.d("Write","Write From Profile")
                        pathToProfilePic = localFile.path
                        state = state.copy(profileImageBitmap = readImageFromExternalStorage()?.asImageBitmap())
                    }.addOnFailureListener {
                        Log.d("jpeg","File Failed")
                    }
                }
            }catch (e:FileNotFoundException){
                Log.d("Not Found","Profile Image Not Found")
                val riversRef = storageRef.child("images/${auth.currentUser?.uid}/profile/")
                val taskRef = riversRef.downloadUrl
                taskRef.addOnFailureListener {
                    state = state.copy(loadedProfilePic = true)
                }.addOnSuccessListener { result ->
                    if (userData != null) {
                        userData.profilePic = result
                        state = state.copy(loadedProfilePic = true, profileImage = listOf(result) as List<Uri>)
                    }
                }
            }
        }
        else{
            state = state.copy(profileImageBitmap = userData.profilePicBitmap)
        }
    }
    fun onEvent(event: UserProfileEvent)
    {
        when(event)
        {
             is UserProfileEvent.firstNameChange -> {
                 var prevFirstName = state.firstName
                 state = state.copy(firstName = event.firstName)
                 if(event.firstName.length >= 14)
                 {
                     state = state.copy(firstName = prevFirstName)
                 }
             }
            is UserProfileEvent.lastNameChange -> {
                var prevLastName = state.lastName
                state = state.copy(lastName = event.lastName)
                if(event.lastName.length >= 14)
                {
                    state = state.copy(lastName = prevLastName)
                }
            }
            is UserProfileEvent.passwordChange -> {
                var prevPass = state.password
                state = state.copy(password = event.password)
                if(state.password.length >= 16)
                {
                    state = state.copy(password = prevPass)
                }
            }
            is UserProfileEvent.newPasswordChange -> {
                var prevPass = state.newPassword
                state = state.copy(newPassword = event.newPassword)
                if(state.password.length >= 16)
                {
                    state = state.copy(newPassword = prevPass)
                }
            }
            is UserProfileEvent.vehicleNameChange -> {
                var prevVehicleName = state.vehicleName
                state = state.copy(vehicleName = event.vehicleNameChange)
                if(state.vehicleName?.length ?: 0  >= 24)
                {
                    state = state.copy(vehicleName = prevVehicleName)
                }
            }
            is UserProfileEvent.vehicleModelChange -> {
                var prevModel = state.vehicleModel
                state = state.copy(vehicleModel = event.vehicleModelChange)
                if(state.vehicleModel?.length ?: 0  >= 14 || !(state.vehicleModel.any { it.isDigit() }))
                {
                    state = state.copy(vehicleModel = prevModel)
                }
            }
            is UserProfileEvent.cnicChange -> {
                var prevCnic = state.cnic
                state = state.copy(cnic = event.cnic)
                if(state.cnic?.length ?:0  > 13)
                {
                    state = state.copy(cnic = prevCnic)
                }
            }
            is UserProfileEvent.changePasswordClicked ->{
                if(state.changePassword)
                {
                    saveNewPassword()
                }
                else{
                    state = state.copy(changePassword = !state.changePassword)
                }
            }
            is UserProfileEvent.changeProfileSettingsClicked ->{
                if(state.changeProfileSettings)
                {
                    saveProfileSettings()
                }
                else{
                    state = state.copy(changeProfileSettings = !state.changeProfileSettings)
                }
            }
            is UserProfileEvent.viewPasswordChange -> {
                state = state.copy(viewPassword = !state.viewPassword)
            }
            is UserProfileEvent.viewNewPasswordChange -> {
                state = state.copy(viewNewPassword = !state.viewNewPassword)
            }
            is UserProfileEvent.viewBecomeDriverChange -> {
                if(state.changeBecomeDriver)
                {
                    saveDriverData()
                }
                else{
                    state = state.copy(changeBecomeDriver = !state.changeBecomeDriver)
                }
            }
            is UserProfileEvent.imageChanged -> {

                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/profile/").putFile(event.uri).addOnCompleteListener {
                    it.addOnFailureListener {
                        state = state.copy(inAppNotificationMsg = "Network Error Failed to Update Profile Image", updatingProfile = false)
                    }
                    it.addOnSuccessListener {
                        state = state.copy(inAppNotificationMsg = "Profile Picture Updated Successfully", profileImage = listOf<Uri>(), updatingProfile = false)
                        loadProfilePic()
                    }
                }
            }
            is UserProfileEvent.scrollCurrentChange -> {
                state = state.copy(scrollCurrent = event.scrollCurrent)
            }
            is UserProfileEvent.profileImageChange -> {
                state = state.copy(profileImage = event.profileImage)
            }
            is UserProfileEvent.cnicImagesChanged -> {
                state = state.copy(cnicPics = event.cnicImages)
            }
            is UserProfileEvent.drivingLicenseImagesChanged -> {
                state = state.copy(drivingLicensePics = event.drivingLicenseImages)
            }
            is UserProfileEvent.carImagesChanged -> {
                state = state.copy(carPics = event.carImages)
            }
            is UserProfileEvent.inAppNofiticaitonMsgChange -> {
                state = state.copy(inAppNotificationMsg = event.inAppNofiticaitonMsg)
            }
            is UserProfileEvent.updatingProfileChange -> {
                state = state.copy(updatingProfile = event.updatingProfile)
            }
            is UserProfileEvent.uploadDriverData -> {
                state = state.copy(uploading = true)
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/cnic/0").putFile(state.cnicPics[0]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/cnic/1").putFile(state.cnicPics[1]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/drivingLicense/0").putFile(state.cnicPics[0]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/drivingLicense/1").putFile(state.cnicPics[1]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/car/0").putFile(state.carPics[0]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/car/1").putFile(state.carPics[1]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/car/2").putFile(state.carPics[2]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/car/3").putFile(state.carPics[3]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/car/4").putFile(state.carPics[4]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
                storageRef.child("images/"+(auth.currentUser?.uid ?: "")+"/car/5").putFile(state.carPics[5]).addOnCompleteListener { it.addOnSuccessListener { state = state.copy(uploadProgress = state.uploadProgress + 1) } }
            }
        }
    }

    private fun saveDriverData(){
        state = if(state.vehicleName.length < 4) {
            state.copy(vehicleNameError = "Invalid Vehicle Name")
        } else {
            state.copy(vehicleNameError = null)
        }
        state = if(state.vehicleModel.length < 4) {
            state.copy(vehicleModelError = "Invalid Vehicle Model")
        } else {
            state.copy(vehicleModelError = null)
        }
        state = if(state.cnic.length < 13) {
            state.copy(cnicError = "Invalid CNIC")
        } else {
            state.copy(cnicError = null)
        }

        state = if(state.uploadProgress != 10) {
            state.copy(uploadingErrorMsg = "Upload All the Documents")
        } else {
            state.copy(uploadingErrorMsg = null)
        }
        if(state.vehicleModelError== null && state.vehicleNameError == null && state.cnicError == null && state.uploadingErrorMsg == null)
        {
            auth.currentUser?.let { db.collection("users").document(it.uid).update(
                "vehicleName",state.vehicleName,
                "vehicleModel",state.vehicleModel,
                "cnic",state.cnic,
                "driver",true
            ) }?.addOnFailureListener {
                Log.d("",it.message.toString())
            }?.addOnSuccessListener {
                userData.isDriver = true
                state = state.copy(changeBecomeDriver = false, isDriver = true)
                Log.d("Updated","Success")
            }
        }

    }
    private fun saveProfileSettings() {
        auth.currentUser?.let { db.collection("users").document(it.uid).update(
            "firstName",state.firstName,
            "lastName",state.lastName
        ) }?.addOnFailureListener {
            Log.d("Updated",it.message.toString())
        }?.addOnSuccessListener {
            state = state.copy(changeProfileSettings = false)
            Log.d("Updated","Success")
            //Saving User Data to File
            var uid  = auth.currentUser?.uid
            var ref = uid?.let { db.collection("users").document(it) }
            ref?.get()?.addOnSuccessListener { result ->
                if(result != null ){
                    saveUserDataToFile(
                        firstName = result.get("firstName") as String ,
                        lastName = result.get("lastName") as String,
                        email = auth.currentUser?.email.toString(),
                        gender = result.get("gender") as String,
                        isDriver = result.get("driver") as Boolean
                    )
                    loadUserData()
                }
            }?.addOnFailureListener { exception ->
                Log.w( "Error", "Error getting documents.", exception)
            }
        }
    }
    private fun saveNewPassword(){
        state = state.copy(
            passwordError = null,
            newPasswordError = null
        )
        val oldPasswordValidationResult = passwordValidator.execute(state.password)
        val newPasswordValidationResult = passwordValidator.execute(state.newPassword)
        val hasError = listOf<ErrorMessage>(
            oldPasswordValidationResult,
            newPasswordValidationResult
        ).any{ !it.successful }

        if(hasError)
        {
            state = state.copy(
                passwordError = oldPasswordValidationResult.errorMessage,
                newPasswordError = newPasswordValidationResult.errorMessage,
            )
        }
        else{
            var credentials: AuthCredential = EmailAuthProvider.getCredential(state.email,state.password)
            auth.currentUser?.reauthenticate(credentials)?.addOnCompleteListener {
                if(it.isSuccessful)
                {
                    auth.currentUser?.updatePassword(state.newPassword)
                        ?.addOnSuccessListener {  Log.d("Updated","Success") }
                        ?.addOnFailureListener { Log.d("Updated",it.message.toString()) }
                    state = state.copy(changePassword = false, inAppNotificationMsg = "Password Updated Successfully", password = "", newPassword = "")
                }
                else{
                    Log.d("Password","NotSuccess")
                    state = state.copy(inAppNotificationMsg = "Wrong Password", passwordError = "Wrong Password")
                }
            }
        }
    }
}