package com.simpdev.sahmride.Presentation.Chat

import Domain.Data.auth
import Domain.Data.db
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ChatViewModel: ViewModel()

{
    var state by mutableStateOf(ChatState())


    fun onEvent(event: ChatEvents)
    {
        when(event){
            is ChatEvents.sendMsgTo -> {
                val msg = hashMapOf(
                    "msg" to state.msgToSend,
                    "sendBy" to event.userType,
                    "timeStamp" to System.currentTimeMillis()
                )
                auth.currentUser?.uid?.let {
                    db.collection("users").document(if(event.userType == "Driver") it else event.userUid).collection("chats").document(if(event.userType == "Driver") event.userUid else it ).collection("messages").document()
                        .set(msg)
                        .addOnSuccessListener { documentReference ->
                            state = state.copy(msgToSend = "")
                            Log.d("Msg Sent", "Successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.w("User in DB Failure", "Error adding document", e)
                        }
                }
            }
            is ChatEvents.msgChange -> {
                state = state.copy(msgToSend = event.msg)
            }
            is ChatEvents.listenForMessages -> {
                val ref =  db.collection("users").document((if(event.userType == "Driver") auth.currentUser?.uid else event.userUid)!!).collection("chats").document(
                    (if(event.userType == "Driver") event.userUid else auth.currentUser?.uid)!!
                ).collection("messages").orderBy("timeStamp")
                ref.addSnapshotListener { value, error ->
                    state = state.copy()
                    if(error != null){
                        Log.d("Error","LISTINING TO MEssgaes Error")
                    }
                    val list = emptyList<Map<String,Any>>().toMutableList()
                    if(value != null ){
                        for( doc in value){
                            list.add(doc.data)
                        }
                    }
                    state = state.copy(messagesList = list)
                }
            }
        }
    }
}