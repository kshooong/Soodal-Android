package kr.ilf.soodal.database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kr.ilf.soodal.database.DatabaseConst
import kr.ilf.soodal.database.converter.InstantConverter
import kr.ilf.soodal.database.dao.SwimmingRecordDao
import kr.ilf.soodal.database.entity.DetailRecord
import kr.ilf.soodal.database.entity.HeartRateSample

@Database(
    entities = [DetailRecord::class, HeartRateSample::class],
    version = DatabaseConst.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(InstantConverter::class)
abstract class SwimmingRecordDatabase : RoomDatabase() {

    abstract fun dailyRecordDao(): SwimmingRecordDao

    companion object {
        private var instance: SwimmingRecordDatabase? = null

        @Synchronized
        fun getInstance(context: Context): SwimmingRecordDatabase? {
            if (instance == null) {
                synchronized(SwimmingRecordDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, SwimmingRecordDatabase::class.java,
                        "DailyRecordDatabase"
                    ).addMigrations(MIGRATION_1_2).build()
                }
            }

            return instance
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 새로운 B 테이블 생성 (A에 대한 외래 키 및 date 컬럼 제거)
        database.execSQL("""
            CREATE TABLE ${DatabaseConst.TB_DETAIL_RECORD}_new (
                id TEXT PRIMARY KEY NOT NULL, 
                startTime INTEGER NOT NULL, 
                endTime INTEGER NOT NULL, 
                activeTime TEXT, 
                distance TEXT, 
                energyBurned TEXT, 
                minHeartRate INTEGER, 
                maxHeartRate INTEGER, 
                avgHeartRate INTEGER, 
                poolLength INTEGER DEFAULT 25 NOT NULL, 
                crawl INTEGER DEFAULT 0 NOT NULL, 
                backStroke INTEGER DEFAULT 0 NOT NULL, 
                breastStroke INTEGER DEFAULT 0 NOT NULL, 
                butterfly INTEGER DEFAULT 0 NOT NULL, 
                kickBoard INTEGER DEFAULT 0 NOT NULL, 
                mixed INTEGER DEFAULT 0 NOT NULL
            )
        """.trimIndent())

        // 기존 B 테이블의 데이터 복사 (A에 대한 외래 키 및 date 컬럼 제외)
        database.execSQL("""
            INSERT INTO ${DatabaseConst.TB_DETAIL_RECORD}_new (id, startTime, endTime, activeTime, distance, energyBurned, 
                               minHeartRate, maxHeartRate, avgHeartRate, poolLength, 
                               crawl, backStroke, breastStroke, butterfly, kickBoard, mixed)
            SELECT id, startTime, endTime, activeTime, distance, energyBurned, 
                   minHeartRate, maxHeartRate, avgHeartRate, poolLength, 
                   crawl, backStroke, breastStroke, butterfly, kickBoard, 
                   mixed
            FROM ${DatabaseConst.TB_DETAIL_RECORD}
        """.trimIndent())

        // 기존 B 테이블 삭제
        database.execSQL("DROP TABLE ${DatabaseConst.TB_DETAIL_RECORD}")

        // 새로운 B 테이블을 기존 이름으로 변경
        database.execSQL("ALTER TABLE ${DatabaseConst.TB_DETAIL_RECORD}_new RENAME TO ${DatabaseConst.TB_DETAIL_RECORD}")

        // A 테이블 삭제
        database.execSQL("DROP TABLE IF EXISTS ${DatabaseConst.TB_DAILY_RECORD}")
    }
}