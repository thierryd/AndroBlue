@file:Suppress("unused")

package androblue.app.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LoginDO(
        val loginId: String,
        val password: String
)