@file:Suppress("unused")

package androblue.app.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VehicleLockResponseDO(val error: ErrorResponseDO?)

@JsonClass(generateAdapter = true)
class ErrorResponseDO