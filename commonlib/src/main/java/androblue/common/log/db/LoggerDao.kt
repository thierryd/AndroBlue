package androblue.common.log.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.threeten.bp.ZonedDateTime

@Dao
interface LoggerDao {

    @Query("SELECT * FROM LoggerItemEntity ORDER BY date ASC")
    suspend fun loadAll(): Array<LoggerItemEntity>

    @Insert
    fun save(entities: Array<LoggerItemEntity>)

    @Query("DELETE FROM LoggerItemEntity WHERE date <= :thresholdDate")
    fun cleanup(thresholdDate: ZonedDateTime)

    @Query("SELECT COUNT(*) FROM LoggerItemEntity")
    suspend fun count(): Int
}
