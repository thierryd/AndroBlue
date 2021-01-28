package androblue.app.repository.db

import androblue.app.repository.ClimateStatus
import androblue.app.repository.LockStatus
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter

@Dao
interface VehicleDao {

    @Query("SELECT * FROM VehicleEntity  limit 1")
    suspend fun load(): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(entity: VehicleEntity)
}

class LockStatusConverter {

    @TypeConverter
    fun toCategory(name: String): LockStatus {
        return try {
            LockStatus.valueOf(name)
        } catch (e: Exception) {
            LockStatus.UNKNOWN
        }
    }

    @TypeConverter
    fun toName(category: LockStatus): String {
        return category.name
    }
}

class ClimateStatusConverter {

    @TypeConverter
    fun toCategory(name: String): ClimateStatus {
        return try {
            ClimateStatus.valueOf(name)
        } catch (e: Exception) {
            ClimateStatus.UNKNOWN
        }
    }

    @TypeConverter
    fun toName(category: ClimateStatus): String {
        return category.name
    }
}