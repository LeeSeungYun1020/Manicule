package com.leeseungyun1020.manicule.feature.scanner

/** Reads the hosting display, never the device's physical sensor angle. */
internal class DisplayRotationTracker(
    private val displayId: () -> Int?,
    private val rotation: () -> Int?,
    private val applyRotation: (Int) -> Unit,
) {
    fun refresh() {
        rotation()?.let(applyRotation)
    }

    fun onDisplayChanged(changedDisplayId: Int) {
        if (changedDisplayId == displayId()) refresh()
    }
}
