package com.simpdev.sahmride.Domain.UseCase

class PasswordValidator {
    fun execute(password:String): ErrorMessage {
        if(password.isBlank()){
            return ErrorMessage(
                successful = false,
                errorMessage = "Password Can't Be Empty"
            )
        }
        else if(password.length < 8)
        {
            return ErrorMessage(
                successful = false,
                errorMessage = "Password must be atleast 8 Character Long"
            )
        }
        else if(!(password.any { it.isLetter() } && password.any { it.isDigit() }))
        {
            return ErrorMessage(
                successful = false,
                errorMessage = "Password must contain least 1 Character & Letter"
            )
        }
        return ErrorMessage()
    }
}