package com.tmt.cache

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.core.content.ContextCompat
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.google.gson.Gson
import com.tmt.cache.helper.Constants
import com.tmt.cache.helper.DBHelper
import com.tmt.cache.helper.DownloadDB
import com.tmt.cache.helper.Downloader
import com.tmt.cache.helper.PrefHelper
import com.tmt.cache.model.Book
import com.tmt.cache.model.BookOnly
import com.tmt.cache.model.BookPercentage
import com.tmt.cache.model.BooksResponse
import com.tmt.cache.model.DownloadData
import com.tmt.cache.model.IonicData
import com.tmt.cache.model.Teaching
import com.tmt.cache.model.TotalDownload
import com.tmt.cache.service.DownloadService
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.Executors

@CapacitorPlugin(name = "Cache")
class CachePlugin : Plugin() {

  private lateinit var mAppContext: Context

  private lateinit var genDb: DBHelper

  private lateinit var allDb: DownloadDB

  private lateinit var myPkg: String

  private lateinit var myFiles: ArrayList<File>
  private lateinit var myDbs: ArrayList<File>

  private var serviceIntent: Intent? = null
  private var mDownloadService: DownloadService? = null
  // Indicate that we would like to update download progress
  private val UPDATE_DOWNLOAD_PROGRESS = 1

  // Use a background thread to check the progress of downloading
  private val executor = Executors.newFixedThreadPool(1)

  // Use a hander to update progress bar on the main thread
  private val mainHandler: Handler = Handler(Looper.getMainLooper()) { msg ->
    if (msg.what === UPDATE_DOWNLOAD_PROGRESS) {
      val downloadProgress: Int = msg.arg1

      // Update your progress bar here.
      DownloadService.myLiveJson.value = downloadProgress.toString()
//        progressBar.setProgress(downloadProgress)
    }
    true
  }
  private lateinit var downloadManager: DownloadManager
  private lateinit var prefHelper: PrefHelper
  private lateinit var gson: Gson

  @SuppressLint("SuspiciousIndentation")
  @Throws(JSONException::class)
  @PluginMethod
  fun action(call: PluginCall) {

    myFiles = ArrayList()
    myDbs = ArrayList()
    prefHelper = PrefHelper(context)
    mAppContext = context.applicationContext

    gson = Gson()
    myPkg = mAppContext.packageName

    val ret = JSObject()

    try {
        Thread {
          Handler(Looper.getMainLooper()).post {
//          val call.data: JSONObject = call.data.getJSONObject("value")

          if (call.data.getString("type") == "initDownloadColumn") {
            if(prefHelper.getBoolean("runOnce") == true) {

              for (dbName in Constants.ALL_DB) {
                val genDb = DBHelper(mAppContext, dbName)
                genDb.updateDownloadColumn()
              }
            }
          }
          else if (call.data.getString("type") == "getTeaching") {
            val teachId = call.data.getString("teaching_id")
            val myDbFile = File("/data/data/$myPkg/databases/", call.data.getString("book_id")!!.toLowerCase(Locale.getDefault()))
            if (myDbFile.exists()) {
              val bookId = myDbFile.name.toLowerCase()
              genDb = DBHelper(mAppContext, bookId)

              ret.put("value", gson.convertToJsonString(genDb.getTeach(teachId)))
              call.resolve(ret)
            } else {
              ret.put("value", "[]")
              call.resolve(ret)
            }
          }
          else if (call.data.getString("type") == "getBookTeaching") {

            var mTeachArray: ArrayList<Teaching> = ArrayList()
            val myDbFile = File("/data/data/$myPkg/databases/", call.data.getString("book_id")!!.toLowerCase(Locale.getDefault()))
            if (myDbFile.exists()) {
              val bookId = myDbFile.name.toLowerCase();
              genDb = DBHelper(mAppContext, bookId)
              mTeachArray = genDb.teachingOnly
              ret.put("value", gson.convertToJsonString(mTeachArray.distinct()))
              call.resolve(ret)
            } else {
                ret.put("value", "[]")
                call.resolve(ret)
            }

          }
          else if (call.data.getString("type") == "getAllBooks") {
            myDbs = ArrayList()

            val mOBookArray: ArrayList<BookOnly> = ArrayList()
            val mNBookArray: ArrayList<BookOnly> = ArrayList()

            for (dbNames in Constants.OLD_BOOKS_ID) {
              myDbs.add(File("/data/data/$myPkg/databases/", dbNames))
            }

            for (myDbFile in myDbs) {
              if (myDbFile.exists()) {
                val bookId = myDbFile.name.toLowerCase()
                genDb = DBHelper(mAppContext, bookId)

                mOBookArray.add(genDb.allBooks)
              }
            }

            myDbs = ArrayList()

            for (bookId in Constants.NEW_BOOKS_ID) {
              myDbs.add(File("/data/data/$myPkg/databases/", bookId))
            }

            for (myDbFile in myDbs) {
              if (myDbFile.exists()) {
                val bookId = myDbFile.name.toLowerCase();
                genDb = DBHelper(mAppContext, bookId)

                mNBookArray.add(genDb.allBooks)
              }
            }

            val mBooksResponse = BooksResponse(mOBookArray, mNBookArray)
              ret.put("value", gson.convertToJsonString(mBooksResponse))
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "getVerses") {
            val bookId = call.data.getString("bookId")!!.toLowerCase()
            val bibleId = call.data.getString("bibleId")
            val chapterNumber = call.data.getString("chapterNumber")
            val myBook = File("/data/data/$myPkg/databases/", bookId)
            if (myBook.exists()) {
              val bookId = myBook.name.toLowerCase()
              genDb = DBHelper(mAppContext, bookId)

                ret.put("value", gson.convertToJsonString(
                    genDb.getVerses(
                        bibleId,
                        chapterNumber
                    )
                ))
                call.resolve(ret)
            } else {
                ret.put("value", "[]")
                call.resolve(ret)
            }
          }
          else if (call.data.getString("type") == "getDownloadList") {
            val book_id = call.data.getString("book_id")!!.toLowerCase(Locale.getDefault())
            val file_type = call.data.getString("file_type")
            genDb = DBHelper(mAppContext, book_id)
              ret.put("value", gson.convertToJsonString(genDb.getAllNotDownloadedUrl(file_type)))
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "getTeachings") {
            val bible_book = call.data.getString("bible_book")!!.toLowerCase()
            if (call.data.has("chapterNumber")) {
              val chapterNumber = call.data.getString("chapterNumber")
              val verseNumber = call.data.getString("verseNumber")
              val myBook = File("/data/data/$myPkg/databases/", bible_book)
              if (myBook.exists()) {
                val bookId = myBook.name
                genDb = DBHelper(mAppContext, bookId)

                  ret.put("value",
                      gson.convertToJsonString(
                          genDb.getTeachings(
                              verseNumber,
                              chapterNumber
                          )
                      ))
                  call.resolve(ret)
              } else {
                  ret.put("value", "[]")
                  call.resolve(ret)
              }
            } else {
              val myBook = File("/data/data/$myPkg/databases/", bible_book.toLowerCase())
              if (myBook.exists()) {
                val bookId = myBook.name.toLowerCase()
                genDb = DBHelper(mAppContext, bookId)

                  ret.put("value", gson.convertToJsonString(genDb.teachings))
                  call.resolve(ret)
              } else {
                  ret.put("value", "[]")
                  call.resolve(ret)
              }
            }
          }
          else if (call.data.getString("type") == "getBibleData") {
            genDb =
              DBHelper(mAppContext, call.data.getString("book_id")!!.toLowerCase(Locale.getDefault()))

            val mBook: Book = genDb.getBook(call.data.getString("book_id")!!.toLowerCase(Locale.getDefault()))
            val mBookArray: ArrayList<Book> = ArrayList()
            mBookArray.add(mBook)
              ret.put("value", gson.convertToJsonString(mBookArray))
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "startDownload") {

            mDownloadService = DownloadService()

            if (!isMyServiceRunning(DownloadService::class.java)) {
              serviceIntent = Intent(mAppContext, DownloadService::class.java)
              serviceIntent!!.putExtra("inputExtra", "Download Service")

              if (call.data != null) {

                if (call.data.has("book_id") && call.data.has("file_type") && call.data.has("chapterNumber") && call.data.has("uuid")) {
                  val mIonicData = IonicData(call.data.getString("book_id")!!.toLowerCase(Locale.getDefault()),
                    call.data.getString("file_type")!!, call.data.getString("chapterNumber")!!, call.data.getString("uuid")!!
                  )
                  genDb = DBHelper(mAppContext, mIonicData.bookId)

                  if (mIonicData.fileType == Constants.CHAPTER) {
                    val mDat = DownloadData(0L, mIonicData.bookId, mIonicData.fileType, genDb.getChapterUrl(mIonicData.chapterNumber), "", genDb.getReference(mIonicData.chapterNumber), mIonicData.chapterNumber, mIonicData.uuid,  Constants.PENDING)
                    DownloadService.mInterface.postValue(gson.convertToJsonString(mDat))

                  } else if (mIonicData.fileType == Constants.TEACHING) {
                    val mDat = DownloadData(0L, mIonicData.bookId, mIonicData.fileType, genDb.getTeachingUrl(mIonicData.uuid, mIonicData.chapterNumber), "", genDb.bookName + " " + mIonicData.chapterNumber, mIonicData.chapterNumber, mIonicData.uuid, Constants.PENDING)
                    DownloadService.mInterface.postValue(gson.convertToJsonString(mDat))
                  }
                }
                // Do something with the extra value
              }
              ContextCompat.startForegroundService(mAppContext, serviceIntent!!)
//              cordova.activity.startService(serviceIntent)
//            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE)

                ret.put("value", "Download started")
                call.resolve(ret)
            }
          }
          else if (call.data.getString("type") == "updateLocalPath") {
            val book_id = call.data.getString("book_id")!!.toLowerCase(Locale.getDefault())
            val file_type = call.data.getString("file_type")
            val local_path = call.data.getString("local_path")
            val chapter_number = call.data.getString("chapterNumber")
            val uuid = call.data.getString("uuid")

            val mVal = "{ \"message\": \"success\", \"status\": \"true\" }"
              ret.put("value", mVal)
              call.resolve(ret)

          }
          else if (call.data.getString("type") == "startAllDownload") {

            downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val book_id = call.data.getString("book_id")!!.toLowerCase(Locale.getDefault())
            val chapter = call.data.getBoolean("chapter")
            val teaching = call.data.getBoolean("teaching")
            genDb = DBHelper(mAppContext, book_id)

            if(chapter) {
              val pendingList =  genDb.pendingChapters
              pendingList.forEach {
                Downloader(it, mAppContext).start()
              }
            }
            if(teaching) {
              val pendingList =  genDb.pendingTeaching
              pendingList.forEach {
                Downloader(it, mAppContext).start()
              }
            }
              ret.put("value", "{ \"message\": \"success\", \"status\": \"true\" }")
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "getLiveProgress") {
              ret.put("value", DownloadService.myLiveJson.value)
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "getPercentage") {

            val book_id = call.data.getString("book_id")!!.toLowerCase(Locale.getDefault())
            val file_type = call.data.getString("file_type")
            var downloaded: Double = 0.0
            var total: Double = 0.0

            genDb = DBHelper(mAppContext, book_id)

            when (file_type) {
              Constants.CHAPTER -> {
                downloaded = genDb.getDownloadCompleted(DBHelper.TABLE_AUDIO).toDouble()
                total = genDb.chaptersCount.toDouble()
              }
              Constants.TEACHING -> {
                downloaded = genDb.getDownloadCompleted(DBHelper.TABLE_TEACHING).toDouble()
                total = genDb.teachingCount.toDouble()
              }
            }

            if(total != 0.0) {
              val percent = (downloaded / total) * 100

              Log.e("Percent", "$downloaded $total $percent")
              var status = Constants.NIL

              // TODO: Check here how many are downloading from TABLE_DOWNLOAD
              when (percent) {
                0.0 -> status = Constants.NIL
                in 0.1..99.9 -> status = Constants.PENDING
                100.0 -> status = Constants.COMPLETED
              }
              val iPercent = percent.toInt()
              val mVal = "{ \"message\": \"success\", \"status\": \"true\", \"download_status\": \"$status\", \"download_percentage\": \"$iPercent\" }"
                ret.put("value", mVal.toString())
            } else {
                ret.put("value", "{}")
            }
            call.resolve(ret)
          }
          else if (call.data.getString("type") == "getBookPercentage") {
            var mArrayData = ArrayList<BookPercentage>()

            val file_type = call.data.getString("file_type")

            var downloaded: Double = 0.0
            var total: Double = 0.0

            for (dbName in Constants.ALL_DB) {
              val genDb = DBHelper(mAppContext, dbName)
              when (file_type) {
                Constants.CHAPTER -> {
                  downloaded = genDb.getDownloadCompleted(DBHelper.TABLE_AUDIO).toDouble()
                  total = genDb.chaptersCount.toDouble()
                }
                Constants.TEACHING -> {
                  downloaded = genDb.getDownloadCompleted(DBHelper.TABLE_TEACHING).toDouble()
                  total = genDb.teachingCount.toDouble()
                }
              }
              if(total != 0.0) {
                val percent = (downloaded / total) * 100

                var status = Constants.NIL

                when (percent) {
                  0.0 -> status = Constants.NIL
                  in 0.1..99.9 -> status = Constants.PENDING
                  100.0 -> status = Constants.COMPLETED
                }
                mArrayData.add(BookPercentage(dbName.toUpperCase(), status, percent.toInt()))
              }

            }
              ret.put("value", gson.convertToJsonString(mArrayData))
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "getBookDownloads") {

            val bookId = call.data.getString("bookId")!!.toLowerCase()

            val genDb = DBHelper(mAppContext, bookId)
              ret.put("value", gson.convertToJsonString(genDb.bookDownloaded))
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "getStatus") {

            val book_id = call.data.getString("book_id")!!.toLowerCase(Locale.getDefault())
            val file_type = call.data.getString("file_type")
            val chapterNumber = call.data.getString("chapterNumber")

            genDb = DBHelper(mAppContext, book_id)
            val mVal = "{ \"message\": \"success\", \"status\": \"true\" }"
              ret.put("value", mVal)
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "updateDownload") {
            val file_name = call.data.getString("file_name")
//          PHP_chapter_3.mp3
            val book_id = file_name?.split("_")?.get(0)?.toLowerCase(Locale.getDefault())

            genDb = DBHelper(mAppContext, book_id)
            genDb.updateDownloadedData(call.data)
            val mVal = "{ \"message\": \"success\", \"status\": \"true\" }"
              ret.put("value", mVal)
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "delete") {
            val book_id = call.data.getString("book_id")!!.toLowerCase(Locale.getDefault())
            val file_type = call.data.getString("file_type")
            val chapterNumber = call.data.getString("chapterNumber")
            val uuid = call.data.getString("uuid")

            genDb = DBHelper(mAppContext, book_id)
            genDb.deleteDownloads(file_type, chapterNumber, uuid)

            val mVal = "{ \"message\": \"success\", \"status\": \"true\" }"
              ret.put("value", mVal)
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "deleteDownloads") {
            val book_id = call.data.getString("book_id")!!.toLowerCase(Locale.getDefault())
            val file_type = call.data.getString("file_type")
            val chapterDownloads = call.data.getBoolean("chapterDownloads")
            val studyDownloads = call.data.getBoolean("studyDownloads")

            when (file_type) {
              "single" -> {
                genDb = DBHelper(mAppContext, book_id)
                if(chapterDownloads) {
                  genDb.deleteDownloads(Constants.CHAPTER)
                }
                if(studyDownloads) {
                  genDb.deleteDownloads(Constants.TEACHING)
                }
              }
              "all" -> {
                for (dbName in Constants.ALL_DB) {
                  genDb = DBHelper(mAppContext, dbName)
                  if(chapterDownloads) {
                    genDb.deleteAllChapters()
                  }
                  if(studyDownloads) {
                    genDb.deleteAllTeaching()
                  }
                }
              }
            }
            val mVal = "{ \"message\": \"success\", \"status\": \"true\" }"
              ret.put("value", mVal)
              call.resolve(ret)
          }
          else if (call.data.getString("type") == "getTotalDownloads") {
            var mArrayData = ArrayList<TotalDownload>()

            for (dbName in Constants.ALL_DB) {
              val genDb = DBHelper(mAppContext, dbName)
              if (genDb.getAtleastDownloaded(dbName.toUpperCase()) != null) {
                mArrayData.add(genDb.getAtleastDownloaded(dbName.toUpperCase()))
              }
            }
              ret.put("value", gson.convertToJsonString(mArrayData))
              call.resolve(ret)
          }
        }
      }.start()
    } catch (e: Exception) {
      call.resolve()
    }
  }

  fun downloadFile(context: Context, fileName: String, fileExtension: String, destinationDirectory: String?, url: String?) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val uri = Uri.parse(url)
    val request = DownloadManager.Request(uri)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension)
    val downloadId = downloadManager.enqueue(request)

    // Run a task in a background thread to check download progress
    executor.execute(Runnable {
      var progress = 0
      var isDownloadFinished = false
      while (!isDownloadFinished) {
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.moveToFirst()) {
          val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
          when (downloadStatus) {
            DownloadManager.STATUS_RUNNING -> {
              val totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
              if (totalBytes > 0) {
                val downloadedBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                progress = (downloadedBytes * 100 / totalBytes).toInt()
              }
            }

            DownloadManager.STATUS_SUCCESSFUL -> {
              progress = 100
              isDownloadFinished = true
            }

            DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {}
            DownloadManager.STATUS_FAILED -> isDownloadFinished = true
          }
          val message: Message = Message.obtain()
          message.what = UPDATE_DOWNLOAD_PROGRESS
          message.arg1 = progress
          mainHandler.sendMessage(message)
        }
      }
    })
  }

  private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager =
      mAppContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
      if (serviceClass.name == service.service.className) {
        return true
      }
    }
    return false
  }

  fun <T> Gson.convertToJsonString(t: T): String {
    return toJson(t).toString()
  }

  private fun dumpFileIntoPhone(FILE_PATH: String, FILE_NAME: String) {

    if (!File(FILE_PATH).exists()) File(FILE_PATH).mkdir()

    val myInput: InputStream = mAppContext.assets.open(FILE_NAME)
    val outFileName = FILE_PATH + FILE_NAME
    val myOutput: OutputStream = FileOutputStream(outFileName)
    val buffer = ByteArray(1024)
    var length: Int
    while (myInput.read(buffer).also { length = it } > 0) {
      myOutput.write(buffer, 0, length)
    }
    myOutput.flush()
    myOutput.close()
    myInput.close()
  }

  private fun dumpOfflieFileIntoPhone(mFile: File) {

    val myInput: InputStream = mAppContext.assets.open(mFile.name)
    val outFileName = mFile.path
    val myOutput: OutputStream = FileOutputStream(outFileName)
    val buffer = ByteArray(1024)
    var length: Int
    while (myInput.read(buffer).also { length = it } > 0) {
      myOutput.write(buffer, 0, length)
    }
    myOutput.flush()
    myOutput.close()
    myInput.close()
  }

  companion object {
    private const val TAG = "Cache"
  }
}

