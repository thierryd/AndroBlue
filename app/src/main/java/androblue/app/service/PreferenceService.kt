package androblue.app.service

import androblue.app.home.HomeActivityController.Companion.VEHICLE_CLIMATE_DEFAULT_TEMPERATURE
import androblue.common.dagger.ScopeApplication
import androblue.common.preference.FlowPreferences
import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

private const val PREF_ACCOUNT_USERNAME = "PREF_ACCOUNT_USERNAME"
private const val PREF_ACCOUNT_PASSWORD = "PREF_ACCOUNT_PASSWORD"
private const val PREF_ACCOUNT_PIN = "PREF_ACCOUNT_PIN"
private const val PREF_MAIN_VEHICLE_ID = "PREF_MAIN_VEHICLE_ID"
private const val PREF_USER_ACCESS_TOKEN = "PREF_USER_ACCESS_TOKEN"
private const val PREF_USER_TOKEN_EXPIRE_AT = "PREF_USER_TOKEN_EXPIRE_AT"
private const val PREF_CLIMATE_TEMP = "PREF_CLIMATE_TEMP"
private const val PREF_SECURED_KEYS_MIGRATION_DONE = "PREF_SECURED_KEYS_MIGRATION_DONE"

@Suppress("LiftReturnOrAssignment")
@ScopeApplication
open class PreferenceService @Inject constructor(application: Application) {

    companion object {
        @VisibleForTesting const val PREF_FILENAME = "androblue_prefs"
        @VisibleForTesting const val SECURED_PREF_FILENAME = "androblue_securedprefs"
    }

    private val prefs = application.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE)
    private val flowPreferences = FlowPreferences.create(prefs)

    private var masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val securedPref = EncryptedSharedPreferences.create(SECURED_PREF_FILENAME, masterKeyAlias, application, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

    init {
        if (!prefs.getBoolean(PREF_SECURED_KEYS_MIGRATION_DONE, false)) {
            securedPref.edit { putString(PREF_ACCOUNT_USERNAME, prefs.getString(PREF_ACCOUNT_USERNAME, "")) }
            securedPref.edit { putString(PREF_ACCOUNT_PASSWORD, prefs.getString(PREF_ACCOUNT_PASSWORD, "")) }
            securedPref.edit { putString(PREF_ACCOUNT_PIN, prefs.getString(PREF_ACCOUNT_PIN, "")) }
            securedPref.edit { putString(PREF_MAIN_VEHICLE_ID, prefs.getString(PREF_ACCOUNT_PIN, "")) }

            prefs.edit { remove(PREF_ACCOUNT_USERNAME) }
            prefs.edit { remove(PREF_ACCOUNT_PASSWORD) }
            prefs.edit { remove(PREF_ACCOUNT_PIN) }
            prefs.edit { remove(PREF_ACCOUNT_PIN) }
            prefs.edit { putBoolean(PREF_SECURED_KEYS_MIGRATION_DONE, true) }
        }
    }

    fun userAccessToken(): String = prefs.getString(PREF_USER_ACCESS_TOKEN, null) ?: ""

    fun userAccessToken(token: String) {
        prefs.edit { putString(PREF_USER_ACCESS_TOKEN, token) }
    }

    fun userAccessTokenExpireAt(tokenExpireAtSystemMillis: Long) {
        prefs.edit { putLong(PREF_USER_TOKEN_EXPIRE_AT, tokenExpireAtSystemMillis) }
    }

    fun userAccessTokenExpireAt() = prefs.getLong(PREF_USER_TOKEN_EXPIRE_AT, -1L)

    /////////////////////////
    fun username() = securedPref.getString(PREF_ACCOUNT_USERNAME, "") ?: ""
    fun username(username: String) {
        securedPref.edit { putString(PREF_ACCOUNT_USERNAME, username) }
    }

    fun password() = securedPref.getString(PREF_ACCOUNT_PASSWORD, "") ?: ""
    fun password(password: String) {
        securedPref.edit { putString(PREF_ACCOUNT_PASSWORD, password) }
    }

    fun pin() = securedPref.getString(PREF_ACCOUNT_PIN, "") ?: ""
    fun pin(pin: String) {
        securedPref.edit { putString(PREF_ACCOUNT_PIN, pin) }
    }

    fun mainVehicleId() = securedPref.getString(PREF_MAIN_VEHICLE_ID, "") ?: ""
    fun mainVehicleId(mainVehicleId: String) {
        securedPref.edit { putString(PREF_MAIN_VEHICLE_ID, mainVehicleId) }
    }

    /////////////////////////
    fun climateTemperature() = prefs.getFloat(PREF_CLIMATE_TEMP, VEHICLE_CLIMATE_DEFAULT_TEMPERATURE)
    fun climateTemperatureFlow() = flowPreferences.getFloatFlow(PREF_CLIMATE_TEMP, VEHICLE_CLIMATE_DEFAULT_TEMPERATURE)
    fun climateTemperature(temperature: Float) {
        prefs.edit { putFloat(PREF_CLIMATE_TEMP, temperature) }
    }
}

fun ZonedDateTime.toSystemMillis() = toInstant().toEpochMilli()
fun Long.toZonedDateTime(zoneId: ZoneId): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)

