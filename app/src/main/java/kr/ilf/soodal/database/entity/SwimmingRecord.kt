package kr.ilf.soodal.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import kr.ilf.soodal.database.DatabaseConst
import java.time.Instant

// 테이블 x 그냥 데이터 클래스로 사용
data class DailyRecord(
    val date: Instant,
    val totalActiveTime: String? = null,
    val totalDistance: String? = null,
    val totalEnergyBurned: String? = null,
    val crawl: Int = 0,
    val backStroke: Int = 0,
    val breastStroke: Int = 0,
    val butterfly: Int = 0,
    val kickBoard: Int = 0,
    val mixed: Int = 0
)

@Entity(tableName = DatabaseConst.TB_DETAIL_RECORD)
data class DetailRecord(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val activeTime: String? = null,
    val distance: String? = null,
    val energyBurned: String? = null,
    val minHeartRate: Long? = null,
    val maxHeartRate: Long? = null,
    val avgHeartRate: Long? = null,
    val poolLength: Int = 25,
    val crawl: Int = 0,
    val backStroke: Int = 0,
    val breastStroke: Int = 0,
    val butterfly: Int = 0,
    val kickBoard: Int = 0,
    val mixed: Int = distance?.toInt() ?: 0
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

data class DetailRecordWithHeartRateSample(
    @Embedded val detailRecord: DetailRecord,

    @Relation(
        parentColumn = "id",
        entityColumn = "detailRecordId"
    )
    val heartRateSamples: List<HeartRateSample>
)