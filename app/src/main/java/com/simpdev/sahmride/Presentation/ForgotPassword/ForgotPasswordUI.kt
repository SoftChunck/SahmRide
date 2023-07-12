package com.simpdev.sahmride.Presentation.ForgotPassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
fun ForgotPasswordUI(mainScreenViewModel: MainScreenViewModel){
    val viewModel = viewModel<ForgotPasswordViewModel>()
    val state = viewModel.state
    Box(
        modifier = Modifier
            .zIndex(1F)
            .fillMaxSize()){
        Image(painter = painterResource(id = R.drawable.bg0), contentDescription = null, contentScale = ContentScale.FillBounds)
        Column (
            modifier = Modifier
                .zIndex(2F)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 200.dp)
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Image(
                painterResource(id = R.drawable.logobgg),
                contentDescription = "",
            )
            Text(text = "Recover Password",
                fontSize = TextUnit(7f, TextUnitType.Em),
                fontWeight = FontWeight(700),
                color = Color.White
            )

            Spacer(modifier = Modifier
                .height(20.dp))
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
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "User Icon", tint = MaterialTheme.colorScheme.primary) },
                placeholder = { Text(text = "Email") },
                onValueChange = {
                                viewModel.onEvent(ForgotPasswordEvents.emailChange(it))
                },
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
            ElevatedButton(
                onClick = {
                    viewModel.onEvent(ForgotPasswordEvents.recoverClicked)
                },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth(0.7f),
            ) {
                Text(
                    text = "RESET"
                )
            }
            Text(text = " or sign in",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,)
            ElevatedButton(onClick = {
                mainScreenViewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.SignIn))
            },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth(0.4f),
            ) {
                Text(text = "SIGN IN")
            }
        }
    }
    Box(
        modifier = Modifier.zIndex(4f)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.Bottom) {
            if(state.emailSent)
            {
                AlertDialog(
                    onDismissRequest = {

                    },
                    icon = { Icon(Icons.Filled.MarkEmailRead, contentDescription = null) },
                    title = {
                        Text(text = "Email Sent")
                    },
                    text = {
                        Text(text = "Reset Email Sent, Check your email to reset password")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mainScreenViewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.SignIn))
                            }
                        ) {
                            Text("Sign In")
                        }
                    },
                )
            }
        }
    }
}