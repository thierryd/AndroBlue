package androblue.common.log.db

import androblue.common.db.DateConverter
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [LoggerItemEntity::class], version = 2)
@TypeConverters(DateConverter::class, CategoryConverter::class)
abstract class LoggerDb : RoomDatabase() {

    abstract fun loggerDao(): LoggerDao
}