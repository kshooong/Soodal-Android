package kr.ilf.kshoong.data

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import java.time.Duration
import java.time.Instant

data class DailySwimData(
    val date: Instant,
    val totalActiveTime: Duration? = null,
    val totalDistance: Length? = null,
    val totalEnergyBurned: Energy? = null,
    val minHeartRate: Long? = null,
    val maxHeartRate: Long? = null,
    val avgHeartRate: Long? = null,
    val sessionRecordList: List<ExerciseSessionRecord> = emptyList()
)
