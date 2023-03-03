package com.simpdev.sahmride.Presentation.HomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUi(navController: NavController)
{
    val viewModel = viewModel<HomeScreenViewModel>()
    val state = viewModel.state

    
    LaunchedEffect(key1 = 1){
        viewModel.checkDriverStatus()
    }
    Column(){

        if(state.isDriver)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "Active",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                    Switch(checked = state.active, onCheckedChange = {
                        viewModel.onEvent(HomeScreenEvents.activeChange)
                    })
                }
            }
        if(state.active)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "Available Seats",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                    TextButton(onClick = {
                        viewModel.onEvent(HomeScreenEvents.expandMenuChange)
                    }) {
                        Text(text = state.availableSeats)
                    }
                    DropdownMenu(
                        expanded = state.expandMenu,
                        onDismissRequest = { },
                        offset = DpOffset(200.dp,(-40).dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("0") },
                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("0")) })
                        DropdownMenuItem(
                            text = { Text("1") },
                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("1"))  })
                        DropdownMenuItem(
                            text = { Text("2") },
                            onClick = {viewModel.onEvent(HomeScreenEvents.availableSeatsChange("2"))  })
                        DropdownMenuItem(
                            text = { Text("3") },
                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("3"))  })
                        DropdownMenuItem(
                            text = { Text("4") },
                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("4"))  })
                        DropdownMenuItem(
                            text = { Text("5") },
                            onClick = { viewModel.onEvent(HomeScreenEvents.availableSeatsChange("5"))  })
                    }
                }
                Row( modifier = Modifier) {

                }
            }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {

            ElevatedCard(
                onClick = {}
            ) {
                Column(modifier = Modifier
                    .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CarRental, contentDescription = "Ride",
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                    )
                    Text(text = "Find Ride Now")
                }
            }
            ElevatedCard(
                onClick = {}
            ) {
                Column(modifier = Modifier
                    .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Timer, contentDescription = "Ride",
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                    )
                    Text(text = "Schedule Ride")
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 20.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ),
            
        ){
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
                    ){
                Text(
                    text = "History",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
                Icon(imageVector = Icons.Filled.History, contentDescription = null)
            }

        }
    }
}