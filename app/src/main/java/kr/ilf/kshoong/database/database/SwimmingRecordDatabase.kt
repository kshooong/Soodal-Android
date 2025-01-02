package kr.ilf.kshoong.database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kr.ilf.kshoong.database.DatabaseConst
import kr.ilf.kshoong.database.converter.InstantConverter
import kr.ilf.kshoong.database.dao.SwimmingRecordDao
import kr.ilf.kshoong.database.entity.DailyRecord
import kr.ilf.kshoong.database.entity.DetailRecord
import kr.ilf.kshoong.database.entity.HeartRateSample

@Database(
    entities = [DailyRecord::class, DetailRecord::class, HeartRateSample::class],
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
                    ).addMigrations(migration_1_2).build()
                }
            }

            return instance
        }
    }
}


val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE ${DatabaseConst.TB_HEARTRATE_SAMPLE} (
                time INTEGER NOT NULL PRIMARY KEY, 
                detailRecordId TEXT NOT NULL, 
                beatsPerMinute INTEGER NOT NULL,
                FOREIGN KEY(detailRecordId) REFERENCES ${DatabaseConst.TB_DETAIL_RECORD}(id) ON DELETE CASCADE ON UPDATE CASCADE
            )
        """.trimIndent()
        )
    }
}