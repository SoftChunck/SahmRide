package com.simpdev.sahmride.Presentation.Config

sealed class ConfigEvents
{
    object expandMenuChange: ConfigEvents()
    data class mapStyleChange(val mapstyle:String):ConfigEvents()
}

