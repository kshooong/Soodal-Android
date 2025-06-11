package kr.ilf.soodal.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// msms 정리 및 공부 필요
// DataStore 인스턴스를 위한 Context 확장 프로퍼티
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    override val notificationsEnabled: Flow<Boolean> =
        getPreferenceFlow(PreferencesKeys.NOTIFICATIONS_ENABLED, false)

    override suspend fun setNotificationsEnabled(enabled: Boolean) =
        setPreferenceValue(PreferencesKeys.NOTIFICATIONS_ENABLED, enabled)


    override val newSessionNotificationsEnabled: Flow<Boolean> =
        getPreferenceFlow(PreferencesKeys.NEW_SESSION_NOTIFICATIONS_ENABLED, false)

    override suspend fun setNewSessionNotificationsEnabled(enabled: Boolean) =
        setPreferenceValue(PreferencesKeys.NEW_SESSION_NOTIFICATIONS_ENABLED, enabled)


    // Worker에서 사용할 수 있도록 단일 값을 가져오는 함수
    suspend fun getNotificationsEnabledOnce(): Boolean =  notificationsEnabled.first()
    suspend fun getNewSessionNotificationsEnabledOnce(): Boolean =  newSessionNotificationsEnabled.first()


    private fun <T> getPreferenceFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.settingsDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    private suspend fun <T> setPreferenceValue(key: Preferences.Key<T>, value: T) {
        try {
            context.settingsDataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NEW_SESSION_NOTIFICATIONS_ENABLED =
            booleanPreferencesKey("new_session_notifications_enabled")
    }
}