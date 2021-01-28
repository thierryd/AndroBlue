package androblue.app.service

import androblue.common.dagger.ScopeApplication
import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
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

@Suppress("LiftReturnOrAssignment")
@ScopeApplication
open class PreferenceService @Inject constructor(application: Application) {

    companion object {
        @VisibleForTesting const val PREF_FILENAME = "androblue_prefs"
    }

    private val prefs = application.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE)

    fun userAccessToken(): String = prefs.getString(PREF_USER_ACCESS_TOKEN, null) ?: ""

    fun userAccessToken(token: String) {
        prefs.edit { putString(PREF_USER_ACCESS_TOKEN, token) }
    }

    fun userAccessTokenExpireAt(tokenExpireAtSystemMillis: Long) {
        prefs.edit { putLong(PREF_USER_TOKEN_EXPIRE_AT, tokenExpireAtSystemMillis) }
    }

    fun userAccessTokenExpireAt() = prefs.getLong(PREF_USER_TOKEN_EXPIRE_AT, -1L)

    /////////////////////////
    fun username() = prefs.getString(PREF_ACCOUNT_USERNAME, "") ?: ""
    fun username(username: String) {
        prefs.edit { putString(PREF_ACCOUNT_USERNAME, username) }
    }

    fun password() = prefs.getString(PREF_ACCOUNT_PASSWORD, "") ?: ""
    fun password(password: String) {
        prefs.edit { putString(PREF_ACCOUNT_PASSWORD, password) }
    }

    fun pin() = prefs.getString(PREF_ACCOUNT_PIN, "") ?: ""
    fun pin(pin: String) {
        prefs.edit { putString(PREF_ACCOUNT_PIN, pin) }
    }

    fun mainVehicleId() = prefs.getString(PREF_MAIN_VEHICLE_ID, "") ?: ""
    fun mainVehicleId(mainVehicleId: String) {
        prefs.edit { putString(PREF_MAIN_VEHICLE_ID, mainVehicleId) }
    }
}

fun ZonedDateTime.toSystemMillis() = toInstant().toEpochMilli()
fun Long.toZonedDateTime(zoneId: ZoneId): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)

