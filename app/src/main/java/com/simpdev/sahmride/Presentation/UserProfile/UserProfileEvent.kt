package com.simpdev.sahmride.Presentation.UserProfile

import android.net.Uri

sealed class UserProfileEvent
{
    data class firstNameChange(val firstName:String): UserProfileEvent()
    data class lastNameChange(val lastName:String): UserProfileEvent()
    data class passwordChange(val password:String): UserProfileEvent()
    data class newPasswordChange(val newPassword:String): UserProfileEvent()
    data class vehicleNameChange(val vehicleNameChange: String):UserProfileEvent()
    data class vehicleModelChange(val vehicleModelChange: String):UserProfileEvent()
    data class cnicChange(val cnic:String):UserProfileEvent()
    object viewPasswordChange: UserProfileEvent()
    object viewNewPasswordChange: UserProfileEvent()
    object viewBecomeDriverChange: UserProfileEvent()
    object changeProfileSettingsClicked: UserProfileEvent()
    object changePasswordClicked: UserProfileEvent()
    data class imageChanged(val uri: Uri):UserProfileEvent()
    data class cnicImagesChanged(val cnicImages:List<Uri>):UserProfileEvent()
    data class drivingLicenseImagesChanged(val drivingLicenseImages:List<Uri>):UserProfileEvent()
    data class carImagesChanged(val carImages:List<Uri>):UserProfileEvent()
    data class scrollCurrentChange(val scrollCurrent:String):UserProfileEvent()
    data class profileImageChange(val profileImage:List<Uri>):UserProfileEvent()
    data class inAppNofiticaitonMsgChange(val inAppNofiticaitonMsg:String?):UserProfileEvent()
    data class updatingProfileChange(val updatingProfile:Boolean):UserProfileEvent()
    object uploadDriverData:UserProfileEvent()
}
