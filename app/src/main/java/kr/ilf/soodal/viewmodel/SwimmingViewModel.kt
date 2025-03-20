package kr.ilf.soodal.viewmodel

import android.app.Application
import android.content.Context.MODE_PRIVATE
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
import kr.ilf.soodal.HealthConnectManager
import kr.ilf.soodal.database.database.SwimmingRecordDatabase
import kr.ilf.soodal.database.entity.DailyRecord
import kr.ilf.soodal.database.entity.DetailRecord
import kr.ilf.soodal.database.entity.DetailRecordWithHeartRateSample
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class SwimmingViewModel(
    private val application: Application,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    val uiState = mutableStateOf(UiState.LOADING)
    val popupUiState = mutableStateOf(PopupUiState.NONE)

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
            HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
            HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY
        )
    val hasAllPermissions = mutableStateOf(false)
    val permissionsContract = healthConnectManager.requestPermissionActivityContract()

    private val changeToken = mutableStateOf<String?>(null)

    private val _dailyRecords =
        MutableStateFlow<MutableMap<ZonedDateTime, DailyRecord>>(mutableMapOf())
    val dailyRecords
        get() = _dailyRecords.asStateFlow()

    private val _currentDetailRecords =
        MutableStateFlow<List<DetailRecordWithHeartRateSample>>(mutableListOf())
    val currentDetailRecords
        get() = _currentDetailRecords.asStateFlow()

    private val _currentModifyRecord =
        MutableStateFlow<DetailRecord?>(null)
    val currentModifyRecord
        get() = _currentModifyRecord.asStateFlow()

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
                val startOfDay = ZonedDateTime.now().minusDays(365L).truncatedTo(ChronoUnit.DAYS)
                val now = Instant.now()
                val timeRangeFilter = TimeRangeFilter.between(startOfDay.toInstant(), now)

                val exerciseSessions = healthConnectManager.readExerciseSessions(timeRangeFilter)

                exerciseSessions.filter { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL }
                    .forEach { session ->
                        val detailRecord =
                            healthConnectManager.readDetailRecord(
                                id = session.metadata.id,
                                session.startTime,
                                session.endTime
                            )

                        val heartRateRecords = healthConnectManager.readHeartRates(
                            session.metadata.id,
                            session.startTime,
                            session.endTime
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            dao?.insertDetailRecordWithHeartRateSamples(
                                detailRecord,
                                heartRateRecords
                            )
                        }
                    }

                nextChangeToken = requestChangeToken()
            } else {
                val changeList = mutableListOf<ExerciseSessionRecord>()
                val deletionList = mutableListOf<String>()
                val changeResponse = healthConnectManager.getChanges(changeToken.value!!)

                // 변경사항 삭제인지 추가,업데이트인지 분기
                do {
                    changeResponse.changes.forEach {
                        when (it) {
                            is UpsertionChange -> changeList.add(it.record as ExerciseSessionRecord)
                            is DeletionChange -> deletionList.add(it.recordId)
                        }
                    }
                } while (changeResponse.hasMore)

                // 추가,업데이트 데이터
                changeList.filter { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL }
                    .forEach { session ->
                        // 변경 레코드 상세 데이터 가져오기
                        val detailRecord =
                            healthConnectManager.readDetailRecord(
                                id = session.metadata.id,
                                session.startTime,
                                session.endTime
                            )

                        // 변경 레코드의 이전 데이터가 있는지 확인
                        val prevDetailRecord = withContext(Dispatchers.IO) {
                            dao?.findDetailRecordById(session.metadata.id)
                        }

                        withContext(Dispatchers.IO) {
                            val heartRateRecords = healthConnectManager.readHeartRates(
                                session.metadata.id,
                                session.startTime,
                                session.endTime
                            )

                            if (prevDetailRecord == null) {
                                dao?.insertDetailRecordWithHeartRateSamples(
                                    detailRecord,
                                    heartRateRecords
                                )
                            } else {
                                dao?.updateDetailRecordWithHeartRateSamples(
                                    detailRecord,
                                    heartRateRecords
                                )
                            }
                        }
                    }

                // 삭제된 레코드 제거
                deletionList.forEach {
                    withContext(Dispatchers.IO) {
                        dao?.deleteDetailRecordById(it)
                    }
                }

                nextChangeToken = changeResponse.nextChangesToken
            }

            val edit = application.getSharedPreferences("changeToken", MODE_PRIVATE).edit()
            edit.putString("changeToken", nextChangeToken)
            edit.apply()

            changeToken.value = nextChangeToken
            _dailyRecords.value = withContext(Dispatchers.IO) {
                val dailyRecordsMap = mutableMapOf<ZonedDateTime, DailyRecord>()

                val start =
                    Instant.now().minus(31, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
                val end = Instant.now()

                dao?.findDetailRecordsBetween(start, end)?.groupBy {
                    ZonedDateTime.ofInstant(it.startTime, ZoneId.systemDefault())
                        .truncatedTo(ChronoUnit.DAYS)
                }?.forEach { (date, records) ->
                    var totalActiveTime = Duration.ZERO
                    var totalDistance = 0
                    var totalEnergyBurned = 0.0
                    var totalCrawl = 0
                    var totalBackStroke = 0
                    var totalBreastStroke = 0
                    var totalButterfly = 0
                    var totalKickBoard = 0
                    var totalMixed = 0

                    records.forEach { record ->
                        totalActiveTime += record.activeTime?.let { Duration.parse(it) }
                            ?: Duration.ZERO
                        totalDistance += record.distance?.toInt() ?: 0
                        totalEnergyBurned += record.energyBurned?.toDouble() ?: 0.0
                        totalCrawl += record.crawl
                        totalBackStroke += record.backStroke
                        totalBreastStroke += record.breastStroke
                        totalButterfly += record.butterfly
                        totalKickBoard += record.kickBoard
                        totalMixed += record.mixed ?: 0
                    }
                    dailyRecordsMap[date] = DailyRecord(
                        date = date.toInstant(),
                        totalActiveTime.toString(),
                        totalDistance.toString(),
                        totalEnergyBurned.toString(),
                        totalCrawl,
                        totalBackStroke,
                        totalBreastStroke,
                        totalButterfly,
                        totalKickBoard,
                        totalMixed
                    )
                }
                dailyRecordsMap
            }

            onSyncComplete()
        }
    }

    fun updateDailyRecords(month: LocalDate) {
        viewModelScope.launch {
            _dailyRecords.value = withContext(Dispatchers.IO) {
                val dailyRecordsMap = mutableMapOf<ZonedDateTime, DailyRecord>()

                val start = month.minusMonths(1L).atStartOfDay().toInstant(ZoneOffset.UTC)
                val end =
                    month.withDayOfMonth(month.lengthOfMonth()).plusMonths(1L).atStartOfDay()
                        .toInstant(ZoneOffset.UTC)

                SwimmingRecordDatabase.getInstance(context = application)?.dailyRecordDao()
                    ?.findDetailRecordsBetween(start, end)?.groupBy {
                        ZonedDateTime.ofInstant(it.startTime, ZoneId.systemDefault())
                            .truncatedTo(ChronoUnit.DAYS)
                    }?.forEach { (date, records) ->
                        var totalActiveTime = Duration.ZERO
                        var totalDistance = 0
                        var totalEnergyBurned = 0.0
                        var totalCrawl = 0
                        var totalBackStroke = 0
                        var totalBreastStroke = 0
                        var totalButterfly = 0
                        var totalKickBoard = 0
                        var totalMixed = 0

                        records.forEach { record ->
                            totalActiveTime += record.activeTime?.let { Duration.parse(it) }
                                ?: Duration.ZERO
                            totalDistance += record.distance?.toInt() ?: 0
                            totalEnergyBurned += record.energyBurned?.toDouble() ?: 0.0
                            totalCrawl += record.crawl
                            totalBackStroke += record.backStroke
                            totalBreastStroke += record.breastStroke
                            totalButterfly += record.butterfly
                            totalKickBoard += record.kickBoard
                            totalMixed += record.mixed ?: 0
                        }
                        dailyRecordsMap[date] = DailyRecord(
                            date = date.toInstant(),
                            totalActiveTime.toString(),
                            totalDistance.toString(),
                            totalEnergyBurned.toString(),
                            totalCrawl,
                            totalBackStroke,
                            totalBreastStroke,
                            totalButterfly,
                            totalKickBoard,
                            totalMixed
                        )
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

                val result = dao?.findDetailRecordsWithHeartRateSamplesByDate(
                    date,
                    date.plus(1, ChronoUnit.DAYS)
                )
                result?.let {
                    _currentDetailRecords.value = it
                }

            }
        }
    }

    fun resetDetailRecord() {
        _currentDetailRecords.value = emptyList()
    }

    fun setModifyRecord(record: DetailRecord?) {
        _currentModifyRecord.value = record
    }

    fun modifyDetailRecord(record: DetailRecord) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                SwimmingRecordDatabase.getInstance(context = application)?.dailyRecordDao()
                    ?.updateDetailRecord(record)
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

enum class PopupUiState {
    NONE,
    MODIFY,
    WRITE,
    APP_FINISH
}