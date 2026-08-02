package com.leeseungyun1020.manicule.core.domain.search

import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import javax.inject.Inject

class DeleteRecentQueryUseCase
    @Inject
    constructor(
        private val searchHistoryRepository: SearchHistoryRepository,
    ) {
        suspend operator fun invoke(query: String) {
            if (query.isNotBlank()) {
                searchHistoryRepository.removeQuery(query)
            }
        }
    }
