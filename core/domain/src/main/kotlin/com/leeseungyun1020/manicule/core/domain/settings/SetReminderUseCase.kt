package com.leeseungyun1020.manicule.core.domain.settings

import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetReminderUseCase
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val reminderScheduler: ReminderScheduler,
    ) {
        suspend operator fun invoke(config: ReminderConfig) {
            val previousConfig = userPreferencesRepository.userPreferences.first().reminder
            userPreferencesRepository.setReminderConfig(config)

            val schedulingResult =
                runCatching {
                    if (config.enabled) {
                        reminderScheduler.schedule(config.time)
                    } else {
                        reminderScheduler.cancel()
                    }
                }
            schedulingResult.exceptionOrNull()?.let { failure ->
                withContext(NonCancellable) {
                    runCatching {
                        userPreferencesRepository.setReminderConfig(previousConfig)
                        if (previousConfig.enabled) {
                            reminderScheduler.schedule(previousConfig.time)
                        } else {
                            reminderScheduler.cancel()
                        }
                    }.exceptionOrNull()?.let(failure::addSuppressed)
                }
                throw failure
            }
        }
    }
