package com.leeseungyun1020.manicule.core.domain.settings

import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalTime
import javax.inject.Inject

class GetActiveReminderTimeUseCase
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
    ) {
        suspend operator fun invoke(): LocalTime? =
            userPreferencesRepository
                .userPreferences
                .first()
                .reminder
                .takeIf { it.enabled }
                ?.time
    }
