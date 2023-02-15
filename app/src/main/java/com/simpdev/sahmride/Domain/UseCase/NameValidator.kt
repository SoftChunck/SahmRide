package com.simpdev.sahmride.Domain.UseCase

class NameValidator {
    fun execute(name:String): ErrorMessage {
        if(name.isBlank())
        {
            return ErrorMessage(
                successful = false,
                errorMessage = "Name can't be empty"
            )
        }
        else if(name.length < 3)
        {
            return ErrorMessage(
                successful = false,
                errorMessage = "Invalid Name"
            )
        }
        return ErrorMessage()
    }
}