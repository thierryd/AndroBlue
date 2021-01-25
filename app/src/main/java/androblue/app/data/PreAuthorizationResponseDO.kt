package androblue.app.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PreAuthorizationResponseDO(val result: PreAuthorizationResultDO?)

@JsonClass(generateAdapter = true)
data class PreAuthorizationResultDO(val pAuth: String?)