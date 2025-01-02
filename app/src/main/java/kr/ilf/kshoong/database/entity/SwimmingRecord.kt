package kr.ilf.kshoong.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import kr.ilf.kshoong.database.DatabaseConst
import java.time.Instant

@Entity(tableName = DatabaseConst.TB_DAILY_RECORD)
data class DailyRecord(
    @PrimaryKey(autoGenerate = false)
    val date: Instant,
    val totalActiveTime: String? = null,
    val totalDistance: String? = null,
    val totalEnergyBurned: String? = null,
)

@Entity(
    tableName = DatabaseConst.TB_DETAIL_RECORD,
    foreignKeys = [ForeignKey(
        entity = DailyRecord::class,
        parentColumns = arrayOf("date"),
        childColumns = arrayOf("date"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class DetailRecord(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val date: Instant,
    val startTime: Instant,
    val endTime: Instant,
    val activeTime: String? = null,
    val distance: String? = null,
    val energyBurned: String? = null,
    val minHeartRate: Long? = null,
    val maxHeartRate: Long? = null,
    val avgHeartRate: Long? = null,
)

@Entity(
    tableName = DatabaseConst.TB_HEARTRATE_SAMPLE,
    foreignKeys = [ForeignKey(
        entity = DetailRecord::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("detailRecordId"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class HeartRateSample(
    @PrimaryKey(autoGenerate = false)
    val time: Instant,
    val detailRecordId: String,
    val beatsPerMinute: Int
)

data class DailyRecordWithAll(
    @Embedded val dailyRecord: DailyRecord,

    @Relation(
        parentColumn = "date",
        entityColumn = "date",
        entity = DetailRecord::class
    )
    val detailRecords: List<DetailRecordWithHeartRateSample>
)

data class DailyRecordWithDetailRecord(
    @Embedded val dailyRecord: DailyRecord,

    @Relation(
        parentColumn = "date",
        entityColumn = "date"
    )
    val detailRecords: List<DetailRecord>
)

data class DetailRecordWithHeartRateSample(
    @Embedded val detailRecord: DetailRecord,

    @Relation(
        parentColumn = "id",
        entityColumn = "detailRecordId"
    )
    val heartRateSamples: List<HeartRateSample>
)