package com.simpdev.sahmride.Presentation.Review

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.simpdev.sahmride.Presentation.Navigation.NavigationEvent
import com.simpdev.sahmride.Presentation.Navigation.NavigationViewModel
import com.simpdev.sahmride.Presentation.Navigation.userInfo
import com.simpdev.sahmride.destinationLocation
import com.simpdev.sahmride.mapNav
import com.simpdev.sahmride.pickupLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewUi(
    userInfo: userInfo,
    navigationViewModel: NavigationViewModel
){
    
    val viewModel = viewModel<ReviewViewModel>()
    val state = viewModel.state
    
    LaunchedEffect(key1 = 1, block = {
        viewModel.onEvent(ReviewEvents.setUserUid(userInfo.userUid))
    })
    val compositon by rememberLottieComposition(spec = LottieCompositionSpec.Url("https://assets10.lottiefiles.com/packages/lf20_s2lryxtd.json"))
    Column(
        modifier = Modifier
            .background(color = Color.White)
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        if(state.reviewSubmitted)
        {
            LottieAnimation(composition = compositon, iterations = 1)
            Row(
                modifier = Modifier.clickable(enabled = true, onClick = {
                    pickupLocation = null
                    destinationLocation = null
                    mapNav.onDestroy()
                    navigationViewModel.onEvent(NavigationEvent.rideCancelled)
                })
            ){
                Icon(imageVector = Icons.Filled.ArrowBackIos, contentDescription = null )
                Text(text = "back to home")
            }
        }
        else {
            Text(
                modifier = Modifier
                    .padding(vertical = 5.dp),
                textAlign = TextAlign.Center,
                text = "How was your ride ?",
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(vertical = 5.dp),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                text = "Rate ${userInfo.firstName} about your riding experience ",
            )
            if (userInfo.userPic == null)
                Image(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .width(100.dp)
                        .height(100.dp)
                        .clip(shape = CircleShape),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = com.simpdev.sahmride.R.drawable.man),
                    contentDescription = "",
                )
            else
                Image(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .width(100.dp)
                        .height(100.dp)
                        .clip(shape = CircleShape),
                    contentScale = ContentScale.Crop,
                    bitmap = userInfo.userPic!!,
                    contentDescription = "",
                )
            Text(
                modifier = Modifier
                    .padding(vertical = 5.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 3.4.em,
                text = "${userInfo.firstName} ${userInfo.lastName}",
            )
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp),
            ) {
                Icon(
                    imageVector = if (state.rating >= 1) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(
                        enabled = true,
                        onClick = { viewModel.onEvent(ReviewEvents.ratingChanged(1)) })
                )
                Icon(
                    imageVector = if (state.rating >= 2) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(
                        enabled = true,
                        onClick = { viewModel.onEvent(ReviewEvents.ratingChanged(2)) })
                )
                Icon(
                    imageVector = if (state.rating >= 3) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(
                        enabled = true,
                        onClick = { viewModel.onEvent(ReviewEvents.ratingChanged(3)) })
                )
                Icon(
                    imageVector = if (state.rating >= 4) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(
                        enabled = true,
                        onClick = { viewModel.onEvent(ReviewEvents.ratingChanged(4)) })
                )
                Icon(
                    imageVector = if (state.rating >= 5) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(
                        enabled = true,
                        onClick = { viewModel.onEvent(ReviewEvents.ratingChanged(5)) })
                )
            }
            OutlinedTextField(
                modifier = Modifier
                    .defaultMinSize(minHeight = 40.dp)
                    .padding(vertical = 5.dp, horizontal = 15.dp),
                value = state.review,
                onValueChange = { viewModel.onEvent(ReviewEvents.reviewChange(it)) },
                maxLines = 7,
                placeholder = { Text(text = "Review...") }
            )

            Button(
                modifier = Modifier
                    .padding(vertical = 5.dp),
                onClick = { viewModel.onEvent(ReviewEvents.submitReview) }) {
                Text(text = "Share Review")
            }
        }

    }
}