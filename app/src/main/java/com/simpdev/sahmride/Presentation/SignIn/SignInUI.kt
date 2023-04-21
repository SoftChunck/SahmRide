package com.simpdev.sahmride.Presentation.SignIn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simpdev.sahmride.CurrentScreen
import com.simpdev.sahmride.MainScreenEvents
import com.simpdev.sahmride.MainScreenViewModel
import com.simpdev.sahmride.R

@OptIn(ExperimentalUnitApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignInUI(mainScreenViewModel: MainScreenViewModel) {
    val viewModel = viewModel<SigninViewModel>()
    val state = viewModel.state
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .zIndex(1F)
            .fillMaxSize()){
      Image(painter = painterResource(id = R.drawable.bg0), contentDescription = null, contentScale = ContentScale.FillBounds)
    }
    Column(
        modifier = Modifier
            .zIndex(2F)
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color.Transparent)
            .padding(15.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){

        Image(
            painterResource(id = R.drawable.logobgg),
            contentDescription = "",
        )
        Text(text = "Welcome!",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 19.dp)
                .padding(vertical = 25.dp),
            fontSize = TextUnit(10f, TextUnitType.Em),
            fontWeight = FontWeight(700),
            color = Color.White
        )
        if (state.loginError != null) {
            Text(
                text = state.loginError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if(state.loginError == "Email not Verified, Verification Email Sent")
        {
            AlertDialog(
                onDismissRequest = {

                },
                icon = { Icon(Icons.Filled.Error, contentDescription = null) },
                title = {
                    Text(text = "Email Not Verified")
                },
                text = {
                    Text(text = "Verification email sent, check your email for verification..")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(SigninEvents.LoginErrorNull)
                        }
                    ) {
                        Text("Close")
                    }
                },
            )
        }
        Spacer(modifier = Modifier
            .height(15.dp))
        OutlinedTextField(
            shape = RoundedCornerShape(40),
            singleLine = true,
            colors = TextFieldDefaults
                .outlinedTextFieldColors(
                    containerColor = Color.Transparent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.background,
                    cursorColor = MaterialTheme.colorScheme.background,
                    textColor = MaterialTheme.colorScheme.background,
                    placeholderColor = MaterialTheme.colorScheme.background
                ),
            value = state.email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "User Icon", tint = MaterialTheme.colorScheme.background) },
            placeholder = { Text(text = "Email") },
            onValueChange = { viewModel.onEvent(SigninEvents.emailChange(it))},
            isError = state.emailError != null,
            modifier = Modifier
                .fillMaxWidth(.9f)
        )
        if (state.emailError != null) {
            Text(
                text = state.emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth(0.9f)
            )
        }
        Spacer(modifier = Modifier
            .height(8.dp))
        OutlinedTextField(
            shape = RoundedCornerShape(40),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.Transparent,
                unfocusedBorderColor = MaterialTheme.colorScheme.background,
                focusedBorderColor = MaterialTheme.colorScheme.background,
                cursorColor = MaterialTheme.colorScheme.background,
                textColor = MaterialTheme.colorScheme.background,
                placeholderColor = MaterialTheme.colorScheme.background
            ),
            value = state.password,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            }),
            visualTransformation = if(state.viewPassword) VisualTransformation.None   else PasswordVisualTransformation() ,
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "User Icon", tint = MaterialTheme.colorScheme.background) },
            trailingIcon = { if(state.viewPassword) Icon(
                Icons.Filled.VisibilityOff,
                contentDescription = " View Password ",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .clickable(onClick = {   viewModel.onEvent(SigninEvents.viewPasswordChange)   }, enabled = true) )
            else Icon(
                Icons.Filled.Visibility,
                contentDescription = " View Password ",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .clickable(onClick = {   viewModel.onEvent(SigninEvents.viewPasswordChange)   }, enabled = true) ) },
            placeholder = { Text(text = "Password") },
            onValueChange = {
                viewModel.onEvent(SigninEvents.passwordChange(it))
            },
            isError = state.passwordError != null,
            modifier = Modifier
                .fillMaxWidth(.9f),
        )
        if (state.passwordError != null) {
            Text(
                text = state.passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth(0.9f)
            )
        }
        Spacer(modifier = Modifier
            .height(20.dp))
        ElevatedButton(onClick = { viewModel.onEvent(SigninEvents.SigninClicked) },
            shape = MaterialTheme.shapes.large,
            enabled = !state.signingIn,
            modifier = Modifier
                .fillMaxWidth(0.7f),
        ) {
            if(state.signingIn)
                CircularProgressIndicator()
            else
                Text(
                    text = "Login"
                )
        }
        Text(
            text = "Forgot Password ? ",
            color = MaterialTheme.colorScheme.background,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth()
                .clickable(enabled = true, onClick = {
                    mainScreenViewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.ForgotPassword))
                })
        )
        Spacer(modifier = Modifier
            .height(30.dp))
        Text(text = " or create new account",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,)
        ElevatedButton(
            onClick = {
                mainScreenViewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.SignUp))
            },
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth(0.4f),
        ) {
            Text(text = "Create")
        }
        if(state.signedInSuccessful)
        {
            mainScreenViewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.HomeScreen))
        }
        Spacer(modifier = Modifier
            .height(14.dp))
    }
}

