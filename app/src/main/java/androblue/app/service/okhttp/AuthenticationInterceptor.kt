package androblue.app.service.okhttp

import androblue.app.service.PreferenceService
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class AuthenticationInterceptor @Inject constructor(private val preferenceService: PreferenceService) : Interceptor {

    @Suppress("LiftReturnOrAssignment")
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalAccessToken = preferenceService.userAccessToken()

        if (originalAccessToken.isEmpty()) {
            //happens when the user is not logged in
            return chain.proceed(chain.request())
        } else {
            val originalRequest = chain.request()
            return chain.proceed(originalRequest.newAuthenticatedRequest(originalAccessToken))
        }
    }
}

private fun Request.newAuthenticatedRequest(token: String) = newBuilder()
        .addHeader("accesstoken", token)
        .build()