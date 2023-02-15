package com.simpdev.sahmride.Domain.UseCase

data class ErrorMessage(
    val successful:Boolean = true,
    val errorMessage: String? = null
)
