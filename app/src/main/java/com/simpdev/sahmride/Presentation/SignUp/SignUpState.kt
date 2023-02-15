package com.simpdev.sahmride.Presentation.SignUp

data class SignUpState(
    val firstName:String = "",
    val firstNameError: String? = null,
    val lastName:String = "",
    val lastNameError: String? = null,
    val email:String = "",
    val password:String = "",
    val emailError:String? = null,
    val passwordError:String? = null,
    val errorMsg:String? = null,
    val viewPassword:Boolean = false,
    val expandGenderList:Boolean = false,
    val genderList: List<String> = listOf<String>("Male","Female","Transgender"),
    val selectedGender:Int = 0,
    val accountCreated:Boolean = false,
    val signingUp:Boolean = false
)
