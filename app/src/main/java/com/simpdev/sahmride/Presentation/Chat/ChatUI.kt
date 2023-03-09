package com.simpdev.sahmride.Presentation.Chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simpdev.sahmride.Presentation.Navigation.userInfo
import com.simpdev.sahmride.Presentation.Trip.TripEvents
import com.simpdev.sahmride.Presentation.Trip.TripUserEvents
import com.simpdev.sahmride.Presentation.Trip.TripUserViewModel
import com.simpdev.sahmride.Presentation.Trip.TripViewModel
import com.simpdev.sahmride.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatUi(
    tripViewModel: TripViewModel?,
    tripUserViewModel: TripUserViewModel?,
    userUids: List<String>,
    userType: String,
    userInfo: userInfo
) {
    val viewModel = viewModel<ChatViewModel>()
    val state = viewModel.state
    LaunchedEffect(key1 = 1, block = {
        viewModel.onEvent(ChatEvents.listenForMessages(userUid = userUids[0], userType = userType))
    })
    Scaffold(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(end = 15.dp, top = 5.dp, bottom = 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            tripViewModel?.onEvent(TripEvents.tripHomeClicked)
                            tripUserViewModel?.onEvent(TripUserEvents.tripHomeClicked)
                        },
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                    if(userInfo.userPic == null){
                        Image(
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp)
                                .clip(shape = CircleShape),
                            contentScale = ContentScale.Crop,
                            painter = painterResource(id = R.drawable.man),
                            contentDescription = null,
                        )
                    }
                    else{
                        Image(
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp)
                                .clip(shape = CircleShape),
                            contentScale = ContentScale.Crop,
                            bitmap = userInfo.userPic!!,
                            contentDescription = null,
                        )
                    }
                    Text(
                        modifier = Modifier
                            .padding(start = 10.dp),
                        text = userInfo.firstName +" "+ userInfo.lastName,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row() {
                    Icon(imageVector = Icons.Filled.Call,contentDescription = null)
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .background(color = MaterialTheme.colorScheme.background)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    OutlinedTextField(
                        placeholder = {Text(text = "Message ...")},
                        singleLine = true,
                        value = state.msgToSend,
                        onValueChange = {
                            viewModel.onEvent(ChatEvents.msgChange(it))
                        })
                    IconButton(
                        onClick = {
                            viewModel.onEvent(ChatEvents.sendMsgTo(userUids[0],userType = userType))
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null )
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState(), enabled = true)
                .padding(it)
        ) {
            state.messagesList.forEach {
                if(it.get("sendBy").toString() == userType)
                {
                    senderMessage(msg = it.get("msg").toString())
                }
                else{
                    reciverMessage(msg = it.get("msg").toString())
                }
            }
        }
    }
}

@Composable
fun senderMessage( msg: String){
    Row(
        modifier = Modifier
            .padding(vertical = 7.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ){
        Row(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(15.dp)
                )
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = msg)
        }
    }
}
@Composable
fun reciverMessage( msg: String){
    Row(
        modifier = Modifier
            .padding(vertical = 7.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ){
        Row(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(15.dp)
                )
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = msg)
        }
    }
}
