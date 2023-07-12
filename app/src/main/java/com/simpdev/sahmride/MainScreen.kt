package com.simpdev.sahmride

import Domain.Data.auth
import Domain.Data.chatClient
import Domain.Data.context
import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simpdev.sahmride.Domain.Data.mapStyle
import com.simpdev.sahmride.Presentation.ForgotPassword.ForgotPasswordUI
import com.simpdev.sahmride.Presentation.SignIn.SignInUI
import com.simpdev.sahmride.Presentation.SignUp.SignUpUI
import com.simpdev.sahmride.Presentation.StartingPage.StartingPageUi
import com.simpdev.sahmride.ui.theme.AppTheme
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory


class MainScreen : ComponentActivity() {

    var locationAccessGranted = false

    @RequiresApi(Build.VERSION_CODES.N)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                locationAccessGranted = true
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                locationAccessGranted = true
            }

            else -> {
                locationAccessGranted = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(ExperimentalUnitApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
//        val color = SurfaceColors.SURFACE_2.getColor(this)
//        window.statusBarColor = color
//        window.navigationBarColor = color
//        val windowInsetsController =
//            WindowCompat.getInsetsController(window, window.decorView)
//        // Configure the behavior of the hidden system bars.
//        windowInsetsController.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(
                // Enables the background sync which is performed to sync user actions done without the Internet connection.
                backgroundSyncEnabled = true,
                // Enables the ability to receive information about user activity such as last active date and if they are online right now.
                userPresence = true,
                // Enables using the database as an internal caching mechanism.
                persistenceEnabled = true,
                // An enumeration of various network types used as a constraint inside upload attachments worker.
                uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
                // Whether the SDK will use a new sequential event handling mechanism.
                useSequentialEventHandler = false,
            ),
            appContext = applicationContext,
        )
        chatClient =
            ChatClient.Builder("z2r8ukcuazvc", applicationContext).withPlugin(offlinePluginFactory)
                .build()

        if (!locationAccessGranted) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .scrollable(
                            state = rememberScrollState(0),
                            orientation = Orientation.Vertical
                        )
                        .background(color = MaterialTheme.colorScheme.background)
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )
                {
                    val viewModel = viewModel<MainScreenViewModel>()

                    if (context == null) {
                        val sharedPreference = LocalContext.current?.getSharedPreferences(
                            "Configs",
                            Context.MODE_PRIVATE
                        )
                        mapStyle = sharedPreference?.getString("mapstyle", "Default").toString()
                        context = LocalContext.current
                    }
                    LaunchedEffect(key1 = null) {
                        if (auth.currentUser != null) {
                            if (auth.currentUser?.isEmailVerified == true) {
                                viewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.HomeScreen))
                            } else {
                                viewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.StartingPage))
                            }
                        }
                    }
                    val state = viewModel.state
                    when (state.currentScreen) {
                        is CurrentScreen.StartingPage -> {
                            StartingPageUi(viewModel)
                        }

                        is CurrentScreen.SignIn -> {
                            SignInUI(viewModel)
                        }

                        is CurrentScreen.SignUp -> {
                            SignUpUI(viewModel)
                        }

                        is CurrentScreen.ForgotPassword -> {
                            ForgotPasswordUI(mainScreenViewModel = viewModel)
                        }

                        is CurrentScreen.HomeScreen -> {
                            Navigation(LocalContext.current)
//                            abc()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun abc(){
    Column(
        modifier = Modifier
            .background(color = Color.Blue, shape = RoundedCornerShape(4.dp))
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        ElevatedButton(onClick = { /*TODO*/ }, modifier = Modifier.width(100.dp)) {
            Text(text = "Hi", fontSize =20.em)
        }
        ElevatedButton(onClick = { /*TODO*/ },modifier = Modifier.width(100.dp)) {
                Text(text = "Hi")

        }
        Divider(modifier =Modifier.fillMaxWidth(0.5f))
        Text(text = "Hi there ", fontSize = 10.em, fontFamily = FontFamily.Serif, color = Color.White)
        Icon(imageVector = Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.White)
        Image(painter = painterResource(id = R.drawable.seats), contentDescription = null)
    }
}