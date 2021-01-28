package androblue.app.repository.db

import androblue.common.db.DateConverter
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [VehicleEntity::class], version = 1)
@TypeConverters(DateConverter::class, LockStatusConverter::class, ClimateStatusConverter::class)
abstract class VehicleDb : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
}