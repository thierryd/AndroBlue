package androblue.app.service.okhttp

import androblue.app.json.MoshiConverter
import javax.inject.Inject

class ApiProvider @Inject constructor(private val okHttpProvider: OkHttpProvider,
                                      private val moshiConverter: MoshiConverter) {

    companion object {
        const val API_ENDPOINT = "https://mybluelink.ca"
    }

    fun apiClient(): ApiClient {
        return ApiClient(API_ENDPOINT, okHttpProvider.okHttpClient(), moshiConverter.provideMoshi())
    }
}