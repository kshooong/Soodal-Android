package kr.ilf.soodal.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kr.ilf.soodal.repository.SettingsRepository

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _notificationPermissionDialogVisible = mutableStateOf(false)
    val notificationPermissionDialogVisible : State<Boolean>
        get() = _notificationPermissionDialogVisible

    fun setNotificationPermissionDialogVisible(visible: Boolean) {
        _notificationPermissionDialogVisible.value = visible
    }

    // 알림
    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    // 새 기록 알릶
    val newSessionNotificationsEnabled: StateFlow<Boolean> =
        settingsRepository.newSessionNotificationsEnabled
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    fun setNewSessionNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNewSessionNotificationsEnabled(enabled)
        }
    }
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}