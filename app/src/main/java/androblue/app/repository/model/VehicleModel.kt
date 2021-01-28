package androblue.app.repository.model

import androblue.app.repository.ClimateStatus
import androblue.app.repository.LockStatus
import androblue.app.repository.LockStatus.UNKNOWN
import androblue.app.service.toZonedDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

private val EMPTY_VEHICLE_MODEL = VehicleModel(UNKNOWN, ClimateStatus.UNKNOWN, 0, 0L.toZonedDateTime(ZoneId.systemDefault()))

class VehicleModel(val lockStatus: LockStatus,
                   val climateStatus: ClimateStatus,
                   val batteryLevel: Int,
                   val lastUpdated: ZonedDateTime)

fun emptyVehicleModel() = EMPTY_VEHICLE_MODEL