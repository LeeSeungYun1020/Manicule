package com.leeseungyun1020.manicule.feature.scanner

import android.view.Surface
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DisplayRotationTrackerTest {
    private var rotation: Int? = Surface.ROTATION_0
    private val applied = mutableListOf<Int>()
    private val tracker = DisplayRotationTracker(displayId = { 7 }, rotation = { rotation }, applyRotation = applied::add)

    @Test
    fun currentDisplayRotationIsReadOnBindAndResume() {
        tracker.refresh()
        rotation = Surface.ROTATION_270
        tracker.refresh()
        assertThat(applied).containsExactly(Surface.ROTATION_0, Surface.ROTATION_270).inOrder()
    }

    @Test
    fun allFourDisplayRotationsAreForwardedWithoutAngleConversion() {
        val rotations = listOf(Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_180, Surface.ROTATION_270)
        rotations.forEach {
            rotation = it
            tracker.onDisplayChanged(7)
        }
        assertThat(applied).containsExactlyElementsIn(rotations).inOrder()
    }

    @Test
    fun anotherDisplayAndDetachedViewAreIgnored() {
        tracker.onDisplayChanged(3)
        rotation = null
        tracker.refresh()
        assertThat(applied).isEmpty()
    }
}
