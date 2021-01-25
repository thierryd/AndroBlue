package androblue.app.json

import androblue.app.BuildConfig
import androblue.common.dagger.ScopeApplication
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import javax.inject.Inject

@ScopeApplication
class MoshiConverter @Inject constructor() {

    @Suppress("ConstantConditionIf")
    private val moshi: Moshi by lazy {
        Moshi.Builder()
                .apply {
                    if (BuildConfig.DEBUG) {
                        //Only add KotlinJsonAdapterFactory if we are debuggable
                        //The build of type "Release" use codegen
                        add(KotlinJsonAdapterFactory())
                    }
                }
                .build()
    }

    fun provideMoshi(): Moshi = moshi
}