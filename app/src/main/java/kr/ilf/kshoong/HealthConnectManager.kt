package kr.ilf.kshoong

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

class HealthConnectManager(val context: Context, val activity: ComponentActivity) {

    private val healthPermissions =
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

    private var healthConnectClient: HealthConnectClient? = null
    private val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()
    private val requestPermissions =
        activity.registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(healthPermissions)) {
                // Permissions successfully granted
                Log.d("HealthConnectUtil", "Permissions successfully granted")
            } else {
                // Lack of required permissions
                Log.d("HealthConnectUtil", "Permissions not granted")
            }
        }

    init {
        val isInit = initHealthConnectClient()
        if (isInit) {
            CoroutineScope(Dispatchers.Main).launch { checkPermissionsAndRun(healthConnectClient!!) }
        }
    }

    private suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(healthPermissions)) {
            // Permissions already granted; proceed with inserting or reading data
            val record =
                readExerciseSession(Instant.now().minusSeconds(60 * 60 * 24 * 7), Instant.now())

        } else {
            requestPermissions.launch(healthPermissions)
        }
    }


    private fun initHealthConnectClient(): Boolean {
        val providerPackageName = "google.android.apps.healthdata"
        val availabilityStatus = HealthConnectClient.getSdkStatus(context, providerPackageName)

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            return false // early return as there is no viable integration
        }

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            // Optionally redirect to package installer to find a provider, for example:
            val uriString =
                "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = Uri.parse(uriString)
                    putExtra("overlay", true)
                    putExtra("callerId", context.packageName)
                }
            )
            return false
        }

        healthConnectClient = HealthConnectClient.getOrCreate(context)

        return true
    }

    private suspend fun readExerciseSessions(
        start: Instant,
        end: Instant
    ): List<ExerciseSessionRecord> {
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient!!.readRecords(request)
        return response.records
    }

    private suspend fun readExerciseSession(
        start: Instant,
        end: Instant
    ): ExerciseSessionRecord {
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
//        val response = healthConnectClient!!.readRecords(request)
        val response = healthConnectClient!!.readRecord(
            ExerciseSessionRecord::class,
            "3822865f-a1a9-4e9b-a3a6-98da6c2a872d"
        )
        return response.record
    }

    private suspend fun readSpeedRecords(
        start: Instant,
        end: Instant
    ): List<SpeedRecord> {
        val request = ReadRecordsRequest(
            recordType = SpeedRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient!!.readRecords(request)
        return response.records
    }

    private suspend fun readHeartRateRecords(
        start: Instant,
        end: Instant
    ): List<HeartRateRecord> {
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient!!.readRecords(request)
        return response.records
    }
}