package kr.ilf.soodal

import android.app.Application
import kr.ilf.soodal.repository.SettingsRepository
import kr.ilf.soodal.repository.SettingsRepositoryImpl

class SoodalApplication : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepositoryImpl(applicationContext)
    }
}
