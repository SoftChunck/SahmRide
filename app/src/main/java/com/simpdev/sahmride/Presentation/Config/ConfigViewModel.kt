package com.simpdev.sahmride.Presentation.Config

import Domain.Data.context
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.simpdev.sahmride.Domain.Data.mapStyle
import com.simpdev.sahmride.mapNav
import com.simpdev.sahmride.mapView

class ConfigViewModel : ViewModel() {

    var state by mutableStateOf(ConfigState())
    fun onEvent(event: ConfigEvents){
        when(event)
        {
            is ConfigEvents.expandMenuChange -> {
                state = state.copy(expandMenu = !state.expandMenu)
            }
            is ConfigEvents.mapStyleChange -> {
                state = state.copy(MapStyle = event.mapstyle,expandMenu = !state.expandMenu)
                mapStyle = event.mapstyle
                mapView = null
                try{
                    mapNav.onDestroy()
                }
                catch (e:UninitializedPropertyAccessException){

                }
                val sharedPreference = context?.getSharedPreferences("Configs", Context.MODE_PRIVATE)
                var editor = sharedPreference?.edit()
                if(editor != null){
                    editor.putString("mapstyle",state.MapStyle)
                    editor.commit()
                }
            }
        }
    }


}