package kr.ilf.soodal.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val notificationsEnabled: Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)

    val newSessionNotificationsEnabled: Flow<Boolean>
    suspend fun setNewSessionNotificationsEnabled(enabled: Boolean)
}