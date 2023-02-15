package com.simpdev.sahmride.Presentation.UserProfile

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap

data class UserProfileState(
    val gender:String? = "",
    val firstName:String = "",
    val firstNameError: String? = null,
    val lastName:String = "",
    val lastNameError: String? = null,
    val email:String = "",
    val passwordError:String? = null,
    val password:String = "",
    val newPassword:String = "",
    val newPasswordError: String? = null,
    val errorMsg:String? = null,
    val vehicleName:String =  "",
    val vehicleNameError: String? = null,
    val vehicleModel:String =  "",
    val vehicleModelError: String? = null,
    val cnic:String =  "",
    val cnicError: String? = null,
    val viewPassword:Boolean = false,
    val viewNewPassword:Boolean = false,
    val changeBecomeDriver: Boolean = false,
    val changeProfileSettings:Boolean = false,
    val changePassword:Boolean = false,
    val passwordNotMatched:Boolean = false,
    val scrollCurrent:String = "Down",
    val profileImage: List<Uri> = listOf<Uri>(),
    val cnicPics: List<Uri> = listOf<Uri>(),
    val drivingLicensePics: List<Uri> = listOf<Uri>(),
    val carPics: List<Uri> = listOf<Uri>(),
    val uploadProgress:Int = 0,
    val inAppNotificationMsg:String? = null,
    val updatingProfile:Boolean = false,
    val loadedProfilePic:Boolean = false,
    val loadedUserData:Boolean = false,
    val uploading:Boolean = false,
    val uploadingErrorMsg:String? = null,
    val isDriver:Boolean = false,
    val profileImageBitmap:ImageBitmap? = null
)