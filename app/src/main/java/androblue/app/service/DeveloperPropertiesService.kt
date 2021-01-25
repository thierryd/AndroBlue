package androblue.app.service

import androblue.app.BuildConfig
import android.content.Context
import java.io.FileNotFoundException
import java.util.Properties
import javax.inject.Inject

private const val DEV_PROPERTIES_FILENAME = "developer.properties"
private const val DEFAULT_USERNAME = "DEFAULT_USERNAME"
private const val DEFAULT_PASSWORD = "DEFAULT_PASSWORD"
private const val DEFAULT_PIN = "DEFAULT_PIN"

class DeveloperPropertiesService @Inject constructor(private val context: Context) {
    private val properties = Properties()

    init {
        loadProperties()
    }

    private fun loadProperties() {
        try {
            if (BuildConfig.DEBUG) {
                properties.load(context.assets.open(DEV_PROPERTIES_FILENAME))
            }
        } catch (e: FileNotFoundException) {
            //ignore - no properties file exists
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun developerOverrideUsername(): String {
        return if (isDebugAndInDevMode()) {
            getStringProperty(DEFAULT_USERNAME)
        } else ""
    }

    fun developerOverridePassword(): String {
        return if (isDebugAndInDevMode()) {
            getStringProperty(DEFAULT_PASSWORD)
        } else ""
    }

    fun developerOverridePin(): String {
        return if (isDebugAndInDevMode()) {
            getStringProperty(DEFAULT_PIN)
        } else ""
    }

    private fun isDebugAndInDevMode() = BuildConfig.DEBUG

    private fun getStringProperty(propertyKey: String): String {
        return try {
            properties.getProperty(propertyKey)
        } catch (e: Exception) {
            ""
        }
    }
}