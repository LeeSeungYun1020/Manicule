package com.leeseungyun1020.manicule.core.domain.search

import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import javax.inject.Inject

class SaveRecentQueryUseCase
    @Inject
    constructor(
        private val searchHistoryRepository: SearchHistoryRepository,
    ) {
        suspend operator fun invoke(query: String) {
            searchHistoryRepository.saveQuery(query)
        }
    }
