package kr.ilf.kshoong.viewmodel

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.changes.DeletionChange
import androidx.health.connect.client.changes.UpsertionChange
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.ilf.kshoong.HealthConnectManager
import kr.ilf.kshoong.database.database.SwimmingRecordDatabase
import kr.ilf.kshoong.database.entity.DailyRecord
import kr.ilf.kshoong.database.entity.DetailRecord
import kr.ilf.kshoong.database.entity.DetailRecordWithHeartRateSample
import kr.ilf.kshoong.database.entity.HeartRateSample
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class SwimmingViewModel(
    private val application: Application,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    val uiState = mutableStateOf(UiState.LOADING)

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

    private val changeToken = mutableStateOf<String?>(null)

    private val _dailyRecords = MutableStateFlow<MutableMap<Instant, DailyRecord>>(mutableMapOf())
    val dailyRecords
        get() = _dailyRecords.asStateFlow()

    private val _currentDetailRecord =
        MutableStateFlow<List<DetailRecordWithHeartRateSample?>>(mutableListOf())
    val currentDetailRecord
        get() = _currentDetailRecord.asStateFlow()

    init {
        viewModelScope.launch {
            hasAllPermissions.value = healthConnectManager.checkPermissions(healthPermissions)
        }
    }

    fun initSwimmingData(onSyncComplete: () -> Unit) {
        viewModelScope.launch {
            val dao = SwimmingRecordDatabase.getInstance(context = application)?.dailyRecordDao()
            var nextChangeToken: String? = null

            if (changeToken.value == null) {
                val startOfDay = ZonedDateTime.now().minusDays(30L).truncatedTo(ChronoUnit.DAYS)
                val now = Instant.now()
                val timeRangeFilter = TimeRangeFilter.between(startOfDay.toInstant(), now)

                val exerciseSessions = healthConnectManager.readExerciseSessions(timeRangeFilter)

                exerciseSessions.filter { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL }
                    .groupBy { it.startTime.truncatedTo(ChronoUnit.DAYS) }
                    .forEach { (date, records) ->
                        var totalDistance = 0
                        var totalCalories = 0.0
                        var totalActiveTime = Duration.ZERO
                        val detailRecords = mutableListOf<DetailRecord>()
                        var heartRateRecords = listOf<HeartRateSample>()

                        records.forEach { session ->
                            val detailRecordResponse =
                                healthConnectManager.readDetailRecord(
                                    id = session.metadata.id,
                                    date,
                                    session.startTime,
                                    session.endTime
                                )

                            totalDistance += detailRecordResponse.distance?.toDouble()?.roundToInt()
                                ?: 0
                            totalCalories += detailRecordResponse.energyBurned?.toDouble() ?: 0.0
                            totalActiveTime += Duration.parse(detailRecordResponse.activeTime)
                                ?: Duration.ZERO

                            detailRecords.add(detailRecordResponse)

                            heartRateRecords = healthConnectManager.readHeartRates(
                                session.metadata.id,
                                session.startTime,
                                session.endTime
                            )
                        }

                        val dailyRecord = DailyRecord(
                            date = date,
                            totalDistance = totalDistance.toString(),
                            totalActiveTime = totalActiveTime.toString(),
                            totalEnergyBurned = totalCalories.toString()
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            dao?.insertDailyRecordWithAll(
                                dailyRecord,
                                detailRecords,
                                heartRateRecords
                            )
                        }
                    }

                nextChangeToken = requestChangeToken()
            } else {
                val changeList = mutableListOf<ExerciseSessionRecord>()
                val deletionList = mutableListOf<String>()
                val changeResponse = healthConnectManager.getChanges(changeToken.value!!)

                do {
                    changeResponse.changes.forEach {
                        when (it) {
                            is UpsertionChange -> changeList.add(it.record as ExerciseSessionRecord)
                            is DeletionChange -> deletionList.add(it.recordId)
                        }
                    }
                } while (changeResponse.hasMore)

                changeList.filter { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL }
                    .groupBy { it.startTime.truncatedTo(ChronoUnit.DAYS) }
                    .forEach { (date, records) ->
                        // 변경 레코드의 날짜에 데이터가 있는지 확인
                        val dailyRecord = withContext(Dispatchers.IO) {
                            dao?.getDailyRecord(date)
                        }

                        // 데이터 초기값 데이터 있으면 가져오고 없으면 0
                        var totalDistance =
                            dailyRecord?.totalDistance?.toDouble()?.roundToInt() ?: 0
                        var totalCalories = dailyRecord?.totalEnergyBurned?.toDouble() ?: 0.0
                        var totalActiveTime =
                            dailyRecord?.totalActiveTime?.let { Duration.parse(it) }
                                ?: Duration.ZERO
                        val insertDetailRecords = mutableListOf<DetailRecord>()
                        val updateDetailRecords = mutableListOf<DetailRecord>()
                        val insertHeartRateRecords = mutableListOf<HeartRateSample>()
                        val updateHeartRateRecords = mutableListOf<HeartRateSample>()

                        records.forEach { session ->
                            // 변경 레코드 상세 데이터 가져오기
                            val detailRecordResponse =
                                healthConnectManager.readDetailRecord(
                                    id = session.metadata.id,
                                    date,
                                    session.startTime,
                                    session.endTime
                                )

                            // 변경 레코드의 이전 데이터가 있는지 확인
                            val detailRecord = withContext(Dispatchers.IO) {
                                dao?.findDetailRecordById(session.metadata.id)
                            }

                            if (detailRecord == null) {
                                // 이전 데이터 없다면 데이터 더하기, insertDetailRecords 에 추가
                                totalDistance += detailRecordResponse.distance?.toDouble()
                                    ?.roundToInt() ?: 0
                                totalCalories += detailRecordResponse.energyBurned?.toDouble()
                                    ?: 0.0
                                totalActiveTime += Duration.parse(detailRecordResponse.activeTime)
                                    ?: Duration.ZERO

                                insertDetailRecords.add(detailRecordResponse)
                                insertHeartRateRecords.addAll(healthConnectManager.readHeartRates(
                                    session.metadata.id,
                                    session.startTime,
                                    session.endTime
                                ))
                            } else {
                                // 이전 데이터 있다면 이전 데이터 빼기 후 현재 데이터 더하기, updateDetailRecords 에 추가
                                totalDistance -= detailRecord.distance?.toDouble()?.roundToInt()
                                    ?: 0
                                totalCalories -= detailRecord.energyBurned?.toDouble() ?: 0.0
                                totalActiveTime -= Duration.parse(detailRecord.activeTime)
                                    ?: Duration.ZERO

                                totalDistance += detailRecordResponse.distance?.toDouble()
                                    ?.roundToInt() ?: 0
                                totalCalories += detailRecordResponse.energyBurned?.toDouble()
                                    ?: 0.0
                                totalActiveTime += Duration.parse(detailRecordResponse.activeTime)
                                    ?: Duration.ZERO

                                updateDetailRecords.add(detailRecordResponse)
                                updateHeartRateRecords.addAll(healthConnectManager.readHeartRates(
                                    session.metadata.id,
                                    session.startTime,
                                    session.endTime
                                ))
                            }

                        }

                        val newDailyRecord = DailyRecord(
                            date = date,
                            totalDistance = totalDistance.toString(),
                            totalActiveTime = totalActiveTime.toString(),
                            totalEnergyBurned = totalCalories.toString()
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            // dailyRecord 없다면 insert, 있으면 update
                            if (dailyRecord == null) {
                                // dailyRecord 없다면 detail도 없을 테니 detail도 insert로직만 호출
                                dao?.insertDailyRecordWithAll(
                                    newDailyRecord,
                                    insertDetailRecords,
                                    insertHeartRateRecords
                                )
                            } else {
                                dao?.updateDailyRecord(newDailyRecord)
                                if (updateDetailRecords.isNotEmpty())
                                    dao?.updateDetailRecords(updateDetailRecords)
                                    dao?.updateHeartRateSamples(updateHeartRateRecords)
                                if (insertDetailRecords.isNotEmpty()) {
                                    dao?.insertDetailRecords(insertDetailRecords)
                                    dao?.insertHeartRateSamples(insertHeartRateRecords)
                                }
                            }
                        }
                    }

                nextChangeToken = changeResponse.nextChangesToken
            }

            val edit = application.getSharedPreferences("changeToken", MODE_PRIVATE).edit()
            edit.putString("changeToken", nextChangeToken)
            edit.apply()

            changeToken.value = nextChangeToken
            _dailyRecords.value = withContext(Dispatchers.IO) {
                val dailyRecordsMap = mutableMapOf<Instant, DailyRecord>()

                val start = Instant.now().minus(31, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
                val end = Instant.now()

                dao?.findAllByMonth(start, end)?.forEach { record ->
                    dailyRecordsMap[record.date] = record
                }

                dailyRecordsMap
            }

            onSyncComplete()
        }
    }

    fun updateDailyRecords(month: LocalDate) {
        viewModelScope.launch {
            _dailyRecords.value = withContext(Dispatchers.IO) {
                val dailyRecordsMap = mutableMapOf<Instant, DailyRecord>()

                val start = month.minusMonths(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
                val end = month.withDayOfMonth(month.lengthOfMonth()).plusMonths(1L).atStartOfDay()
                    .toInstant(ZoneOffset.UTC)

                SwimmingRecordDatabase.getInstance(context = application)?.dailyRecordDao()
                    ?.findAllByMonth(start, end)?.forEach { record ->
                        dailyRecordsMap[record.date] = record
                    }

                dailyRecordsMap
            }
        }
    }

    fun findDetailRecord(date: Instant) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dao = SwimmingRecordDatabase.getInstance(context = application)
                    ?.dailyRecordDao()

                val result = dao?.findDetailRecordsWithHeartRateSamplesByDate(date)
                result?.let {
                    _currentDetailRecord.value = it
                }

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

    private suspend fun requestChangeToken(): String? {
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

enum class UiState {
    LOADING,
    COMPLETE,
    SCROLLING
}