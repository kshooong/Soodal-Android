package kr.ilf.kshoong.viewmodel

import android.app.Application
import android.content.Context.MODE_PRIVATE
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kr.ilf.kshoong.HealthConnectManager
import kr.ilf.kshoong.database.database.SwimmingRecordDatabase
import kr.ilf.kshoong.database.entity.DailyRecord
import kr.ilf.kshoong.database.entity.DetailRecord
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class SwimmingViewModel(
    private val application: Application,
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

    fun initSwimmingData() {
        viewModelScope.launch {
            if (changeToken.value == null) {
                val startOfDay = ZonedDateTime.now().minusDays(30L).truncatedTo(ChronoUnit.DAYS)
                val now = Instant.now()
                val timeRangeFilter = TimeRangeFilter.between(startOfDay.toInstant(), now)

                val exerciseSessions = healthConnectManager.readExerciseSessions(timeRangeFilter)

                exerciseSessions.filter { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL }
                    .groupBy { it.startTime.truncatedTo(ChronoUnit.DAYS) }
                    .forEach { (date, records) ->
                        var totalDistance = 0.0
                        var totalCalories = 0.0
                        var totalActiveTime = Duration.ZERO
                        val detailRecords = mutableListOf<DetailRecord>()

                        records.forEach { session ->
                            val detailRecordResponse =
                                healthConnectManager.readDetailRecord(
                                    id = session.metadata.id,
                                    date,
                                    session.startTime,
                                    session.endTime
                                )

                            totalDistance += detailRecordResponse.distance?.toDouble() ?: 0.0
                            totalCalories += detailRecordResponse.energyBurned?.toDouble() ?: 0.0
                            totalActiveTime += Duration.parse(detailRecordResponse.activeTime)
                                ?: Duration.ZERO

                            detailRecords.add(detailRecordResponse)
                        }

                        val dailyRecord = DailyRecord(
                            date = date,
                            totalDistance = totalDistance.toString(),
                            totalActiveTime = totalActiveTime.toString(),
                            totalEnergyBurned = totalCalories.toString()
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            SwimmingRecordDatabase.getInstance(context = application)
                                ?.dailyRecordDao()
                                ?.insertDailyRecordWithDetailRecord(dailyRecord, detailRecords)
                        }
                    }


                val edit = application.getSharedPreferences("changeToken", MODE_PRIVATE).edit()
                edit.putString("changeToken", requestChangeToken())
                edit.apply()

            } else {

            }
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

    suspend fun requestChangeToken(): String? {
        setChangeToken(healthConnectManager.requestChangeToken())

        return changeToken.value
    }
}

class SwimmingViewModelFactory(
    private val application: Application,
    private val healthConnectManager: HealthConnectManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwimmingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SwimmingViewModel(
                application = application,
                healthConnectManager = healthConnectManager
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}