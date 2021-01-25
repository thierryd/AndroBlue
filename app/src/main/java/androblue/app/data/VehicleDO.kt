@file:Suppress("unused")

package androblue.app.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VehicleResultListDO(val result: VehicleListDO?)

@JsonClass(generateAdapter = true)
data class VehicleListDO(val vehicles: List<VehicleListItemDO>?)

@JsonClass(generateAdapter = true)
data class VehicleListItemDO(val vehicleId: String?)

@JsonClass(generateAdapter = true)
data class VehicleDO(val result: VehicleResultDO?)

@JsonClass(generateAdapter = true)
data class VehicleResultDO(val status: VehicleStatusDO?)

@JsonClass(generateAdapter = true)
data class VehicleStatusDO(val doorLock: Boolean?,
                           val airCtrlOn: Boolean?,
                           val evStatus: BatteryStatusDO?)

@JsonClass(generateAdapter = true)
data class BatteryStatusDO(val batteryStatus: Int?)

