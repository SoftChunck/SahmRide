package com.simpdev.sahmride

import Domain.Data.auth
import Domain.Data.context
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.SahmRideTheme
import com.simpdev.sahmride.Presentation.ForgotPassword.ForgotPasswordUI
import com.simpdev.sahmride.Presentation.SignIn.SignInUI
import com.simpdev.sahmride.Presentation.SignUp.SignUpUI


class MainScreen : ComponentActivity() {

    var locationAccessGranted = false
    private val LocationCallback = LocationListeningCallback(this)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                locationAccessGranted = true
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                locationAccessGranted = true
            } else -> {
            locationAccessGranted= false
        }
        }
    }

    @OptIn(ExperimentalUnitApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if(!locationAccessGranted)
        {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContent {
            SahmRideTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .scrollable(state = rememberScrollState(0), orientation = Orientation.Vertical)
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )
                {

                    val viewModel = viewModel<MainScreenViewModel>()

                    if(context == null)
                    {
                        context = LocalContext.current
                    }
                    LaunchedEffect(key1 = null){

                        if(auth.currentUser != null)
                        {
                            viewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.HomeScreen))
                            if(auth.currentUser == null)
                            {
                                viewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.SignIn))
                            }
                        }
                    }
                    val state = viewModel.state
                    when(state.currentScreen){
                        is CurrentScreen.SignIn ->{
                            SignInUI(viewModel)
                        }
                        is CurrentScreen.SignUp ->{
                            SignUpUI(viewModel)
                        }
                        is CurrentScreen.ForgotPassword ->{
                            ForgotPasswordUI(mainScreenViewModel = viewModel)
                        }
                        is CurrentScreen.HomeScreen -> {
                            Navigation(LocalContext.current,LocationCallback)
                        }
                    }
                }
            }
        }
    }
}
