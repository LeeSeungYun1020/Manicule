package com.leeseungyun1020.manicule.core.domain.search

import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import com.leeseungyun1020.manicule.core.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentQueriesUseCase
    @Inject
    constructor(
        private val searchHistoryRepository: SearchHistoryRepository,
    ) {
        operator fun invoke(limit: Int = 10): Flow<List<SearchQuery>> = searchHistoryRepository.observeRecentQueries(limit)
    }
