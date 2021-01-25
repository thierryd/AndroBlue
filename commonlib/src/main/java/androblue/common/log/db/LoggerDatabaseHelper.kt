package androblue.common.log.db

import androblue.common.dagger.ScopeApplication
import androblue.common.log.Category
import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import kotlin.math.min

@Suppress("EXPERIMENTAL_API_USAGE")
@ScopeApplication
class LoggerDatabaseHelper @Inject constructor(private val loggerDao: LoggerDao) {

    companion object {
        @VisibleForTesting const val LOG_BUFFER_CAPACITY = 250
    }

    private val mutex = Mutex()
    private var currentBuffer = arrayListOf<LoggerItemEntity>()

    fun logToDatabase(priority: Int, category: Category, date: ZonedDateTime, thread: String, message: String) {
        runBlocking {
            mutex.withLock {
                currentBuffer.add(LoggerItemEntity(null, priority, category, date, thread,
                                                   message.substring(0, min(message.length, 400)))) //store a message with a max length of 400

                if (currentBuffer.size >= LOG_BUFFER_CAPACITY) {
                    saveBufferToDb()
                }
            }
        }
    }

    suspend fun loadAll(): Array<LoggerItemEntity> {
        return loggerDao.loadAll()
    }

    suspend fun count(): Int {
        return loggerDao.count()
    }

    fun flush() {
        Log.d("TDRA", "TDRA AAAA ")
        runBlocking {
            Log.d("TDRA", "TDRA BBBB ")
            mutex.withLock {
                Log.d("TDRA", "TDRA CCCC ")
                saveBufferToDb()
                Log.d("TDRA", "TDRA DDDD ")
            }
        }
    }

    private fun saveBufferToDb() {
        Log.d("TDRA", "TDRA CCCC 1")
        val buffer = currentBuffer
        Log.d("TDRA", "TDRA CCCC 2")
        currentBuffer = arrayListOf()
        Log.d("TDRA", "TDRA CCCC 3")
        val entities = buffer.toTypedArray()
        Log.d("TDRA", "TDRA CCCC 3 A")
        loggerDao.save(entities)
        Log.d("TDRA", "TDRA CCCC 3 B")
        Log.d("TDRA", "TDRA CCCC 4")
    }
}