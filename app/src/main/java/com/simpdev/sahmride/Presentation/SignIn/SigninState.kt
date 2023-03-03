package com.simpdev.sahmride.Presentation.SignIn

import android.content.Context

data class SigninState(
    val email:String = "",
    val password:String = "",
    val emailError:String? = null,
    val passwordError:String? = null,
    var loginError:String? = null,
    val viewPassword:Boolean = false,
    val signedInSuccessful:Boolean = false,
    val signingIn:Boolean = false,
    val context: Context? = null
)
