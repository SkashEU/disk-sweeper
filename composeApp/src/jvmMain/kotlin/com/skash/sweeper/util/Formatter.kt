package com.skash.sweeper.util

import com.skash.sweeper.domain.model.PlatformConfiguration
import kotlin.math.log10
import kotlin.math.pow

//TODO: Move this into own module
fun Long.toHumanReadableSize(): String {
    if (this <= 0) return "0 B"
    val base = PlatformConfiguration.getPlatformConfiguration().fileSizeBase.toDouble()
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(base)).toInt()

    if (digitGroups >= units.size) return "Unknown"

    val value = this / base.pow(digitGroups.toDouble())
    val roundedValue = ((value * 100).toLong() / 100.0)
    val stringValue = if (roundedValue % 1.0 == 0.0) {
        roundedValue.toLong().toString()
    } else {
        roundedValue.toString()
    }
    return "$stringValue ${units[digitGroups]}"
}

fun Long.formatSecondsToDigital(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    return if (hours > 0) {
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}