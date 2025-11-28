package com.skash.sweeper

sealed interface UIEvent {

    data class SnackBar(val message: String) : UIEvent
}