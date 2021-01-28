package androblue.app.repository.model

import androblue.app.data.VehicleStatusDO
import androblue.app.repository.ClimateStatus
import androblue.app.repository.LockStatus
import androblue.app.repository.db.VehicleEntity
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class VehicleModelAssembler @Inject constructor() {

    fun assembleWith(vehicleStatusDO: VehicleStatusDO): VehicleModel {
        return VehicleModel(lockStatus(vehicleStatusDO), climateStatus(vehicleStatusDO), batteryLevel(vehicleStatusDO), ZonedDateTime.now())
    }

    fun assembleWith(uid: Long?, vehicleModel: VehicleModel): VehicleEntity {
        return VehicleEntity(uid, vehicleModel.lockStatus, vehicleModel.climateStatus, vehicleModel.batteryLevel, vehicleModel.lastUpdated)
    }

    fun assembleWith(vehicleEntity: VehicleEntity): VehicleModel {
        return VehicleModel(vehicleEntity.lockStatus, vehicleEntity.climateStatus, vehicleEntity.batteryLevel, vehicleEntity.lastUpdated)
    }

    private fun lockStatus(status: VehicleStatusDO?): LockStatus {
        return when {
            status == null -> return LockStatus.UNKNOWN
            status.doorLock == null -> return LockStatus.UNKNOWN
            status.doorLock == true -> LockStatus.LOCKED
            else -> LockStatus.UNLOCKED
        }
    }

    private fun climateStatus(status: VehicleStatusDO?): ClimateStatus {
        return when {
            status == null -> return ClimateStatus.UNKNOWN
            status.airCtrlOn == null -> return ClimateStatus.UNKNOWN
            status.airCtrlOn == true -> ClimateStatus.ON
            else -> ClimateStatus.OFF
        }
    }

    private fun batteryLevel(status: VehicleStatusDO?): Int {
        return when (status) {
            null -> 0
            else -> status.evStatus?.batteryStatus ?: 0
        }
    }
}