package com.leeseungyun1020.manicule.core.notifications.di

import android.content.Context
import androidx.work.WorkManager
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import com.leeseungyun1020.manicule.core.notifications.AndroidReminderNotificationPublisher
import com.leeseungyun1020.manicule.core.notifications.ReminderNotificationPublisher
import com.leeseungyun1020.manicule.core.notifications.WorkManagerReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {
    @Binds
    @Singleton
    abstract fun bindReminderScheduler(impl: WorkManagerReminderScheduler): ReminderScheduler

    @Binds
    @Singleton
    abstract fun bindReminderNotificationPublisher(impl: AndroidReminderNotificationPublisher): ReminderNotificationPublisher

    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(
            @ApplicationContext context: Context,
        ): WorkManager = WorkManager.getInstance(context)
    }
}
