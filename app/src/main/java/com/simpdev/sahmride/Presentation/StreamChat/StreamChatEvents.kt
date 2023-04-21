package com.simpdev.sahmride.Presentation.StreamChat

sealed class StreamChatEvents
{
    data class changeCurrentScreen(val screen: CurrentScreen):StreamChatEvents()
    data class changeChannelId(val channelId:String):StreamChatEvents()
}
