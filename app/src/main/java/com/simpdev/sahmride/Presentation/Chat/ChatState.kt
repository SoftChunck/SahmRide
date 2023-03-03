package com.simpdev.sahmride.Presentation.Chat

data class ChatState(
    val msgToSend:String = "",
    val messagesList:List<Map<String,Any>> = emptyList()
)
