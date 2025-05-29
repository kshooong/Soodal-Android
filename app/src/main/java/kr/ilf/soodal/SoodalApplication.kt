package kr.ilf.soodal

import android.app.Application
import android.app.NotificationManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kr.ilf.soodal.repository.SettingsRepository
import kr.ilf.soodal.repository.SettingsRepositoryImpl
import kr.ilf.soodal.util.NotificationUtil
import kr.ilf.soodal.worker.NewSessionNotificationWorker
import java.util.concurrent.TimeUnit

class SoodalApplication : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()

        settingsRepository = SettingsRepositoryImpl(applicationContext)

        createNotificationChannels()
        startSwimmingCheckWorker()
    }

    private fun createNotificationChannels() {
        val notificationUtil = NotificationUtil.getInstance(this)

        notificationUtil.createNotificationChannel(
            NotificationUtil.CHANNEL_ID_NEW_SESSIONS,
            resources.getString(R.string.channel_name_new_sessions),
            NotificationManager.IMPORTANCE_DEFAULT,
            resources.getString(R.string.channel_description_new_sessions)
        )
    }

    private fun startSwimmingCheckWorker() {
        val newSessionNotificationWorkRequest =
            PeriodicWorkRequestBuilder<NewSessionNotificationWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            NewSessionNotificationWorker.NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            newSessionNotificationWorkRequest
        )
    }
}
