package androblue.app.service.okhttp

import androblue.app.BuildConfig
import androblue.common.log.Logger
import android.app.Application
import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

@Suppress("ObjectPropertyName")
private const val `5_MEGS_IN_BYTES` = 5L * 1024L * 1024L

class OkHttpProvider @Inject constructor(private val application: Application,
                                         private val authenticationInterceptor: AuthenticationInterceptor) {

    private var jsonDiskCache: Cache? = null

    private val logger = Logger.Builder().build()

    fun okHttpClient(): OkHttpClient {
        return newBuilder()
                .addInterceptor(authenticationInterceptor)
                .addInterceptor(HeaderInterceptor())
                .cache(jsonDiskCache())
                .build()
    }

    @Suppress("ControlFlowWithEmptyBody")
    private fun newBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
                .apply {
                    if (BuildConfig.DEBUG) {
                        //setupProxy(this, "192.168.86.28")
                    }
                    connectTimeout(60, SECONDS)
                    callTimeout(60, SECONDS)
                    writeTimeout(60, SECONDS)
                    readTimeout(60, SECONDS)
                    addInterceptor(HttpLoggingInterceptor { logger.d(it) }.apply {
                        level =
                            HttpLoggingInterceptor.Level.BODY
                    })
                }
    }

    private fun jsonDiskCache(): Cache {
        if (jsonDiskCache == null) {
            synchronized(this) {
                if (jsonDiskCache == null) {
                    val cacheSize = `5_MEGS_IN_BYTES`
                    val cacheDir = application.getDir("content_cache", Context.MODE_PRIVATE)
                    jsonDiskCache = Cache(cacheDir, cacheSize)
                }
            }
        }
        return jsonDiskCache!!
    }

    @Suppress("unused")
    private fun setupProxy(builder: OkHttpClient.Builder, proxyServer: String) {
        if (proxyServer.isNotBlank()) {
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyServer, 8888))
            builder.proxy(proxy)
        }
    }
}

private class HeaderInterceptor : Interceptor {

    private val offset = TimeUnit.MILLISECONDS.toHours(TimeZone.getDefault().rawOffset.toLong())

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        return chain.proceed(originalRequest.newBuilder()
                                     .header("from", "SPA")
                                     .header("language", "1")
                                     .header("Content-Type", "application/json")
                                     .header("offset", "$offset")
                                     .build())
    }
}