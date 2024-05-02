package com.tmt.cache.service

import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tmt.cache.helper.Constants
import com.tmt.cache.helper.DBHelper
import com.tmt.cache.model.DownloadData
import com.tmt.cache.model.MLiveDownload
import com.pressbible.lugandan.MainActivity
import com.pressbible.lugandan.R
import org.json.JSONObject
import java.io.File
import java.util.Locale


class AllDownloadService : LifecycleService() {

  lateinit var APP_NAME: String
  private var myHandler: Handler? = null
  private var serviceRunning: Boolean = false

  private lateinit var operDB: DBHelper
  private lateinit var myDbs: java.util.ArrayList<File>
  private lateinit var genDb: DBHelper

  private lateinit var downloadManager: DownloadManager
  private val downloadIds: List<Long> = ArrayList()
  val gson = Gson()

  var mBinder: IBinder = LocalBinder()

  inner class LocalBinder : Binder() {
    val serverInstance: AllDownloadService
      get() = this@AllDownloadService
  }

  // Create a single handler to manage all progress runnables
  private val handler = Handler()

  // Create a map to store download IDs and their corresponding progress runnables
  val downloadProgressRunnables = mutableMapOf<Long, Runnable>()

  private lateinit var progressRunnable: Runnable

  private lateinit var downloadingFilesArray: ArrayList<DownloadData>
  private lateinit var liveFilesArray: ArrayList<MLiveDownload>

  private lateinit var mContext: Context

  private var isProgressChecking = false

  override fun onCreate() {
    super.onCreate()

    val pm: PackageManager = applicationContext.packageManager
    val ai: ApplicationInfo? = try {
      pm.getApplicationInfo(packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
      null
    }
    APP_NAME = (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)") as String

    downloadManager = applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager

    myHandler = Handler()

    mContext = applicationContext

    downloadingFilesArray = ArrayList()
    liveFilesArray = ArrayList()

  }

  private lateinit var book_id: String

  private fun fetchIonicData() {
    val jsonObject = JSONObject(mInterface.value.toString())
//      val mData = intent.getStringExtra("data")
    if (jsonObject.has("book_id") && jsonObject.has("file_type") && jsonObject.has("chapter") && jsonObject.has("uuid")) {
      book_id = jsonObject.getString("book_id").toLowerCase(Locale.getDefault())
      val file_type = jsonObject.getString("file_type")
      val chapterNumber = jsonObject.getString("chapter")
      val uuid = jsonObject.getString("uuid")
//        val chap_audio: Boolean = jsonObject.getBoolean("chapterAudio")
//        val teachingAudio: Boolean = jsonObject.getBoolean("teachingAudio")
      genDb = DBHelper(mContext, book_id)

      if (file_type == Constants.CHAPTER) {
        downloadingFilesArray.add(
          DownloadData(0L, book_id, file_type, genDb.getChapterUrl(chapterNumber), "", genDb.getReference(chapterNumber), chapterNumber, uuid, Constants.PENDING)
        )
      } else if (file_type == Constants.TEACHING) {
        downloadingFilesArray.add(
          DownloadData(0L, book_id, file_type, genDb.getTeachingUrl(uuid, chapterNumber), "", genDb.bookName + " " + chapterNumber, chapterNumber, uuid, Constants.PENDING)
        )
      }
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent != null) {
       fetchIonicData()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT)
      val manager = getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(serviceChannel)
    }

    val notificationIntent = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
    val notification = NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(APP_NAME)
      .setContentText(this.resources.getString(R.string.app_name) + " downloading content").setSmallIcon(android.R.drawable.ic_menu_share)
      .setContentIntent(pendingIntent).setPriority(Notification.PRIORITY_LOW)
      .setNotificationSilent().build()
    startForeground(1, notification)

    val connectivityManager =
      applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      connectivityManager.registerDefaultNetworkCallback(networkCallback)
    } else {
      connectivityManager.registerNetworkCallback(
        NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
        networkCallback
      )
    }

    return super.onStartCommand(intent, flags, START_STICKY)
  }
  private var isReceiverRegistered = false

  private fun scanAndDownloadFiles() {

    serviceRunning = false

    if(!isReceiverRegistered) {
      registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
      isReceiverRegistered = true
    }

//    operDB = DBHelper(applicationContext, "databaseName")

//      val distinctVerses = operDB.versesToDownload(book.bookId).distinct()
//      val distinctChapters = operDB.chaptersToDownload(book.bookId).distinct()
    /*
      var downloadId: 0L,
      var bookId: gen,
      var downloadUrl: http://.....,
      var filePath: "",
      var fileName: String
    */
    downloadingFilesArray.forEach {
      if(it.status == Constants.PENDING) {
        val directoryPath = it.bookId + File.separator + it.fileType // gen/(chapter/teach)

//        /android/data/package/files/gen/(chapter/teach)/gen1.mp3
        if (!File(File(getExternalFilesDir(null), directoryPath).absolutePath + it.fileName).exists()) {

          val request = DownloadManager.Request(Uri.parse(it.downloadUrl))
          request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
          request.setAllowedOverRoaming(false)
          request.setTitle("${it.fileName} ${it.fileType} audio") // Gen.1.mp3 chapter audio
          request.setDescription("")
          request.setVisibleInDownloadsUi(true)
//      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//      request.setDestinationUri(Uri.fromFile(mFile))
          request.setDestinationInExternalFilesDir(
            applicationContext,
            "${it.bookId}/${it.fileType}",
            it.fileName + if(it.fileType == Constants.TEACHING) ".mp3" else ""
          )

          it.downloadId = downloadManager.enqueue(request)
          it.status = Constants.DWNLDING

          if (!isProgressChecking) {

            /*handler.postDelayed({
              checkDownloadProgress(it.downloadId)
            }, Constants.CHECK_PROGRESS_INTERVAL)*/

            progressRunnable = Runnable {

              if(mInterface.value != null) {
                fetchIonicData()
              }
              checkDownloadProgress(it.downloadId)
              scanAndDownloadFiles()
              handler.postDelayed(progressRunnable, Constants.CHECK_PROGRESS_INTERVAL)
            }

            handler.post(progressRunnable)
          }

        }
      }
    }

    myDbs = ArrayList()

    for (dbNames in Constants.OLD_BOOKS_ID) {
      myDbs.add(File("/data/data/${applicationContext.packageName}/databases/", dbNames))
    }

    for (dbNames in Constants.NEW_BOOKS_ID) {
      myDbs.add(File("/data/data/${applicationContext.packageName}/databases/", dbNames))
    }
    val dirFile = File(applicationContext.externalCacheDir.toString() + File.separator + Constants.IMAGE_FOLDER)
    if (!dirFile.exists()) dirFile.mkdirs()


//            for (myDbFile in myDbs) {
//                if (myDbFile.exists()) {
//                    val bookId = myDbFile.name.toLowerCase();
//                    genDb = DBHelper(applicationContext, bookId)
//                    imagesList.add(genDb.imageToDownload(bookId))
//                }
//            }

//            if (!imagesList.isNullOrEmpty()) {
//                val iter: Iterator<DownloadData> = imagesList.iterator()
//                while (iter.hasNext()) {
//                    val mImage: DownloadData = iter.next()
//                    if (mImage != null) {
////                    val myDestFile = File("/data/data/${applicationContext.packageName}/cache/", mImage.fileName) // create SecurityException
//                        val myDestFile = File(applicationContext.externalCacheDir.toString() + File.separator + Constants.IMAGE_FOLDER + File.separator + mImage.fileName)
//                        if (!myDestFile.exists()) {
//                            val mUri = Uri.parse(mImage.downloadUrl)
//                            val request = DownloadManager.Request(mUri)
//                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//                            request.setAllowedOverRoaming(false)
//                            request.setTitle("Cache offline content")
//                            request.setDescription("")
//                            request.setVisibleInDownloadsUi(true)
//                            request.setDestinationUri(Uri.fromFile(myDestFile))
//
//                            mImage.downloadId = downloadManager.enqueue(request)
//                        } else {
////                            Orca Something in not right in loop. Things are repeating
////                            Log.d(CHANNEL_ID, mImage.dbName)
//
//                        }
//                    }
//
//                }
//            }

  }

  // TODO: Check Multiple download progress here
  private fun checkDownloadProgress(downloadId: Long) {
    val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    val query = DownloadManager.Query()
    query.setFilterById(downloadId)
    val cursor = downloadManager.query(query)
    if (cursor.moveToFirst()) {
      val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
      val bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
      val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
      when (status) {
        DownloadManager.STATUS_RUNNING -> {                 // Download is in progress

          val totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
          if (totalBytes > 0) {
            val downloadedBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val progress = (downloadedBytes * 100 / totalBytes).toInt()

            isProgressChecking = true

            if(downloadingFilesArray.isNotEmpty()) {

              val mPending = downloadingFilesArray.find {
                it.status == Constants.PENDING && it.downloadId == downloadId
              }

              if (mPending != null) {
                Log.e("Pending", (gson.toJson(mPending)).toString())

                if(mPending.status == Constants.PENDING) mPending.status = Constants.DWNLDING

                val index = downloadingFilesArray.indexOf(mPending)
                downloadingFilesArray[index] = mPending
              }

              val mDownloading = downloadingFilesArray.find {
                it.status == Constants.DWNLDING && it.downloadId == downloadId
              }

              if(mDownloading != null) {
                Log.e("Downloading", (gson.toJson(mDownloading)).toString())
                val index = downloadingFilesArray.indexOf(mDownloading)
                if(liveFilesArray.isEmpty()) {
                  liveFilesArray.add(MLiveDownload(downloadingFilesArray[index].fileName, "audio", progress, downloadingFilesArray[index].fileType))
                } else {
                  liveFilesArray.set(index, MLiveDownload(downloadingFilesArray[index].fileName, "audio", progress, downloadingFilesArray[index].fileType))
                }
              }

            }
            myLiveJson.postValue(gson.toJson(liveFilesArray).toString())

          }
        }

        DownloadManager.STATUS_SUCCESSFUL -> {

          // Stop constant progress checking
//          handler.removeCallbacks(progressRunnable)
        }

        DownloadManager.STATUS_FAILED -> {

        }
      }
    }
    cursor.close()
  }

  var networkCallback: NetworkCallback = object : NetworkCallback() {
    override fun onAvailable(network: Network) {
      if (!serviceRunning) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
          scanAndDownloadFiles()
        }, 5000)
      }
    }

    override fun onLost(network: Network) {
//            serviceRunning = false
    }
  }

  var onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
    override fun peekService(myContext: Context, service: Intent): IBinder {
      return super.peekService(myContext, service)
    }

    override fun onReceive(ctxt: Context, intent: Intent) {
      val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
      val mDat = downloadingFilesArray.find {
        it.downloadId == downloadId
      }!!.apply {
        this.status = Constants.COMPLETED
        this.downloadId = downloadId
      }

      liveFilesArray.remove(liveFilesArray.find {
        it.progress == 100
      })
      val liveRemove = liveFilesArray.find {
        it.progress == 100
      }

      val index = downloadingFilesArray.indexOf(mDat)
      liveFilesArray.set(index, MLiveDownload(downloadingFilesArray[index].fileName, "audio", 100, downloadingFilesArray[index].fileType))

      myLiveJson.postValue(gson.toJson(liveFilesArray).toString())

      val query = DownloadManager.Query()
      query.setFilterById(downloadId)
      val cursor: Cursor = downloadManager.query(query)

      if (cursor != null) {
        if (cursor.moveToFirst()) {
          val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
          if (DownloadManager.STATUS_RUNNING == cursor.getInt(columnIndex)) {
            val totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            if (totalBytes > 0) {
              val downloadedBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
              val progress = (downloadedBytes * 100 / totalBytes).toInt()
              Log.e("MAMA MIYA", "Broadcast $progress")
            }
          }
          if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
            val fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
            val filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            val fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
            unregisterReceiver(this)

          }
        }
      }
    }
  }

  override fun onBind(intent: Intent): IBinder {
    super.onBind(intent)
    return mBinder
  }

  companion object {
    private const val CHANNEL_ID = "DownloadServiceChannel"
    var myLiveJson = MutableLiveData<String>() // Provide progress and status of downloading to Ionic (BIDIRECTIONAL)
    var mInterface = MutableLiveData<String>() // Pass data from Ionic to DownloadService (UNIDIRECTIONAL)
  }
}
