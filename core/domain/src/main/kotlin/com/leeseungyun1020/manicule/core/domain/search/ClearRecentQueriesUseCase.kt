package com.leeseungyun1020.manicule.core.domain.search

import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import javax.inject.Inject

class ClearRecentQueriesUseCase
    @Inject
    constructor(
        private val searchHistoryRepository: SearchHistoryRepository,
    ) {
        suspend operator fun invoke() {
            searchHistoryRepository.clearHistory()
        }
    }
