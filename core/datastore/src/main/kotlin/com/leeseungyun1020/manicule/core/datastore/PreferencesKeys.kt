package com.leeseungyun1020.manicule.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val REMINDER_TIME = stringPreferencesKey("reminder_time")
}
