package com.simpdev.sahmride.Presentation.ForgotPassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
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
    Box{
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 100.dp)
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Image(
                painterResource(id = R.drawable.sahmlogo),
                contentDescription = "",
            )
            Text(text = "Recover Password",
                fontSize = TextUnit(7f, TextUnitType.Em),
                fontWeight = FontWeight(700),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier
                .height(20.dp))
            OutlinedTextField(
                shape = RoundedCornerShape(40),
                singleLine = true,
                colors = TextFieldDefaults
                    .outlinedTextFieldColors(
                        containerColor = Color.Transparent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
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
            Button(
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
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,)
            ElevatedButton(onClick = {
                mainScreenViewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.SignIn))
            },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth(0.7f),
            ) {
                Text(text = "SIGN IN")
            }
        }
    }
    Box{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.Bottom) {
            if(state.emailSent)
            {
                Snackbar(
                    action = {
                        IconButton(onClick = {
                            viewModel.onEvent(ForgotPasswordEvents.emailSentChange(false))
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "")
                        }
                    }
                ){
                    Text(text = "Reset Email Sent")
                }
            }
        }
    }
}