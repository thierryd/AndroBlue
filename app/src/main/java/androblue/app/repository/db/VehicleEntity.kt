package androblue.app.repository.db

import androblue.app.repository.ClimateStatus
import androblue.app.repository.LockStatus
import androblue.app.service.toZonedDateTime
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

@Entity
data class VehicleEntity(@PrimaryKey(autoGenerate = true)
                         val uid: Long?,

                         val lockStatus: LockStatus,
                         val climateStatus: ClimateStatus,
                         val batteryLevel: Int,
                         val lastUpdated: ZonedDateTime) {

    fun clearData(): VehicleEntity {
        return this.copy(lockStatus = LockStatus.UNKNOWN, climateStatus = ClimateStatus.UNKNOWN, batteryLevel = 0, lastUpdated = 0L.toZonedDateTime(ZoneId.systemDefault()))
    }
}
