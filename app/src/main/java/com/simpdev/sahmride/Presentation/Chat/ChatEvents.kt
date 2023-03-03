package com.simpdev.sahmride.Presentation.Chat

sealed class ChatEvents{
    data class sendMsgTo(val userUid: String, val userType: String):ChatEvents()
    data class msgChange(val msg:String):ChatEvents()
    data class listenForMessages(val userUid: String, val userType: String):ChatEvents()
}
