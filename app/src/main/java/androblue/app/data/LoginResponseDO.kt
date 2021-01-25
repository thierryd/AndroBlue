@file:Suppress("unused")

package androblue.app.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LoginResponseDO(
        val result: LoginResultDO?,
        val error: LoginErrorResultDO?
)

@JsonClass(generateAdapter = true)
class LoginResultDO(
        val accessToken: String?,
        val refreshToken: String?,
        val expireIn: Long
)

@JsonClass(generateAdapter = true)
class LoginErrorResultDO(
        val errorCode: String?,
        val errorDesc: String?
)