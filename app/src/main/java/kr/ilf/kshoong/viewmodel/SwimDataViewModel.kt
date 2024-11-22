package kr.ilf.kshoong.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ilf.kshoong.HealthConnectManager
import kr.ilf.kshoong.data.ExerciseSessionData
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class SwimDataViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    enum class UiState {
        Loading, Complete
    }

    val uiState = mutableStateOf(UiState.Loading)
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
    val permissionsContract = healthConnectManager.requestPermissionActivityContract()

    private val _swimDataFlow =
        MutableStateFlow<MutableMap<String, ExerciseSessionData>>(mutableMapOf())
    val swimDataFlow
        get() = _swimDataFlow.asStateFlow()

    val hasAllPermissions = mutableStateOf(false)

    init {
        viewModelScope.launch {
            hasAllPermissions.value = healthConnectManager.checkPermissions(healthPermissions)
        }
    }

    fun initSwimData() {
        viewModelScope.launch {
            val hasPermissions = healthConnectManager.checkPermissions(healthPermissions)
            if (hasPermissions) {
                val exerciseSessions = readExerciseRecords()
                exerciseSessions.forEach { exerciseSessionRecord ->
                    if (exerciseSessionRecord.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL) {
                        val uid = exerciseSessionRecord.metadata.id
                        val date = exerciseSessionRecord.startTime.plus(9L, ChronoUnit.HOURS).toString()

                        _swimDataFlow.value[date] = healthConnectManager.readAssociatedSessionData(uid)
                    }
                }
            }

            uiState.value = UiState.Complete
        }
    }

    private suspend fun readExerciseRecords(): List<ExerciseSessionRecord> {
        val startOfDay = ZonedDateTime.now().minusWeeks(1L).truncatedTo(ChronoUnit.DAYS)
        val now = Instant.now()
        val timeRangeFilter = TimeRangeFilter.between(startOfDay.toInstant(), now)

        return healthConnectManager.readExerciseSessions(timeRangeFilter)
    }
}

class SwimDataViewModelFactory(
    private val healthConnectManager: HealthConnectManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwimDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SwimDataViewModel(
                healthConnectManager = healthConnectManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}