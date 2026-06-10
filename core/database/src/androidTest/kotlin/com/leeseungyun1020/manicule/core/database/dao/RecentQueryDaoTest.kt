package com.leeseungyun1020.manicule.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.ManiculeDatabase
import com.leeseungyun1020.manicule.core.database.entity.RecentQueryEntity
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test

class RecentQueryDaoTest {
    private lateinit var db: ManiculeDatabase
    private lateinit var dao: RecentQueryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ManiculeDatabase::class.java).build()
        dao = db.recentQueryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun upsert_and_observe_ordered() =
        runTest {
            dao.upsert(RecentQueryEntity("query1", Instant.fromEpochMilliseconds(100)))
            dao.upsert(RecentQueryEntity("query2", Instant.fromEpochMilliseconds(200)))

            dao.observeRecent(10).test {
                val items = awaitItem()
                assertThat(items).hasSize(2)
                assertThat(items[0].query).isEqualTo("query2")
                assertThat(items[1].query).isEqualTo("query1")
                cancelAndIgnoreRemainingEvents()
            }
        }
}
