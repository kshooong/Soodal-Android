package kr.ilf.soodal.util

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.util.fastRoundToInt
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ChangesResponse
import androidx.health.connect.client.time.TimeRangeFilter
import kr.ilf.soodal.database.entity.DetailRecord
import kr.ilf.soodal.database.entity.HeartRateSample
import java.time.Instant

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    var availability = mutableStateOf(false)
        private set

    init {
        availability.value = checkHealthConnectClient()
    }

    suspend fun requestChangeToken(): String {
        val request = ChangesTokenRequest(
            setOf(
                ExerciseSessionRecord::class,
            )
        )

        return healthConnectClient.getChangesToken(request)
    }

    suspend fun getChanges(token: String): ChangesResponse {
        return healthConnectClient.getChanges(token)
    }

    suspend fun readExerciseSessions(
        timeRangeFilter: TimeRangeFilter
    ): List<ExerciseSessionRecord> {
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = timeRangeFilter
        )

        return healthConnectClient.readRecords(request).records
    }

    suspend fun readDetailRecord(
        id: String,
        startTime: Instant,
        endTime: Instant
    ): DetailRecord {
        val timeRangeFilter = TimeRangeFilter.between(
            startTime = startTime,
            endTime = endTime
        )
        val aggregateDataTypes = setOf(
            ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
            DistanceRecord.DISTANCE_TOTAL,
            TotalCaloriesBurnedRecord.ENERGY_TOTAL,
            HeartRateRecord.BPM_AVG,
            HeartRateRecord.BPM_MAX,
            HeartRateRecord.BPM_MIN,
        )

        val aggregateRequest = AggregateRequest(
            metrics = aggregateDataTypes,
            timeRangeFilter = timeRangeFilter,
        )

        val aggregateData = healthConnectClient.aggregate(aggregateRequest)

        return DetailRecord(
            id = id,
            startTime = startTime,
            endTime = endTime,
            activeTime = aggregateData[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]?.toString(),
            distance = aggregateData[DistanceRecord.DISTANCE_TOTAL]?.inMeters?.fastRoundToInt()
                ?.toString() ?: "0",
            energyBurned = aggregateData[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toString(),
            minHeartRate = aggregateData[HeartRateRecord.BPM_MIN],
            maxHeartRate = aggregateData[HeartRateRecord.BPM_MAX],
            avgHeartRate = aggregateData[HeartRateRecord.BPM_AVG]
        )
    }

    suspend fun readHeartRates(
        detailRecordId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HeartRateSample> {
        val timeRangeFilter = TimeRangeFilter.between(
            startTime = startTime,
            endTime = endTime
        )

        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = timeRangeFilter
        )

        val heartRateSamples = mutableListOf<HeartRateSample>()

        val heartRateRecords = healthConnectClient.readRecords(request).records
        if (heartRateRecords.isNotEmpty()) {
            heartRateRecords[0].samples.forEach { sample ->
                heartRateSamples.add(
                    HeartRateSample(
                        sample.time,
                        detailRecordId,
                        sample.beatsPerMinute.toInt()
                    )
                )
            }
        }

        return heartRateSamples
    }

    suspend fun checkPermissions(permissions: Set<String>): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        // 33이하에서? 실제 권한을 동의해서 getGrantedPermissions에서 PERMISSION_READ_HEALTH_DATA_HISTORY 권한은 반환되지않고 필수 권한도 아니므로 빼고 확인
        return granted.containsAll(permissions.minus(HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY))
    }


    fun requestPermissionActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    private fun checkHealthConnectClient(): Boolean {
        val availabilityStatus = HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE_NAME)

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            return false
        }

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            return false
        }

        return true
    }

    private suspend inline fun <reified T : Record> readRecords(
        timeRangeFilter: TimeRangeFilter,
        dataOriginFilter: Set<DataOrigin>
    ): List<T> {
        val request = ReadRecordsRequest(
            recordType = T::class,
            timeRangeFilter = timeRangeFilter,
            dataOriginFilter = dataOriginFilter
        )

        return healthConnectClient.readRecords(request).records
    }

    companion object {
        const val PROVIDER_PACKAGE_NAME = "com.google.android.apps.healthdata"
    }
}