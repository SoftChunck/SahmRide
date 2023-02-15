package com.simpdev.sahmride.Presentation.ForgotPassword

import Domain.Data.auth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.simpdev.sahmride.Domain.UseCase.EmailValidator

class ForgotPasswordViewModel(
    private val emailValidator: EmailValidator = EmailValidator()
): ViewModel() {
    var state by mutableStateOf(ForgotPasswordState())

    fun onEvent(event: ForgotPasswordEvents)
    {
        when(event)
        {
            is ForgotPasswordEvents.emailChange -> {
                state = state.copy(email = event.email)
            }
            is ForgotPasswordEvents.emailSentChange -> {
                state = state.copy(emailSent = event.emailSent)
            }
            is ForgotPasswordEvents.recoverClicked -> {
                recoverSubmit()
            }
        }
    }

    private fun recoverSubmit() {
        val emailValidationResult = emailValidator.execute(state.email)
        if(emailValidationResult.successful)
        {
            auth.sendPasswordResetEmail(state.email)
            state = state.copy(emailSent = true, emailError = null)
        }
        else{
            state = state.copy(emailError = emailValidationResult.errorMessage)
        }
    }
}