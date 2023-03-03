package com.simpdev.sahmride.Presentation.SignUp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    Box {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.Top,
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
                painterResource(id = R.drawable.sahmlogo),
                contentDescription = "",
            )
            Text(text = "Create Account",
                fontSize = TextUnit(7f, TextUnitType.Em),
                fontWeight = FontWeight(700),
                color = MaterialTheme.colorScheme.primary
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
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
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
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
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
                    disabledTextColor = Color.Black,
                    disabledLabelColor = MaterialTheme.colorScheme.primary,
                    disabledIndicatorColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.background

                ),
                label = { Text(text = "Gender")},
                placeholder = { Text(text = "Select Gender")},
                trailingIcon = {
                    if (state.expandGenderList) Icon(Icons.Filled.ExpandLess, contentDescription = "")
                    else Icon(Icons.Filled.ExpandMore, contentDescription = "")
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
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
                    modifier = Modifier
                        .clickable(onClick = {   viewModel.onEvent(SignUpEvents.viewPasswordChange)   }, enabled = true) )
                else Icon(
                    Icons.Filled.Visibility,
                    contentDescription = " View Password ",
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
            Button(onClick = {
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
            Text(text = " or already have an account",
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
            Spacer(modifier = Modifier
                .height(14.dp))
        }
    }
}

