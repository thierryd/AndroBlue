@file:Suppress("unused")

package androblue.app.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponseDO(val result: LoginResultDO?,
                           val error: LoginErrorResultDO?
)

@JsonClass(generateAdapter = true)
data class LoginResultDO(val accessToken: String?,
                         val refreshToken: String?,
                         val expireIn: Long
)

@JsonClass(generateAdapter = true)
data class LoginErrorResultDO(val errorCode: String?,
                              val errorDesc: String?
)