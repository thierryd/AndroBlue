package androblue.common.preference

import android.content.SharedPreferences
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * We need to keep a list of listeners in a set since SharedPreference keep weak reference only and the listeners
 * are garbage collected if we don't keep a hard link to it
 */
private val listenersSet = hashSetOf<CoroutineSinglePreferenceChangeListener<*>>()
private val mutexListenerSet = Mutex()

/**
 * FlowPreferences is used to observe a preference and have a callback the when the value changes
 */
class FlowPreferences(private val prefs: SharedPreferences) {

    companion object {
        fun create(prefs: SharedPreferences) = FlowPreferences(prefs)
    }

    @Suppress("UNCHECKED_CAST")
    fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
        return prefs.getPreferenceFlow(SharedPreferences::getBoolean, key, defaultValue)
    }

    @Suppress("UNCHECKED_CAST")
    fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
        return prefs.getPreferenceFlow(SharedPreferences::getInt, key, defaultValue)
    }

    @Suppress("UNCHECKED_CAST")
    fun getFloatFlow(key: String, defaultValue: Float): Flow<Float> {
        return prefs.getPreferenceFlow(SharedPreferences::getFloat, key, defaultValue)
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringFlow(key: String, defaultValue: String): Flow<String> {
        return prefs.getPreferenceFlow(SharedPreferences::getString, key, defaultValue) as Flow<String>
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringSetFlow(key: String, defaultValue: Set<String>): Flow<Set<String>> {
        return prefs.getPreferenceFlow(SharedPreferences::getStringSet, key, defaultValue) as Flow<Set<String>>
    }

}

private fun <T> SharedPreferences.getPreferenceFlow(getPreference: SharedPreferences.(String, T) -> T, key: String, defaultValue: T): Flow<T> = callbackFlow {
    offer(getPreference(key, defaultValue))

    val listener = CoroutineSinglePreferenceChangeListener(this, key, defaultValue, getPreference)
    mutexListenerSet.withLock {
        listenersSet.add(listener)
    }

    registerOnSharedPreferenceChangeListener(listener)

    listener.channel.awaitClose {
        unregisterOnSharedPreferenceChangeListener(listener)
        listenersSet.remove(listener)
    }
}

private class CoroutineSinglePreferenceChangeListener<T> constructor(val channel: ProducerScope<T>,
                                                                     private val key: String,
                                                                     private val defaultValue: T,
                                                                     private inline val getPreference: SharedPreferences.(String, T) -> T)
    : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (this.key == key) {
            onSharedPreferenceChanged(sharedPreferences)
        }
    }

    private fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences) {
        channel.offer(sharedPreferences.getCurrentValue())
    }

    fun SharedPreferences.getCurrentValue() = getPreference(key, defaultValue)
}