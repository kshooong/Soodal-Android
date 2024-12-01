package kr.ilf.kshoong.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kr.ilf.kshoong.HealthConnectManager
import kr.ilf.kshoong.database.entity.DailyRecord

class SwimmingViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    val healthPermissions =
        setOf(
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(SpeedRecord::class),
            HealthPermission.getWritePermission(SpeedRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getWritePermission(DistanceRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getWritePermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        )
    val hasAllPermissions = mutableStateOf(false)
    val permissionsContract = healthConnectManager.requestPermissionActivityContract()

    val changeToken = mutableStateOf<String?>(null)

    private val _swimmingData = MutableStateFlow<MutableList<DailyRecord>>(mutableListOf())
    val swimmingData
        get() = _swimmingData

    init {
        viewModelScope.launch {
            hasAllPermissions.value = healthConnectManager.checkPermissions(healthPermissions)
        }
    }

    fun checkPermissions(): Boolean {
        viewModelScope.launch {
            hasAllPermissions.value = healthConnectManager.checkPermissions(healthPermissions)
        }

        return hasAllPermissions.value
    }

    fun setChangeToken(token: String?) {
        changeToken.value = token
    }
}

class SwimmingViewModelFactory(
    private val healthConnectManager: HealthConnectManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwimmingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SwimmingViewModel(
                healthConnectManager = healthConnectManager
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}