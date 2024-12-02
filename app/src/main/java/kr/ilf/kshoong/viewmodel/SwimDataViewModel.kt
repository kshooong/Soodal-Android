package kr.ilf.kshoong.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ilf.kshoong.HealthConnectManager
import kr.ilf.kshoong.data.DailySwimData
import kr.ilf.kshoong.data.SwimDetailData
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SwimDataViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    enum class UiState {
        Loading, Complete, Scrolling
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

    private val _dailySwimDataFlow = MutableStateFlow<DailySwimData?>(null)
    val dailySwimDataFlow
        get() = _dailySwimDataFlow.asStateFlow()


    private val _swimDataFlow =
        MutableStateFlow<MutableMap<String, SwimDetailData>>(mutableMapOf())
    val swimDataFlow
        get() = _swimDataFlow.asStateFlow()

    private val _exerciseSessionsFlow =
        MutableStateFlow<MutableList<ExerciseSessionRecord>>(mutableListOf())
    val exerciseSessionsFlow
        get() = _exerciseSessionsFlow.asStateFlow()

    private val _currentSwimDataFlow2 =
        MutableStateFlow<MutableMap<Instant, DailySwimData>>(mutableMapOf())
    val currentSwimDataFlow2
        get() = _currentSwimDataFlow2.asStateFlow()

    private val _swimDataFlow2 =
        MutableStateFlow<MutableMap<Instant, DailySwimData>>(mutableMapOf())
    val swimDataFlow2
        get() = _swimDataFlow2.asStateFlow()

    val hasAllPermissions = mutableStateOf(false)

    init {
        viewModelScope.launch {
            hasAllPermissions.value = healthConnectManager.checkPermissions(healthPermissions)
        }
    }


    fun deleteCurrentSwimData() {
        _currentSwimDataFlow2.value.clear()
    }
    fun updateCurrentSwimData(date: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-E")
        val localDateTime = LocalDate.parse(date, formatter)
        val instant = localDateTime.atStartOfDay().toInstant(ZoneOffset.UTC)

        val data = _swimDataFlow2.value[instant]

        data?.let { _currentSwimDataFlow2.value.put(instant, it) }
    }

    fun initSwimData() {
        viewModelScope.launch {
            val hasPermissions = healthConnectManager.checkPermissions(healthPermissions)
            if (hasPermissions) {
                val exerciseSessions = readExerciseRecords()
                _exerciseSessionsFlow.value = exerciseSessions as MutableList<ExerciseSessionRecord>
                dailySwimmingData()

//                exerciseSessions.forEach { exerciseSessionRecord ->
//                    if (exerciseSessionRecord.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL) {
//                        val uid = exerciseSessionRecord.metadata.id
//                        val date =
//                            exerciseSessionRecord.startTime.plus(9L, ChronoUnit.HOURS).toString()
//
//                        _swimDataFlow.value[date] =
//                            healthConnectManager.readAssociatedSessionData(uid)
//                    }
//                }
            }

            uiState.value = UiState.Complete
        }
    }

    suspend fun readDailySwimData(date: String) {
        viewModelScope.launch {
            val hasPermissions = healthConnectManager.checkPermissions(healthPermissions)
            if (hasPermissions) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-E")
                val localDateTime = LocalDate.parse(date, formatter)
                val instant = localDateTime.atStartOfDay().toInstant(ZonedDateTime.now().offset)

//                _dailySwimDataFlow.value = healthConnectManager.readDailySwimData(instant)

            }
        }
    }

    private suspend fun dailySwimmingData() {
//        viewModelScope.launch {
        _swimDataFlow2.value =
            _exerciseSessionsFlow.value
                .filter { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL }
                .groupBy { it.startTime.truncatedTo(ChronoUnit.DAYS) }
                .mapValues { (date, records) ->
                    var totalDistance = 0.0
                    var totalCalories = 0.0
                    var totalActiveTime = Duration.ZERO

                    records.forEach { session ->
                        val dailySwimDataResponse =
                            healthConnectManager.readDailySwimData(
                                session.startTime,
                                session.endTime
                            )

                        totalDistance += dailySwimDataResponse.totalDistance?.inMeters ?: 0.0
                        totalCalories += dailySwimDataResponse.totalEnergyBurned?.inKilocalories
                            ?: 0.0
                        totalActiveTime += dailySwimDataResponse.totalActiveTime
                            ?: Duration.ZERO
                    }

                    DailySwimData(
                        date = date,
                        totalActiveTime = totalActiveTime,
                        totalDistance = Length.meters(totalDistance),
                        totalEnergyBurned = Energy.kilocalories(totalCalories),
                        sessionRecordList = records
                    )
                } as MutableMap<Instant, DailySwimData>
//        }
    }

    private suspend fun readExerciseRecords(): List<ExerciseSessionRecord> {
        val startOfDay = ZonedDateTime.now().minusDays(30L).truncatedTo(ChronoUnit.DAYS)
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