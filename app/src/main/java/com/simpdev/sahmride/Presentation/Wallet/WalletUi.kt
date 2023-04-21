package com.simpdev.sahmride.Presentation.Wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.simpdev.sahmride.R

@Composable
fun WalletUI(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(imageVector = Icons.Filled.ArrowBackIos, contentDescription = null)
                Text(
                    text = "Wallet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(imageVector = Icons.Filled.Settings, contentDescription = null)
            }
            Column(
                modifier = Modifier
                    .padding(vertical = 45.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Balance",
                    color = Color.Gray, fontSize =  2.8.em
                )
                Text(
                    text = "Rs.2000",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(painterResource(id = R.drawable.send), contentDescription = null,modifier = Modifier.size(32.dp))
                    Text(text = "Send", fontSize =  2.8.em)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(painterResource(id = R.drawable.deposit), contentDescription = null,modifier = Modifier.size(32.dp))
                    Text(text = "Deposit", fontSize =  2.8.em)
                }
            }
            Column(
                modifier = Modifier
                    .padding(top = 40.dp, start = 3.dp, end = 3.dp)
                    .shadow(elevation = 40.dp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(topStart = 80f, topEnd = 90f)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 15.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Transaction History",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                }
                Divider(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                )
                Row(
                    modifier = Modifier
                        .padding(horizontal = 15.dp, vertical = 10.dp)
                        .fillMaxWidth()
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Sat, 10th March 2023",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp )
                    Text(
                        text = "+2000 Rs",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                    )
                }

            }
        }
    }
}

@Composable
fun PaymentDialog(){
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.Transparent),
    ){
        Column(
            modifier = Modifier
                .background(
                    color = Color.Transparent,
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(20)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .padding(70.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .clip(shape = CircleShape),
                        contentScale = ContentScale.Crop,
                        painter = painterResource(id = R.drawable.man),
                        contentDescription = null
                    )
                    Text(
                        text = "USAMA MUNEEB",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 5.dp),
                        )
                    Text(
                        modifier = Modifier.padding(vertical = 5.dp),
                        text = "Rs.2000",
                        fontSize  = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Pay")
                    }
                }
            }
        }
    }
}