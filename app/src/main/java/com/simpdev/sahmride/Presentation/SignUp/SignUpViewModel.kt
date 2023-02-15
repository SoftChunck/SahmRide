package com.simpdev.sahmride.Presentation.SignUp

import Domain.Data.auth
import Domain.Data.db
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.simpdev.sahmride.Domain.UseCase.EmailValidator
import com.simpdev.sahmride.Domain.UseCase.ErrorMessage
import com.simpdev.sahmride.Domain.UseCase.NameValidator
import com.simpdev.sahmride.Domain.UseCase.PasswordValidator

class SignUpViewModel(
    private val nameValidator: NameValidator = NameValidator(),
    private val emailValidator: EmailValidator = EmailValidator(),
    private val passwordValidator: PasswordValidator = PasswordValidator()
): ViewModel() {
    var state by mutableStateOf(SignUpState())
    fun onEvent(event: SignUpEvents)
    {
        when(event)
        {
            is SignUpEvents.firstNameChange -> {
                var prevFirstName = state.firstName
                state = state.copy(firstName = event.firstName)
                if(event.firstName.length >= 14)
                {
                    state = state.copy(firstName = prevFirstName)
                }
            }
            is SignUpEvents.lastNameChange -> {
                var prevLastName = state.lastName
                state = state.copy(lastName = event.lastName)
                if(event.lastName.length >= 14)
                {
                    state = state.copy(lastName = prevLastName)
                }
            }
            is SignUpEvents.emailChange -> {
                state = state.copy(email = event.email)
            }
            is SignUpEvents.passwordChange -> {
                var prevPass = state.password
                state = state.copy(password = event.password)
                if(state.password.length >= 16)
                {
                    state = state.copy(password = prevPass)
                }
            }
            is SignUpEvents.expandGenderListChanged ->{
                state = state.copy(expandGenderList = event.expandGenderListChanged)
            }
            is SignUpEvents.selectedGenderChanged ->{
                state = state.copy(selectedGender = event.selectedGender)
            }
            is SignUpEvents.viewPasswordChange -> {
                state = state.copy(viewPassword = !state.viewPassword)
            }
            is SignUpEvents.SigninClicked -> {

            }
            is SignUpEvents.SignupClicked -> {
                checkForErrorsAndSubmit()
            }
        }
    }

    private fun checkForErrorsAndSubmit() {
        state = state.copy(signingUp = true)
        val firstNameValidationResult = nameValidator.execute(state.firstName)
        val lastNameValidationResult = nameValidator.execute(state.lastName)
        val emailValidationResult = emailValidator.execute(state.email)
        val passwordValidationResult = passwordValidator.execute(state.password)

        val hasError = listOf<ErrorMessage>(
            firstNameValidationResult,
            lastNameValidationResult,
            emailValidationResult,
            passwordValidationResult
        ).any{ !it.successful }

        if(hasError)
        {
            state = state.copy(
                firstNameError = firstNameValidationResult.errorMessage,
                lastNameError = lastNameValidationResult.errorMessage,
                emailError = emailValidationResult.errorMessage,
                passwordError = passwordValidationResult.errorMessage,
                signingUp = false
            )
        }
        else{
            state = state.copy(
                firstNameError = null,
                lastNameError = null,
                emailError = null,
                passwordError = null,
                errorMsg = null,
                accountCreated = false
            )
            auth.createUserWithEmailAndPassword(state.email, state.password)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        val userdata = hashMapOf(
                            "firstName" to state.firstName,
                            "lastName" to state.lastName,
                            "gender" to state.genderList[state.selectedGender],
                            "driver" to false
                        )
                        auth.currentUser?.uid?.let {
                            db.collection("users").document(it)
                                .set(userdata)
                                .addOnSuccessListener { documentReference ->
                                    val user = auth.currentUser
                                    auth.currentUser?.sendEmailVerification()
                                    state = state.copy(accountCreated = true)
                                }
                                .addOnFailureListener { e ->
                                    Log.w("User in DB Failure", "Error adding document", e)
                                }
                        }
                        state = state.copy(signingUp = false)
                    } else {
                        state = state.copy(signingUp = false)
                        state = state.copy(errorMsg = task.exception?.localizedMessage ?: "")
//                        if(state.errorMsg?.contains("password") == true)
//                        {
//                            state = state.copy(passwordError = )
//                        }
//                        else if(errorMsg.contains("email")){
//                            emailError = true
//                        }
                        Log.w("Fail", "createUserWithEmail:failure", task.exception)
                    }
                }
        }
    }
}