package kr.ilf.soodal.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.ilf.soodal.MainActivity
import kr.ilf.soodal.R
import kr.ilf.soodal.SharedPrefConst.AppSync
import kr.ilf.soodal.SharedPrefConst.LastCheckTime
import kr.ilf.soodal.SoodalApplication
import kr.ilf.soodal.util.HealthConnectManager
import kr.ilf.soodal.util.NotificationUtil
import java.time.Instant
import kotlin.math.max

class NewSessionNotificationWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val healthConnectManager = HealthConnectManager(context)
    private val settingsRepository =
        (context.applicationContext as SoodalApplication).settingsRepository

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val notificationEnabled = settingsRepository.getNotificationsEnabledOnce()
        val newSessionNotificationEnabled =
            settingsRepository.getNewSessionNotificationsEnabledOnce()
        val healthConnectAvailability by healthConnectManager.availability

        if (!(notificationEnabled && newSessionNotificationEnabled && healthConnectAvailability)) {
            return@withContext Result.success()
        }

        try {
            // 헬스 커넥트 읽기 권한 체크
            val requiredPermissions =
                setOf(HealthPermission.getReadPermission(ExerciseSessionRecord::class))
            val grantedPermissions = healthConnectManager.checkPermissions(requiredPermissions)

            if (!grantedPermissions) {
                return@withContext Result.failure()
            }

            // 조회 시간 확인
            val prefsAppSync = context.getSharedPreferences(AppSync.NAME, MODE_PRIVATE)
            val prefsLastCheckTime = context.getSharedPreferences(LastCheckTime.NAME, MODE_PRIVATE)

            val lastAppSyncTimeMills = prefsAppSync.getLong(AppSync.KEY_LAST_SYNC_TIME, 0L)
            val lastCheckTimeMills =
                prefsLastCheckTime.getLong(LastCheckTime.KEY_LAST_CHECK_TIME, 0L)

            if (lastAppSyncTimeMills == 0L) {
                // 최초 싱크를 하지 않은 경우 종료
                return@withContext Result.success()
            }

            val startTimeMills = max(lastAppSyncTimeMills, lastCheckTimeMills)
            val startTime = Instant.ofEpochMilli(startTimeMills)
            val endTime = Instant.now()

            // 새로운 수영 기록 조회
            val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            val newExerciseSessions = healthConnectManager.readExerciseSessions(timeRangeFilter)
            val hasNewSwimmingExerciseSession =
                newExerciseSessions.any { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL }

            if (!hasNewSwimmingExerciseSession) {
                // 새로운 수영 기록이 없는 경우 종료
                return@withContext Result.success()
            }

            // 알림 전송 로직
            val isSent = sendNotification()
            if (!isSent) {
                return@withContext Result.failure()
            }

            // 조회 시간 저장
            prefsLastCheckTime.edit(true) {
                putLong(LastCheckTime.KEY_LAST_CHECK_TIME, endTime.toEpochMilli())
                // edit 블럭이 종료된 후 자동 실행(commit: Boolean 파라미터를 통해 apply, commit 선택 가능. 기본값 apply)
            }

            return@withContext Result.success()
        } catch (e: Exception) {
            e.printStackTrace()

            return@withContext Result.failure()
        }
    }

    /**
     * 새 기록 알림 전송
     *
     * @return 알림 전송 여부 Boolean
     */
    private fun sendNotification(): Boolean {
        // 채널 생성이 안된 경우를 대비해 한번 더 생성(있다면 중복 안됨)
        NotificationUtil.createNotificationChannel(
            context,
            NotificationUtil.CHANNEL_ID_NEW_SESSIONS,
            context.resources.getString(R.string.channel_name_new_sessions),
            NotificationManager.IMPORTANCE_DEFAULT,
            context.resources.getString(R.string.channel_description_new_sessions)
        )

        // 알림에 필요한 데이터 생성
        val title = context.resources.getString(R.string.notification_title_new_sessions)
        val message = context.resources.getString(R.string.notification_message_new_sessions)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0, // requestCode, 여러 PendingIntent를 구분할 때 사용. 0으로 하면 이전 인텐트가 있다면 교체됨.
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // 알림 전송 및 결과 반환
        return NotificationUtil.sendNotification(
            context,
            NotificationUtil.CHANNEL_ID_NEW_SESSIONS,
            title,
            message,
            pendingIntent,
            NotificationCompat.PRIORITY_DEFAULT
        )
    }

    companion object {
        const val NAME = "NewSessionNotificationWork"
    }
}