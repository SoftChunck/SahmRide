package com.simpdev.sahmride.Presentation.SignIn

import Domain.Data.*
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.simpdev.sahmride.Domain.UseCase.EmailValidator
import com.simpdev.sahmride.Domain.UseCase.ErrorMessage
import com.simpdev.sahmride.Domain.UseCase.PasswordValidator

class SigninViewModel(
    private val emailValidator: EmailValidator = EmailValidator(),
    private val passwordValidator: PasswordValidator = PasswordValidator()
): ViewModel() {
    var state by mutableStateOf(SigninState())
    fun onEvent(event: SigninEvents)
    {
        when(event)
        {
            is SigninEvents.emailChange -> {
                state = state.copy(email = event.email)
            }
            is SigninEvents.passwordChange -> {
                var prevPass = state.password
                state = state.copy(password = event.password)
                if(state.password.length >= 16)
                {
                    state = state.copy(password = prevPass)
                }
            }
            is SigninEvents.viewPasswordChange -> {
                state = state.copy(viewPassword = !state.viewPassword)
            }
            is SigninEvents.SigninClicked -> {
                checkForErrorsAndSubmit()
            }
            is SigninEvents.SignupClicked -> {

            }
            is SigninEvents.ForgotPasswordClicked -> {

            }
        }
    }

    private fun checkForErrorsAndSubmit() {
        state = state.copy(signingIn = true)
        val emailValidationResult = emailValidator.execute(state.email)
        val passwordValidationResult = passwordValidator.execute(state.password)

        val hasError = listOf<ErrorMessage>(
            emailValidationResult,
            passwordValidationResult
        ).any{ !it.successful }

        if(hasError)
        {
            state = state.copy(
                emailError = emailValidationResult.errorMessage,
                passwordError = passwordValidationResult.errorMessage,
                signingIn = false
            )
        }
        else{
            state = state.copy(
                emailError = null,
                passwordError = null,
                loginError = null,
            )
            auth.signInWithEmailAndPassword(state.email, state.password)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        if(auth.currentUser?.isEmailVerified == true)
                        {
                            state = state.copy(signingIn = false,signedInSuccessful = true)
                            //Saving Profile Pic to External Storage
                                val riversRef = storageRef.child("images/${auth.currentUser?.uid}/profile")
                                fetchImageAndSaveToInternalStorage(riversRef,"profile")
                            //Saving User Data to File
                            var uid  = auth.currentUser?.uid
                            var ref = uid?.let { db.collection("users").document(it) }
                            ref?.get()?.addOnSuccessListener { result ->
                                if(result != null ){
                                    saveUserDataToFile(
                                        firstName = result.get("firstName") as String ,
                                        lastName = result.get("lastName") as String,
                                        email = auth.currentUser?.email.toString(),
                                        gender = result.get("gender") as String,
                                        isDriver = result.get("driver") as Boolean
                                    )
                                }
                            }?.addOnFailureListener { exception ->
                                Log.w( "Error", "Error getting documents.", exception)
                            }
                        }
                        else{
                            state = state.copy(signingIn = false)
                            auth.currentUser?.sendEmailVerification()
                            state = state.copy(loginError = "Email not Verified, Verification Email Sent")
                        }
                    } else {
                        state = state.copy(signingIn = false)
                        Log.w("Failed", "signInWithEmail:failure", task.exception)
                        state = state.copy(loginError = task.exception?.message)
                    }
                }
        }
    }
}