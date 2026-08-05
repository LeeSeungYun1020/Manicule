package com.leeseungyun1020.manicule.navigation

import com.leeseungyun1020.manicule.feature.bookdetail.navigation.BookDetailRoute
import com.leeseungyun1020.manicule.feature.home.navigation.HomeRoute
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryRoute
import com.leeseungyun1020.manicule.feature.settings.navigation.SettingsRoute
import com.leeseungyun1020.manicule.feature.stats.navigation.StatsRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class TopLevelDestinationTest {
    @Test
    fun `top level destinations use feature owned routes`() {
        assertSame(HomeRoute, TopLevelDestination.HOME.route)
        assertSame(LibraryRoute, TopLevelDestination.LIBRARY.route)
        assertEquals(StatsRoute(), TopLevelDestination.STATS.route)
        assertSame(SettingsRoute, TopLevelDestination.SETTINGS.route)
    }

    @Test
    fun `typed routes preserve arguments and defaults`() {
        assertEquals("9788956746425", BookDetailRoute("9788956746425").isbn)
        assertNull(StatsRoute().focus)
        assertEquals("calendar", StatsRoute(focus = "calendar").focus)
    }
}
