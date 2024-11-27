package kr.ilf.kshoong.database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.ilf.kshoong.database.DatabaseConst
import kr.ilf.kshoong.database.converter.InstantConverter
import kr.ilf.kshoong.database.dao.SwimmingRecordDao
import kr.ilf.kshoong.database.entity.DailyRecord
import kr.ilf.kshoong.database.entity.DetailRecord

@Database(
    entities = [DailyRecord::class, DetailRecord::class, ],
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
                    ).build()
                }
            }

            return instance
        }
    }
}