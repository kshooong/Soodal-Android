package kr.ilf.soodal.util

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kr.ilf.soodal.R

class NotificationUtil private constructor(private val application: Application) {

    /**
     * NotificationChannel 생성
     *
     * @param channelId 시스템에서 각 알림 채널을 고유하게 식별하는 문자
     * @param channelName 사용자에게 보여지는 알림 채널의 이름
     * @param channelImportance 채널 중요도(NotificationManager.IMPORTANCE_*)
     * @param channelDescription 사용자에서 보여지는 알림 설명
     */
    fun createNotificationChannel(
        channelId: String,
        channelName: CharSequence,
        channelImportance: Int,
        channelDescription: String
    ) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            channelImportance
        ).apply {
            description = channelDescription
        }

        val manager =
            application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**
     * 알림 권환 확인 후 알림 전송
     *
     * @param channelId 알림 채널 ID
     * @param title 알림 제목
     * @param message 알림 내용
     * @param pendingIntent 알림 클릭 시 전송될 인텐트
     * @param priority 중요도(NotificationCompat.PRIORITY_*)
     * @return 알림 전송 여부 Boolean
     */
    fun sendNotification(
        channelId: String,
        title: String,
        message: String,
        pendingIntent: PendingIntent,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ): Boolean {
        // 알림 표시 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    application,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false// 알림 보내기 중단
            }
        }

        val builder =
            NotificationCompat.Builder(application, channelId)
                .setSmallIcon(R.drawable.ic_soodal)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority)
                .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(application)) {
            notify(
                NOTIFICATION_ID_NEW_SESSIONS,
                builder.build()
            )
        }

        return true
    }

    companion object {
        @Volatile
        private var INSTANCE: NotificationUtil? = null

        fun getInstance(application: Application): NotificationUtil {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationUtil(application).also { INSTANCE = it }
            }
        }

        // 알림 채널 ID
        const val CHANNEL_ID_NEW_SESSIONS = "channel_new_sessions"

        // 알림 ID
        const val NOTIFICATION_ID_NEW_SESSIONS = 1002
    }
}