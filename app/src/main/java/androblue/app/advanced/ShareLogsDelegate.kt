package androblue.app.advanced

import androblue.app.BuildConfig
import androblue.app.R
import androblue.common.TimeService
import androblue.common.dagger.ScopeActivity
import androblue.common.log.Logger
import androblue.common.log.db.LoggerDatabaseHelper
import androblue.common.utils.UIThread
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.Builder
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.util.Locale
import javax.inject.Inject

private const val LOG_FILE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

@ScopeActivity
class ShareLogsDelegate @Inject constructor(private val activity: AdvancedActivity,
                                            private val timeService: TimeService,
                                            private val loggerDatabaseHelper: LoggerDatabaseHelper) {

    private val logger = Logger.Builder().build()
    private val filesShared = arrayListOf<File>()

    init {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                filesShared.forEach {
                    val path = it.absolutePath
                    val result = it.delete()
                    logger.d("ShareLogsDelegate deleted file:$path result:$result")
                }
            }
        })
    }

    fun shareLogs() {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            loggerDatabaseHelper.flush()

            withContext(Dispatchers.Main) {
                showDialog(loggerDatabaseHelper.count())
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showDialog(logCount: Int) {
        val progressView = LayoutInflater.from(activity).inflate(R.layout.home_sharelogprogress, null)

        val alertDialog = createProgressDialog(progressView, logCount)

        val job = activity.lifecycleScope.launch {
            val logFormatter = DateTimeFormatter.ofPattern(LOG_FILE_DATE_FORMAT, Locale.ENGLISH)
            val timestamp = logFormatter.format(ZonedDateTime.now(timeService.defaultZoneId()))
            val fileWithLogs = createTempLogFile(progressView, timestamp)

            if (!isActive) {
                return@launch
            }

            shareLogs(fileWithLogs)

            UIThread.post(Runnable { alertDialog.cancel() })
        }

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.admin_general_shareLogsCancel)) { _, _ -> job.cancel() }
        alertDialog.setOnDismissListener { job.cancel() }

        alertDialog.show()
    }

    private fun shareLogs(fileWithLogs: File) {
        filesShared.add(fileWithLogs)

        val logFormatter = DateTimeFormatter.ofPattern(LOG_FILE_DATE_FORMAT, Locale.ENGLISH)
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "text/plain"

        val shareSubject = "${activity.getString(R.string.admin_general_shareLogSubject)} - ${logFormatter.format(ZonedDateTime.now(timeService.defaultZoneId()))}"
        intent.putExtra(Intent.EXTRA_SUBJECT, shareSubject)
        intent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.admin_general_shareLogText))

        val files = arrayListOf<Uri>().apply {
            add(FileProvider.getUriForFile(activity, "${BuildConfig.APPLICATION_ID}.fileprovider", fileWithLogs))
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)

        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.admin_general_shareVia)))
    }

    @Suppress("SuspendFunctionOnCoroutineScope")
    private suspend fun CoroutineScope.createTempLogFile(progressView: View, timestamp: String): File {
        val file = createTempFile("logs_$timestamp.txt")
        writeLogToFile(progressView, file)
        return file
    }

    private fun createTempFile(fileName: String): File {
        val filePath = File(activity.cacheDir, "logs")
        filePath.mkdir()

        val file = File(filePath, fileName)
        file.createNewFile()
        return file
    }

    private suspend fun CoroutineScope.writeLogToFile(progressView: View, file: File) {
        val progressBar = progressView.findViewById<ProgressBar>(R.id.admin_sharelogprogress)

        val logFormatter = DateTimeFormatter.ofPattern(LOG_FILE_DATE_FORMAT, Locale.ENGLISH)
        val logs = loggerDatabaseHelper.loadAll()
        progressBar.max = logs.size

        logs.forEachIndexed { index, entity ->
            file.appendText("${entity.toNiceString(logFormatter)}\n")
            if (index % 100 == 0) {
                progressBar.progress = index
            }

            if (!isActive) {
                return@forEachIndexed
            }
        }
    }

    private fun createProgressDialog(progressView: View, logCount: Int): AlertDialog {
        return Builder(activity)
                .setTitle(R.string.admin_general_shareLogs)
                .setView(progressView)
                .setMessage(activity.resources.getQuantityString(R.plurals.admin_general_loading, logCount, logCount))
                .create()
    }
}