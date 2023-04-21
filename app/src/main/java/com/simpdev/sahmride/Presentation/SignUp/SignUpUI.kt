package com.simpdev.sahmride.Presentation.SignUp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
fun SignUpUI(mainScreenViewModel: MainScreenViewModel) {
    val viewModel = viewModel<SignUpViewModel>()
    val state = viewModel.state
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .zIndex(1F)
            .fillMaxSize()){
        Image(painter = painterResource(id = R.drawable.bg0), contentDescription = null, contentScale = ContentScale.FillBounds)
    }
        Column (
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2F)
                .padding(horizontal = 15.dp)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            if(state.accountCreated)
            {
                AlertDialog(
                    onDismissRequest = {

                    },
                    icon = { Icon(Icons.Filled.MarkEmailRead, contentDescription = null) },
                    title = {
                        Text(text = "Account Created")
                    },
                    text = {
                        Text(text = "Verification email sent, check your email to verify")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mainScreenViewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.SignIn))
                            }
                        ) {
                            Text("SignIn")
                        }
                    },
                )
            }
            Image(
                painterResource(id = R.drawable.logobgg),
                contentDescription = "",
            )
            Text(text = "Create Account",
                modifier = Modifier.padding(vertical = 15.dp),
                fontSize = TextUnit(7f, TextUnitType.Em),
                fontWeight = FontWeight(700),
                color = MaterialTheme.colorScheme.background
            )
            Spacer(modifier = Modifier
                .height(14.dp))
            if(state.errorMsg != null && !state.accountCreated ){
                Text(
                    text = state.errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                )
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                OutlinedTextField(
                    value = state.firstName,
                    shape = RoundedCornerShape(40),
                    colors = TextFieldDefaults
                        .outlinedTextFieldColors(
                            containerColor = Color.Transparent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.background,
                            cursorColor = MaterialTheme.colorScheme.background,
                            textColor = MaterialTheme.colorScheme.background,
                            placeholderColor = MaterialTheme.colorScheme.background,
                            focusedLabelColor = MaterialTheme.colorScheme.background,
                            unfocusedLabelColor = MaterialTheme.colorScheme.background,
                        ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    label = { Text(text = "First Name") },
                    singleLine = true,
                    placeholder = { Text(text = "First Name") },
                    isError = state.firstNameError != null,
                    onValueChange = {
                        viewModel.onEvent(SignUpEvents.firstNameChange(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth(.45f)
                )
                OutlinedTextField(
                    value = state.lastName,
                    singleLine = true,
                    shape = RoundedCornerShape(40),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = TextFieldDefaults
                        .outlinedTextFieldColors(
                            containerColor = Color.Transparent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.background,
                            cursorColor = MaterialTheme.colorScheme.background,
                            textColor = MaterialTheme.colorScheme.background,
                            placeholderColor = MaterialTheme.colorScheme.background,
                            focusedLabelColor = MaterialTheme.colorScheme.background,
                            unfocusedLabelColor = MaterialTheme.colorScheme.background,
                        ),
                    label = { Text(text = "Last Name") },
                    placeholder = { Text(text = "Last Name") },
                    isError = state.lastNameError != null,
                    onValueChange = {
                       viewModel.onEvent(SignUpEvents.lastNameChange(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth(.9f)
                )
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true, onClick = {
                        viewModel.onEvent(SignUpEvents.expandGenderListChanged(!state.expandGenderList))
                    }),
                colors = TextFieldDefaults.textFieldColors(
                    disabledTextColor = Color.White,
                    disabledLabelColor = MaterialTheme.colorScheme.background,
                    disabledIndicatorColor = MaterialTheme.colorScheme.background,
                    containerColor = Color.Transparent,
                    textColor = MaterialTheme.colorScheme.background,
                    placeholderColor = MaterialTheme.colorScheme.background,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.background,


                ),
                label = { Text(text = "Gender")},
                placeholder = { Text(text = "Select Gender")},
                trailingIcon = {
                    if (state.expandGenderList) Icon(Icons.Filled.ExpandLess, contentDescription = "",tint = MaterialTheme.colorScheme.background)
                    else Icon(Icons.Filled.ExpandMore, contentDescription = "",tint = MaterialTheme.colorScheme.background)
                },
                enabled = false,
                value = state.genderList[state.selectedGender],
                onValueChange = {

                }
            )
            if(state.expandGenderList){
                state.genderList.forEachIndexed{
                        index,element ->
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        headlineText = { Text(text = element)},
                        modifier = Modifier
                            .clickable(enabled = true, onClick = {
                                viewModel.onEvent(SignUpEvents.selectedGenderChanged(index))
                                viewModel.onEvent(SignUpEvents.expandGenderListChanged(false))
                            })
                    )
                    Divider()
                }
            }
            OutlinedTextField(
                value = state.email,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(40),
                colors = TextFieldDefaults
                    .outlinedTextFieldColors(
                        containerColor = Color.Transparent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.background,
                        focusedBorderColor = MaterialTheme.colorScheme.background,
                        cursorColor = MaterialTheme.colorScheme.background,
                        textColor = MaterialTheme.colorScheme.background,
                        placeholderColor = MaterialTheme.colorScheme.background,
                        focusedLabelColor = MaterialTheme.colorScheme.background,
                        unfocusedLabelColor = MaterialTheme.colorScheme.background,
                    ),
                label = { Text(text = "Email") },
                isError = state.emailError != null,
                placeholder = { Text(text = "Email") },
                onValueChange = {
                    viewModel.onEvent(SignUpEvents.emailChange(it))
                },
                modifier = Modifier
                    .fillMaxWidth()
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
            OutlinedTextField(
                value = state.password,
                singleLine = true,
                shape = RoundedCornerShape(40),
                colors = TextFieldDefaults
                    .outlinedTextFieldColors(
                        containerColor = Color.Transparent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.background,
                        focusedBorderColor = MaterialTheme.colorScheme.background,
                        cursorColor = MaterialTheme.colorScheme.background,
                        textColor = MaterialTheme.colorScheme.background,
                        placeholderColor = MaterialTheme.colorScheme.background,
                        focusedLabelColor = MaterialTheme.colorScheme.background,
                        unfocusedLabelColor = MaterialTheme.colorScheme.background,
                    ),
                label = { Text(text = "Password") },
                isError = state.passwordError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password,imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                visualTransformation = if(state.viewPassword) VisualTransformation.None   else PasswordVisualTransformation() ,
                trailingIcon = { if(state.viewPassword) Icon(
                    Icons.Filled.VisibilityOff,
                    contentDescription = " View Password ",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .clickable(onClick = {   viewModel.onEvent(SignUpEvents.viewPasswordChange)   }, enabled = true) )
                else Icon(
                    Icons.Filled.Visibility,
                    contentDescription = " View Password ",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .clickable(onClick = {   viewModel.onEvent(SignUpEvents.viewPasswordChange)  }, enabled = true) ) },
                placeholder = { Text(text = "Password") },
                onValueChange = {
                    viewModel.onEvent(SignUpEvents.passwordChange(it))
                },
                modifier = Modifier
                    .fillMaxWidth(),
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
                .height(14.dp))
            ElevatedButton(onClick = {
                             viewModel.onEvent(SignUpEvents.SignupClicked)
            },
                enabled = !state.signingUp,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth(0.7f),
            ) {
                if(state.signingUp)
                    CircularProgressIndicator()
                else
                    Text(
                        text = "CREATE ACCOUNT"
                    )
            }
            Spacer(modifier = Modifier
                .height(14.dp))
            Text(text = " or already have an account",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,)
            ElevatedButton(onClick = {
                mainScreenViewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.SignIn))
            },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth(0.4f),
            ) {
                Text(text = "SIGN IN")
            }
        }
}

