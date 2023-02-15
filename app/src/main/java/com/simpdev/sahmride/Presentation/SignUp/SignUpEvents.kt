package com.simpdev.sahmride.Presentation.SignUp

sealed class SignUpEvents{
    data class firstNameChange(val firstName:String): SignUpEvents()
    data class lastNameChange(val lastName:String): SignUpEvents()
    data class emailChange(val email:String): SignUpEvents()
    data class passwordChange(val password:String): SignUpEvents()
    object viewPasswordChange: SignUpEvents()
    object SigninClicked: SignUpEvents()
    object SignupClicked: SignUpEvents()
    data class expandGenderListChanged(val expandGenderListChanged: Boolean):SignUpEvents()
    data class selectedGenderChanged(val selectedGender:Int):SignUpEvents()
}
