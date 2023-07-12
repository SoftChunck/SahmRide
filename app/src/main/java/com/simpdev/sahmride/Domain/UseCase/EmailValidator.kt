package com.simpdev.sahmride.Domain.UseCase

import android.util.Patterns

class EmailValidator {
    fun execute(email:String): ErrorMessage {
        if(email.isBlank()){
            return ErrorMessage(
                successful = false,
                errorMessage = "Email Can't Be Empty"
            )
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            return ErrorMessage(
                successful = false,
                errorMessage = "Invalid Email"
            )
        }
        return ErrorMessage()
    }
}