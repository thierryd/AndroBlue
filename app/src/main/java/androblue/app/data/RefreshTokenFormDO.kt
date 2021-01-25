@file:Suppress("unused")

package androblue.app.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RefreshTokenFormDO(val deviceID: String, val refresh: String)