package androblue.app

import androblue.app.di.component.AndroBlueApplicationComponent
import androblue.app.di.component.DaggerAndroBlueApplicationComponent
import androblue.app.service.PreferenceService
import androblue.common.ext.appVersionCode
import androblue.common.ext.appVersionName
import androblue.common.log.Logger
import androblue.common.log.LoggerInjectionHolder
import androblue.common.log.db.LoggerDatabaseHelper
import android.app.Application
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@Suppress("unused")
class AndroBlueApplication : Application(), HasAndroidInjector {

    @Inject lateinit var preferenceService: PreferenceService
    @Inject lateinit var dispatchingInjector: DispatchingAndroidInjector<Any>
    @Inject lateinit var loggerDatabaseHelper: LoggerDatabaseHelper

    private lateinit var androBlueApplicationComponent: AndroBlueApplicationComponent
    private lateinit var logger: Logger
    private val loggerInjectionHolder = LoggerInjectionHolder()
    private val lock = Object()

    @Volatile
    private var initialized: Boolean = false

    override fun onCreate() {
        super.onCreate()

        logger = Logger.Builder().build()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        ensureInitialized()
        return dispatchingInjector
    }

    private fun ensureInitialized() {
        if (!initialized) {
            synchronized(lock) {
                if (!initialized) {
                    initialize()
                    initialized = true
                }
            }
        }
    }

    @Suppress("ConstantConditionIf")
    private fun initialize() {
        Log.d("AndroBlueApplication", "AndroBlueApplication initialize START")

        try {
            AndroidThreeTen.init(this)
            ZonedDateTime.now(ZoneId.systemDefault()) //Create a ZonedDateTime to force the load of the TimeZone assets

            androBlueApplicationComponent = buildAndroBlueApplicationComponent()

            //Important: injection of LoggerInjectionHolder must be done BEFORE injecting AndroBlueApplication. Otherwise,
            //injected service will not log their creation
            androBlueApplicationComponent.inject(loggerInjectionHolder)

            Logger.initialize(loggerInjectionHolder)
            androBlueApplicationComponent.inject(this)

            Logger.Builder().build().apply {
                d("----------------------------------------------------")
                d("- AndroBlueApplication initialize() - ${ZonedDateTime.now()}")
                d("- DEBUG: ${BuildConfig.DEBUG}")
                d("- APPLICATION_ID: ${BuildConfig.APPLICATION_ID}")
                d("- BUILD_TYPE: ${BuildConfig.BUILD_TYPE}")
                d("- AppVersionCode: ${applicationContext.appVersionCode()}")
                d("- AppVersionName: ${applicationContext.appVersionName()}")
                d("- Configuration: ${applicationContext.resources.configuration}")
                d("----------------------------------------------------")
            }

            Thread.setDefaultUncaughtExceptionHandler(LoggingExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()!!, loggerDatabaseHelper))
        } catch (t: Throwable) {
            t.printStackTrace()
            Log.e("AndroBlueApplication", "Error in initialize()", t)
            throw t
        }
    }

    private fun buildAndroBlueApplicationComponent(): AndroBlueApplicationComponent {
        return DaggerAndroBlueApplicationComponent.builder()
                .application(this)
                .build()
    }

    fun getAndroBlueApplicationComponent(): AndroBlueApplicationComponent {
        ensureInitialized()
        return androBlueApplicationComponent
    }
}