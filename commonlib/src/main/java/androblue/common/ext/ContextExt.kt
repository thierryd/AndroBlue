package androblue.common.ext

import androblue.common.log.Logger
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

private val logger = Logger.Builder().build()

/**
 * Use appVersionName() instead of BuildConfig.versionName since each module has a BuildConfig specific to the module
 */
fun Context.appVersionName(): String {

    try {
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        return pInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        logger.e("getAppVersionName() exception", e)
    }

    return ""
}

/**
 * Use appVersionCode() instead of BuildConfig.versionCode since each module has a BuildConfig specific to the module
 */
fun Context.appVersionCode(): Long {
    try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return if (VERSION.SDK_INT >= VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    } catch (e: Exception) {
        logger.e("getAppVersionCode() exception", e)
    }
    return 1L
}