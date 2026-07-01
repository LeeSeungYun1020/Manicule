package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.data.datasource.ReadingRecordLocalDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class StatsRepositoryImpl
    @Inject
    constructor(
        private val readingRecordLocalDataSource: ReadingRecordLocalDataSource,
    ) : StatsRepository {
        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> =
            readingRecordLocalDataSource
                .observeBetween(start, end)
                .map { list ->
                    list.map { it.asExternalModel() }
                }
    }
