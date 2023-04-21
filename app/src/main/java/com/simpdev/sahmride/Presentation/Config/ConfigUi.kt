package com.simpdev.sahmride.Presentation.Config

import Domain.Data.auth
import Domain.Data.context
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simpdev.sahmride.MainScreen


@Composable
fun ConfigUi(){
    val viewModel = viewModel<ConfigViewModel>()
    val state = viewModel.state
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Configurations", fontSize = 5.em, fontWeight = FontWeight.SemiBold)
            Icon(imageVector = Icons.Filled.Settings, contentDescription = null)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Map style", fontWeight = FontWeight.SemiBold)
            Column {
                TextButton(onClick = {
                    viewModel.onEvent(ConfigEvents.expandMenuChange)
                }) {
                    Text(text = state.MapStyle)
                }
                DropdownMenu(
                    expanded = state.expandMenu,
                    onDismissRequest = {
                        viewModel.onEvent(ConfigEvents.expandMenuChange)
                    },
                ) {
                    DropdownMenuItem(
                        text = { Text("Default") },
                        onClick = { viewModel.onEvent(ConfigEvents.mapStyleChange("Default")) })
                    DropdownMenuItem(
                        text = { Text("Dark") },
                        onClick = { viewModel.onEvent(ConfigEvents.mapStyleChange("Dark")) })
                    DropdownMenuItem(
                        text = { Text("Light") },
                        onClick = { viewModel.onEvent(ConfigEvents.mapStyleChange("Light")) })
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            TextButton(onClick = {
                val sharedPreference = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
                var editor = sharedPreference?.edit()
                editor?.clear()
                auth.signOut()
                val restart = Intent(context, MainScreen::class.java)
                context?.startActivity(restart)
            }) {
                Text(text = "Logout")
                Icon(imageVector = Icons.Filled.Logout, contentDescription = null)
            }
        }

    }
}