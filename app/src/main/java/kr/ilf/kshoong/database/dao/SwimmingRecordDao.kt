package kr.ilf.kshoong.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kr.ilf.kshoong.database.DatabaseConst
import kr.ilf.kshoong.database.entity.DailyRecord
import kr.ilf.kshoong.database.entity.DailyRecordWithDetailRecord
import kr.ilf.kshoong.database.entity.DetailRecord
import java.time.Instant

@Dao
interface SwimmingRecordDao {

    /*
    * DailyRecordWithDetailRecord
    */
    @Query("SELECT * FROM ${DatabaseConst.TB_DAILY_RECORD} ORDER BY date DESC")
    fun getAllRecords(): List<DailyRecordWithDetailRecord>

    @Transaction
    fun insertDailyRecordWithDetailRecord(
        dailyRecord: DailyRecord,
        detailRecord: List<DetailRecord>
    ) {
        insertDailyRecord(dailyRecord)
        insertDetailRecords(detailRecord)
    }

    /*
    * DailyRecord
    */
    @Query("SELECT * FROM ${DatabaseConst.TB_DAILY_RECORD} WHERE date = :date")
    fun getDailyRecordWithDetailRecord(date: Instant): DailyRecordWithDetailRecord

    @Query("SELECT * FROM ${DatabaseConst.TB_DAILY_RECORD} WHERE date = :date")
    fun getDailyRecord(date: Instant): DailyRecord?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDailyRecord(dailyRecord: DailyRecord)

    @Insert
    fun insertDailyRecords(dailyRecord: List<DailyRecord>)

    @Update
    fun updateDailyRecord(dailyRecord: DailyRecord)

    @Update
    fun updateDailyRecords(dailyRecord: List<DailyRecord>)

    @Delete
    fun deleteDailyRecord(dailyRecord: DailyRecord)

    @Delete
    fun deleteDailyRecords(dailyRecord: List<DailyRecord>)

    /*
    * DetailRecord
    */
    @Query("SELECT * FROM ${DatabaseConst.TB_DETAIL_RECORD} WHERE id = :id")
    fun getDetailRecord(id: String): DetailRecord

    @Query("SELECT * FROM ${DatabaseConst.TB_DETAIL_RECORD} WHERE date = :date")
    fun getDetailRecordsByDate(date: Instant): List<DetailRecord>

    @Insert
    fun insertDetailRecord(detailRecord: DetailRecord)

    @Insert
    fun insertDetailRecords(detailRecord: List<DetailRecord>)

    @Update
    fun updateDetailRecord(detailRecord: DetailRecord)

    @Update
    fun updateDetailRecords(detailRecord: List<DetailRecord>)

    @Delete
    fun deleteDetailRecord(detailRecord: DetailRecord)

    @Delete
    fun deleteDetailRecords(detailRecord: List<DetailRecord>)

    @Query("SELECT * FROM ${DatabaseConst.TB_DAILY_RECORD} daily WHERE date BETWEEN :start AND :end")
    fun findAllByMonth(start: Instant, end: Instant): List<DailyRecord>
}