package com.simpdev.sahmride.Presentation.StartingPage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.simpdev.sahmride.CurrentScreen
import com.simpdev.sahmride.MainScreenEvents
import com.simpdev.sahmride.MainScreenViewModel
import com.simpdev.sahmride.R

@Composable
fun StartingPageUi(viewModel: MainScreenViewModel) {
    var selectedIndex by remember {
        mutableStateOf(0)
    }
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    var (x, y) = dragAmount
                    when {
                        x > 80 -> { /* right */
                            if (selectedIndex > 0) {
                                selectedIndex -= 1
                            }

                        }

                        x < 80 -> { /* left */
                            if (selectedIndex < 2) {
                                selectedIndex += 1
                            }
                        }
                    }
                    when {
                        y > 0 -> { /* down */
                        }

                        y < 0 -> {
                            /* up */
                        }
                    }
                }
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(selectedIndex == 0)
        {
            Image(painter = painterResource(id = R.drawable.page0), contentDescription = null )
            Text(text = "Setting Pickup Point", fontSize = 7.em, fontWeight = FontWeight.SemiBold,color = MaterialTheme.colorScheme.primary)
            Text(text = "Select your pickup point & destination by searching on map or by dragging the cursor over the map by two fingers",
                textAlign = TextAlign.Center,modifier = Modifier.padding(horizontal = 20.dp,))
        }
        else if(selectedIndex == 1)
        {
            Image(painter = painterResource(id = R.drawable.page00), contentDescription = null )
            Text(text = "Get Ride", fontSize = 7.em, fontWeight = FontWeight.SemiBold,color = MaterialTheme.colorScheme.primary)
            Text(text = "Select your pickup point & destination by searching on map or by dragging the cursor over the map by two fingers",
                textAlign = TextAlign.Center,modifier = Modifier.padding(horizontal = 20.dp,))
        }
        else
        {
            Image(painter = painterResource(id = R.drawable.page000), contentDescription = null )
            Text(text = "Best Driver", fontSize = 7.em, fontWeight = FontWeight.SemiBold,color = MaterialTheme.colorScheme.primary)
            Text(text = "Select your pickup point & destination by searching on map or by dragging the cursor over the map by two fingers",
                textAlign = TextAlign.Center,modifier = Modifier.padding(horizontal = 20.dp,))
            ElevatedButton(
                modifier = Modifier.padding(vertical = 10.dp),
                onClick = {
                    viewModel.onEvent(MainScreenEvents.ChangeScreen(CurrentScreen.SignIn))
                }) {
                Text(text = "Get Started")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        DotsIndicator(totalDots = 3, selectedIndex = selectedIndex)
    }
}

@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int
) {

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), horizontalArrangement = Arrangement.Center
    ) {

        items(totalDots) { index ->
            if (index == selectedIndex) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.primary)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color = Color.LightGray)
                )
            }

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }
    }
}