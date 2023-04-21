package com.simpdev.sahmride.Presentation.StreamChat

data class StreamChatState(
    val currentScreen: CurrentScreen = CurrentScreen.ChannelScreen,
    val channelId:String? = null
)

sealed class CurrentScreen{
    object ChannelScreen:CurrentScreen()
    object MessageScreen:CurrentScreen()
}
