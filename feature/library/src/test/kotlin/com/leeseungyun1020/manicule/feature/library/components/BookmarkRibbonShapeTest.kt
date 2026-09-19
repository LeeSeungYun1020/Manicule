package com.leeseungyun1020.manicule.feature.library.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class BookmarkRibbonShapeTest {
    @Test
    fun createOutline_producesGenericOutlineWithPath() {
        val shape = BookmarkRibbonShape(notchDepth = 3.dp)
        val density = Density(density = 1f)
        val size = Size(width = 9f, height = 30f)

        val outline = shape.createOutline(size, LayoutDirection.Ltr, density)

        assertThat(outline).isInstanceOf(Outline.Generic::class.java)
        val bounds = (outline as Outline.Generic).path.getBounds()
        assertThat(bounds.left).isEqualTo(0f)
        assertThat(bounds.top).isEqualTo(0f)
        assertThat(bounds.right).isEqualTo(9f)
        assertThat(bounds.bottom).isEqualTo(30f)
    }
}
