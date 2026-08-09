package com.leeseungyun1020.manicule.core.domain.settings

import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import javax.inject.Inject

class SetReminderUseCase
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val reminderScheduler: ReminderScheduler,
    ) {
        suspend operator fun invoke(config: ReminderConfig) {
            userPreferencesRepository.setReminderConfig(config)
            if (config.enabled) {
                reminderScheduler.schedule(config.time)
            } else {
                reminderScheduler.cancel()
            }
        }
    }
