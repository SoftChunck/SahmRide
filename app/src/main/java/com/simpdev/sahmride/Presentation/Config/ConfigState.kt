package com.simpdev.sahmride.Presentation.Config

import com.simpdev.sahmride.Domain.Data.mapStyle

data class ConfigState(
    val MapStyle:String = mapStyle,
    val expandMenu:Boolean = false,
)
