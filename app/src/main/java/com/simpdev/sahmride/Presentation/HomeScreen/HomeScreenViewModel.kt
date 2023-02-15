package com.simpdev.sahmride.Presentation.HomeScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeScreenViewModel: ViewModel() {
    var state by mutableStateOf(HomeScreenState())
}