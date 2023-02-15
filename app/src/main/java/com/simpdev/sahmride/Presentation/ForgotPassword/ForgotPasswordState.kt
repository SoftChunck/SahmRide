package com.simpdev.sahmride.Presentation.ForgotPassword

data class ForgotPasswordState(
    val email:String = "",
    val emailError:String? = null,
    val emailSent:Boolean = false
)
