package com.simpdev.sahmride.Presentation.UserProfile

import Domain.Data.userData
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.simpdev.sahmride.R


@OptIn(ExperimentalUnitApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserProfileUI(
    innerPadding:PaddingValues
)
{
    val viewModel = viewModel<UserProfileViewModel>()
    val state = viewModel.state
    val galleryLauncherProfile =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            viewModel.onEvent(UserProfileEvent.profileImageChange(uriList))
        }
    val galleryLauncherCnic =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            viewModel.onEvent(UserProfileEvent.cnicImagesChanged(uriList))
        }
    val galleryLauncherDrivingLicense =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            viewModel.onEvent(UserProfileEvent.drivingLicenseImagesChanged(uriList))
        }
    val galleryLauncherCar =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            viewModel.onEvent(UserProfileEvent.carImagesChanged(uriList))
        }
    LaunchedEffect(key1 = 1){
        if(!state.loadedProfilePic && !state.loadedUserData && userData.firstName == null)
        {
            viewModel.loadUserData()
            viewModel.loadProfilePic()
        }
        if(userData.firstName != null && state.firstName == "")
        {
            viewModel.setUserDetails()
            viewModel.loadProfilePic()
        }
        else if(state.firstName == "" && state.loadedProfilePic && state.loadedUserData){
            viewModel.setUserDetails()
        }
    }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f),
    ){
        if(state.profileImageBitmap == null)
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                painter = if(state.profileImage.size == 0)
                    if(userData.profilePic == null)
                        painterResource(id = R.drawable.man)
                    else
                        rememberImagePainter(userData.profilePic)
                else
                    rememberImagePainter(state.profileImage[0]),
                contentDescription = "")
        else
            Image(
                bitmap = state.profileImageBitmap,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                contentDescription = "")
        Surface(
            color = Color.Black, modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f)
        ) {}
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (x, y) = dragAmount
                    when {
                        x > 0 -> { /* right */
                        }
                        x < 0 -> { /* left */
                        }
                    }
                    when {
                        y > 0 -> { /* down */
                            viewModel.onEvent(UserProfileEvent.scrollCurrentChange("Down"))
                        }
                        y < 0 -> {
                            /* up */
                            viewModel.onEvent(UserProfileEvent.scrollCurrentChange("Up"))
                        }
                    }
                }
            }
        ,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (state.scrollCurrent == "Up") 1f else 0.74f)
                .padding(if (state.scrollCurrent == "Up") innerPadding else PaddingValues(0.dp))
                .background(
                    MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(
                        topStart = if (state.scrollCurrent == "Up") 0.dp else 25.dp,
                        topEnd = if (state.scrollCurrent == "Up") 0.dp else 25.dp
                    ),
                ),
            horizontalAlignment = if(state.scrollCurrent == "Up") Alignment.CenterHorizontally else Alignment.Start
        ) {
            if(state.profileImageBitmap == null)
            Image(
                modifier = Modifier
                    .zIndex(1f)
                    .width(100.dp)
                    .height(100.dp)
                    .offset(
                        if (state.scrollCurrent == "Down") 25.dp else 0.dp,
                        if (state.scrollCurrent == "Down") (-45).dp else 20.dp
                    )
                    .clip(shape = CircleShape)
                    .clickable(enabled = true, onClick = {
                        galleryLauncherProfile.launch("image/*")
                    }),
                contentScale = ContentScale.Crop,
                painter = if(state.profileImage.size == 0)
                    if(userData.profilePic == null)
                        painterResource(id = R.drawable.man)
                    else
                        rememberImagePainter(userData.profilePic)
                else
                    rememberImagePainter(state.profileImage[0]),
                contentDescription = "",
            )
            else
                Image(
                    modifier = Modifier
                        .zIndex(1f)
                        .width(100.dp)
                        .height(100.dp)
                        .offset(
                            if (state.scrollCurrent == "Down") 25.dp else 0.dp,
                            if (state.scrollCurrent == "Down") (-45).dp else 20.dp
                        )
                        .clip(shape = CircleShape)
                        .clickable(enabled = true, onClick = {
                            galleryLauncherProfile.launch("image/*")
                        }),
                    contentScale = ContentScale.Crop,
                    bitmap = state.profileImageBitmap,
                    contentDescription = "",
                )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .offset(y = if (state.scrollCurrent == "Down") (-80).dp else 35.dp),
                horizontalAlignment = if(state.scrollCurrent == "Down") Alignment.End else Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ){
                Column(){
                    Text(
                        modifier = if(state.scrollCurrent == "Down") Modifier else Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 3.4.em,
                        text = state.firstName.uppercase()+" "+state.lastName.uppercase(),
                    )
                    Row(
                        modifier = if(state.scrollCurrent == "Down") Modifier else Modifier.fillMaxWidth(),
                        horizontalArrangement = if(state.scrollCurrent == "Down") Arrangement.End else Arrangement.Center,
                    ){
                        Icon(imageVector = Icons.Filled.Male, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(text = "Male", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .offset(y = if (state.scrollCurrent == "Down") (-80).dp else 50.dp)
                    .verticalScroll(state = rememberScrollState(), enabled = true)
                    ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = "Profile Settings",
                        fontWeight = FontWeight.Bold,
                    )
                    if(state.changeProfileSettings)
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(enabled = true, onClick = {viewModel.onEvent(UserProfileEvent.changeProfileSettingsClicked)})
                        )
                    else
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(enabled = true, onClick = {viewModel.onEvent(UserProfileEvent.changeProfileSettingsClicked)})
                        )
                }
                Divider(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = state.firstName,
                        enabled = state.changeProfileSettings,
                        label = { Text(text = "First Name")},
                        onValueChange = {
                            viewModel.onEvent(UserProfileEvent.firstNameChange(it))
                        },

                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth(0.45f)
                    )
                    OutlinedTextField(
                        value = state.lastName,
                        enabled = state.changeProfileSettings,
                        label = { Text(text = "Last Name")},
                        onValueChange = {
                            viewModel.onEvent(UserProfileEvent.lastNameChange(it))
                        },

                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            viewModel.onEvent(UserProfileEvent.changeProfileSettingsClicked)
                        }),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth(0.85f)
                    )
                }
                OutlinedTextField(
                    enabled = false,
                    value = state.email,
                    label = { Text(text = "Email")},
                    onValueChange = {},
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
                    ),
                    trailingIcon = {
                                   Icon(imageVector = Icons.Filled.Verified, contentDescription = null,tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = "Change Password",
                        fontWeight = FontWeight.Bold,
                    )
                    if(state.changePassword)
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(enabled = true, onClick = {viewModel.onEvent(UserProfileEvent.changePasswordClicked)})
                        )
                    else
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(enabled = true, onClick = {viewModel.onEvent(UserProfileEvent.changePasswordClicked)})
                        )
                }
                Divider(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                )
                OutlinedTextField(
                    value = state.password,
                    singleLine = true,
                    placeholder = { Text(text = "Old Password")},
                    isError = state.passwordError != null,
                    enabled = state.changePassword,
                    label = { Text(text = "Old Password")},
                    onValueChange = {
                        viewModel.onEvent(UserProfileEvent.passwordChange(it))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
                    ),
                    visualTransformation = if(state.viewPassword) VisualTransformation.None   else PasswordVisualTransformation() ,
                    trailingIcon = { if(state.viewPassword) Icon(
                        Icons.Filled.VisibilityOff,
                        contentDescription = " View Password ",
                        modifier = Modifier
                            .clickable(onClick = {   viewModel.onEvent(UserProfileEvent.viewPasswordChange)   }, enabled = true) )
                    else Icon(
                        Icons.Filled.Visibility,
                        contentDescription = " View Password ",
                        modifier = Modifier
                            .clickable(onClick = {   viewModel.onEvent(UserProfileEvent.viewPasswordChange)  }, enabled = true) ) },
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxWidth()
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
                OutlinedTextField(
                    value = state.newPassword,
                    placeholder = { Text(text = "New Password")},
                    singleLine = true,
                    isError = state.newPasswordError != null,
                    enabled = state.changePassword,
                    label = { Text(text = "New Password")},
                    onValueChange = {
                        viewModel.onEvent(UserProfileEvent.newPasswordChange(it))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        viewModel.onEvent(UserProfileEvent.changePasswordClicked)
                    }),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
                    ),
                    visualTransformation = if(state.viewNewPassword) VisualTransformation.None   else PasswordVisualTransformation() ,
                    trailingIcon = { if(state.viewNewPassword) Icon(
                        Icons.Filled.VisibilityOff,
                        contentDescription = " View Password ",
                        modifier = Modifier
                            .clickable(onClick = {   viewModel.onEvent(UserProfileEvent.viewNewPasswordChange)   }, enabled = true) )
                    else Icon(
                        Icons.Filled.Visibility,
                        contentDescription = " View Password ",
                        modifier = Modifier
                            .clickable(onClick = {   viewModel.onEvent(UserProfileEvent.viewNewPasswordChange)  }, enabled = true) ) },

                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxWidth()
                )
                if (state.newPasswordError != null) {
                    Text(
                        text = state.newPasswordError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .fillMaxWidth(0.9f)
                    )
                }
                if(!state.isDriver)
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp, top = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Text(
                            text = "Become Driver",
                            fontWeight = FontWeight.Bold,
                        )
                        if(state.changeBecomeDriver)
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable(enabled = true, onClick = {viewModel.onEvent(UserProfileEvent.viewBecomeDriverChange)})
                            )
                        else
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable(enabled = true, onClick = {viewModel.onEvent(UserProfileEvent.viewBecomeDriverChange)})
                            )
                    }
                    Divider(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                    )
                    OutlinedTextField(
                        value = state.vehicleName,
                        placeholder = { Text(text = "Vehicle Name")},
                        singleLine = true,
                        isError = state.newPasswordError != null,
                        enabled = state.changeBecomeDriver,
                        label = { Text(text = "Vehicle Name")},
                        onValueChange = {
                            viewModel.onEvent(UserProfileEvent.vehicleNameChange(it))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Filled.ElectricCar,
                                contentDescription = null)
                        },
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                    )
                    if (state.vehicleNameError != null) {
                        Text(
                            text = state.vehicleNameError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .fillMaxWidth(0.9f)
                        )
                    }
                    OutlinedTextField(
                        value = state.vehicleModel,
                        placeholder = { Text(text = "Model No.")},
                        singleLine = true,
                        isError = state.newPasswordError != null,
                        enabled = state.changeBecomeDriver,
                        label = { Text(text = "Model No.")},
                        onValueChange = {
                            viewModel.onEvent(UserProfileEvent.vehicleModelChange(it))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Filled.FormatListNumbered,
                                contentDescription = null)
                        },
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                    )
                    if (state.vehicleModelError != null) {
                        Text(
                            text = state.vehicleModelError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .fillMaxWidth(0.9f)
                        )
                    }
                    OutlinedTextField(
                        value = state.cnic,
                        placeholder = { Text(text = "CNIC")},
                        singleLine = true,
                        isError = state.newPasswordError != null,
                        enabled = state.changeBecomeDriver,
                        label = { Text(text = "CNIC")},
                        onValueChange = {
                            viewModel.onEvent(UserProfileEvent.cnicChange(it))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    if (state.cnicError != null) {
                        Text(
                            text = state.cnicError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .fillMaxWidth(0.9f)
                        )
                    }
                    Text(
                        text = "Upload Some Important Documents",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 20.dp),
                        color = if(state.uploadingErrorMsg != null) MaterialTheme.colorScheme.error else if(state.changeBecomeDriver) Color.Black else Color.LightGray
                    )
                    Divider(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                        ){
                            Text(
                                text = "CNIC",
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Front Back",
                                fontWeight = FontWeight.Thin,
                            )
                        }
                        IconButton(enabled = state.changeBecomeDriver,onClick = { galleryLauncherCnic.launch("image/*")}) {
                            if(state.cnicPics.size < 2)
                                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                            else
                                Icon(imageVector = Icons.Filled.Done, contentDescription = null, tint = Color.Green)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                        ){
                            Text(
                                text = "Driving License",
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Front Back",
                                fontWeight = FontWeight.Thin,
                            )
                        }
                        IconButton(enabled = state.changeBecomeDriver,onClick = { galleryLauncherDrivingLicense.launch("image/*") }) {
                            if(state.drivingLicensePics.size < 2)
                                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                            else
                                Icon(imageVector = Icons.Filled.Done, contentDescription = null, tint = Color.Green)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                        ){
                            Text(
                                text = "Vehicle",
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "All 4 Sides & 2 Inside Pics",
                                fontWeight = FontWeight.Thin,
                            )
                        }
                        IconButton(enabled = state.changeBecomeDriver,onClick = { galleryLauncherCar.launch("image/*") }) {
                            if(state.carPics.size < 6)
                                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                            else
                                Icon(imageVector = Icons.Filled.Done, contentDescription = null, tint = Color.Green)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        ElevatedButton(
                            enabled = (state.carPics.size >= 6 && state.drivingLicensePics.size >= 2 && state.cnicPics.size >= 2 && state.uploadProgress == 0),
                            onClick = {
                                viewModel.onEvent(UserProfileEvent.uploadDriverData)
                            }) {
                            Text(text = if(state.uploading && state.uploadProgress == 10) "Uploaded" else if(state.uploading)"Uploading..." else "Upload")
                        }
                        Text(text = (state.uploadProgress * 10).toString()+"%", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
    Box(){
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 15.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if(state.updatingProfile)
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(25.dp)
                        .height(25.dp)
                )
            else
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    tint = if(state.profileImage.size > 0) MaterialTheme.colorScheme.primary else Color.Gray ,
                    modifier = Modifier.clickable(
                        enabled = state.profileImage.size > 0,
                        onClick = {
                            viewModel.onEvent(UserProfileEvent.updatingProfileChange(true))
                            viewModel.onEvent(UserProfileEvent.imageChanged(state.profileImage[0]))
                        }
                    )
                )
        }
    }
    Box{
        if(state.inAppNotificationMsg != null)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.Top) {
                    Snackbar(
                        action = {
                            IconButton(onClick = {
                                viewModel.onEvent(UserProfileEvent.inAppNofiticaitonMsgChange(null))
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "")
                            }
                        }
                    ){
                        Text(text = state.inAppNotificationMsg)
                    }
            }
    }
}