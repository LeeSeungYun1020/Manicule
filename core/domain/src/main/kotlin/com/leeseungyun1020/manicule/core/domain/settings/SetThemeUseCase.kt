package com.leeseungyun1020.manicule.core.domain.settings

import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.model.ThemeMode
import javax.inject.Inject

class SetThemeUseCase
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
    ) {
        suspend operator fun invoke(themeMode: ThemeMode) {
            userPreferencesRepository.setThemeMode(themeMode)
        }
    }
