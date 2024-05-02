package com.tmt.cache.helper

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.tmt.cache.model.DownloadData
import java.util.concurrent.Executors

class Downloader(val it: DownloadData, val context: Context) {

  private lateinit var downloadManager: DownloadManager
  private val executor = Executors.newFixedThreadPool(1)

  public fun start() {
    downloadManager = context.getSystemService(LifecycleService.DOWNLOAD_SERVICE) as DownloadManager

    val request = DownloadManager.Request(Uri.parse(it.downloadUrl))
    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
    request.setAllowedOverRoaming(false)
    request.setTitle("${it.fileName} ${it.fileType} audio") // Gen.1.mp3 chapter audio
    request.setDescription("")
    request.setVisibleInDownloadsUi(true)
//      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//      request.setDestinationUri(Uri.fromFile(mFile))
    request.setDestinationInExternalFilesDir(
      context,
      "${it.bookId.toLowerCase()}/${it.fileType}",
      "${it.downloadUrl.substring(it.downloadUrl.lastIndexOf("/") + 1)}"
    )

    val downloadId = downloadManager.enqueue(request)
    executor.execute(Runnable {
      var isDownloadFinished = false
      while (!isDownloadFinished) {
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor != null) {
          if (cursor.moveToFirst()) {
            when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
              DownloadManager.STATUS_RUNNING -> {
                val totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                if (totalBytes > 0) {
                  val downloadedBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                  val progress = (downloadedBytes * 100 / totalBytes).toInt()
                  Log.e("MAMA MIYA", "Downloader $progress")
                }
              }
              DownloadManager.STATUS_SUCCESSFUL -> {
                val fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                val filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                val fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
                Log.e("MAMA MIYA", "Downloader 100%")

                isDownloadFinished = true
                executor.shutdown()
              }
              DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {}
              DownloadManager.STATUS_FAILED -> {
                isDownloadFinished = true
                executor.shutdown()
              }
            }
          }
        }
      }
    })
  }
}
