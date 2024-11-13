package kr.ilf.kshoong.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.ilf.kshoong.MainActivity
import java.time.Instant

class HealthConnectUtil(val context: Context, val activity: ComponentActivity) {

    private val PERMISSIONS =
        setOf(
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        )

    private var healthConnectClient: HealthConnectClient? = null
    private val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()
    private val requestPermissions =
        activity.registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(PERMISSIONS)) {
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
            GlobalScope.launch { checkPermissionsAndRun(healthConnectClient!!) }
        }
    }

    private suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            // Permissions already granted; proceed with inserting or reading data
            MainActivity.realData = readExerciseSessions(Instant.now().minusSeconds(60 * 60 * 24 * 7), Instant.now())
            MainActivity.realData.forEach {
                Log.d("HealthConnectUtil out", it.toString())
            }

        } else {
            requestPermissions.launch(PERMISSIONS)
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
}