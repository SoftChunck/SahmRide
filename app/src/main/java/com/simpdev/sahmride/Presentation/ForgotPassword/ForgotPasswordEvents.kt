package com.simpdev.sahmride.Presentation.ForgotPassword

sealed class ForgotPasswordEvents{
    data class emailChange(val email:String):ForgotPasswordEvents()
    data class emailSentChange(val emailSent:Boolean):ForgotPasswordEvents()
    object recoverClicked:ForgotPasswordEvents()
}
