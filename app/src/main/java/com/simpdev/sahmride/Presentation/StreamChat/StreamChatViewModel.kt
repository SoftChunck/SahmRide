package com.simpdev.sahmride.Presentation.StreamChat

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class StreamChatViewModel: ViewModel() {
    var state by mutableStateOf(StreamChatState())

    fun onEvent(event: StreamChatEvents){
        when(event){
            is StreamChatEvents.changeCurrentScreen -> {
                state = state.copy(currentScreen = event.screen)
            }
            is StreamChatEvents.changeChannelId -> {

                Log.d("Channelllz",event.channelId.toString())
                state = state.copy(channelId = event.channelId, currentScreen = CurrentScreen.MessageScreen)
            }
        }
    }
}