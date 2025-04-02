package kr.ilf.soodal.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kr.ilf.soodal.database.DatabaseConst
import kr.ilf.soodal.database.entity.DetailRecord
import kr.ilf.soodal.database.entity.DetailRecordWithHR
import kr.ilf.soodal.database.entity.HeartRateSample
import java.time.Instant

@Dao
interface SwimmingRecordDao {


    @Query("SELECT * FROM ${DatabaseConst.TB_DETAIL_RECORD} WHERE startTime BETWEEN :start AND :end")
    @Transaction
    fun findDetailRecordsWithHeartRateSamplesByDate(
        start: Instant,
        end: Instant
    ): List<DetailRecordWithHR>

    @Transaction
    fun insertDetailRecordWithHeartRateSamples(
        detailRecord: DetailRecord,
        heartRateSamples: List<HeartRateSample>
    ) {
        insertDetailRecord(detailRecord)
        insertHeartRateSamples(heartRateSamples)
    }

    @Transaction
    fun updateDetailRecordWithHeartRateSamples(
        detailRecord: DetailRecord,
        heartRateSamples: List<HeartRateSample>
    ) {
        updateDetailRecord(detailRecord)
        updateHeartRateSamples(heartRateSamples)
    }

    /*
    * DetailRecord
    */
    @Query("SELECT * FROM ${DatabaseConst.TB_DETAIL_RECORD} WHERE id = :id")
    fun findDetailRecordById(id: String): DetailRecord

    @Query("SELECT * FROM ${DatabaseConst.TB_DETAIL_RECORD} daily WHERE startTime BETWEEN :start AND :end")
    fun findDetailRecordsBetween(start: Instant, end: Instant): List<DetailRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDetailRecord(detailRecord: DetailRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDetailRecords(detailRecord: List<DetailRecord>)

    @Update
    fun updateDetailRecord(detailRecord: DetailRecord)

    @Update
    fun updateDetailRecords(detailRecord: List<DetailRecord>)

    @Delete
    fun deleteDetailRecord(detailRecord: DetailRecord)

    @Query("DELETE FROM ${DatabaseConst.TB_DETAIL_RECORD} WHERE id = :id")
    fun deleteDetailRecordById(id: String)

    @Delete
    fun deleteDetailRecords(detailRecord: List<DetailRecord>)

    /*
    * HeartRateSample
    */
    @Query("SELECT * FROM ${DatabaseConst.TB_HEARTRATE_SAMPLE} WHERE time = :time")
    fun findHeartRateSampleByTime(time: Instant): HeartRateSample

    @Query("SELECT * FROM ${DatabaseConst.TB_HEARTRATE_SAMPLE} WHERE detailRecordId = :detailRecordId")
    fun findHeartRateSamplesById(detailRecordId: String): List<HeartRateSample>

    @Insert
    fun insertHeartRateSample(heartRateSample: HeartRateSample)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHeartRateSamples(heartRateSamples: List<HeartRateSample>)

    @Update
    fun updateHeartRateSample(heartRateSample: HeartRateSample)

    @Update
    fun updateHeartRateSamples(heartRateSamples: List<HeartRateSample>)

    @Delete
    fun deleteHeartRateSample(heartRateSample: HeartRateSample)

    @Delete
    fun deleteHeartRateSamples(heartRateSamples: List<HeartRateSample>)

}