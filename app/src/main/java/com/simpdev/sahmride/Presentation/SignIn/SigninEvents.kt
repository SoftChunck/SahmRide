package com.simpdev.sahmride.Presentation.SignIn

sealed class SigninEvents{
    data class emailChange(val email:String): SigninEvents()
    data class passwordChange(val password:String): SigninEvents()
    object viewPasswordChange: SigninEvents()
    object SigninClicked: SigninEvents()
    object SignupClicked: SigninEvents()
    object LoginErrorNull: SigninEvents()
    object ForgotPasswordClicked: SigninEvents()
}
