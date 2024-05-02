package com.tmt.cache.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tmt.cache.model.Book;
import com.tmt.cache.model.BookOnly;
import com.tmt.cache.model.Chapters;
import com.tmt.cache.model.DownloadData;
import com.tmt.cache.model.IonicData;
import com.tmt.cache.model.MNVerse;
import com.tmt.cache.model.MVerse;
import com.tmt.cache.model.Teaching;
import com.tmt.cache.model.Verse;

import java.util.ArrayList;

public class DownloadDB extends SQLiteOpenHelper {

  public static final String DATABASE_NAME = "downloads";

  ////////////////////////////////////////// DOWNLOAD_DATA ///////////////////////////////////////
  public static final String TABLE_DOWNLOAD = "DOWNLOAD";
  public static final String BOOK_NAME_ENG = "BOOK_NAME_ENG";
  public static final String BOOK_ID = "BOOK_ID";
  public static final String CHAPTERNUMBER = "CHAPTERNUMBER";
  public static final String UUID = "UUID";
  public static final String MIME_TYPE = "MIME_TYPE";
  public static final String AUDIO_PATH = "AUDIO_PATH";
  public static final String DOWNLOAD_ID = "DOWNLOAD_ID";

  public static final String URL = "URL";
  public static final String LOCAL_PATH = "LOCAL_PATH";
  public static final String TYPE = "TYPE";
  public static final String STATUS = "STATUS";

  public DownloadDB(Context context) {
    super(context, DATABASE_NAME, null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table " + TABLE_DOWNLOAD + "(id integer primary key, " + BOOK_ID + " text, " + BOOK_NAME_ENG + " text, " + TYPE + " text, " + DOWNLOAD_ID + " text, " + URL + " text, " + AUDIO_PATH + " text," + MIME_TYPE + " text," + CHAPTERNUMBER + " text, " + UUID + " text, " + STATUS + " text)");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOAD + "");
    onCreate(db);
  }

  public Cursor getData(int id) {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor res = db.rawQuery("select * from contacts where id=" + id + "", null);
//        res.close();
    return res;
  }

  public boolean downloadExist(IonicData mData) {
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("Select * from " + TABLE_DOWNLOAD + " WHERE " + BOOK_ID + "=?  AND " + TYPE + "=? AND " + CHAPTERNUMBER + "= ? AND " + UUID + "= ?", new String[]{mData.getBookId(), mData.getFileType(), mData.getChapterNumber(), mData.getUuid()});
    if (cursor.getCount() <= 0) {
      cursor.close();
      db.close();
      return false;
    }
    cursor.close();
    db.close();
    return true;
  }

  public boolean statusPendingOrDownloading(IonicData mData) {
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("Select * from " + TABLE_DOWNLOAD + " WHERE " + BOOK_ID + "=?  AND " + TYPE + "=? AND " + CHAPTERNUMBER + "= ? AND " + UUID + "= ? AND " + STATUS + " IN (?, ?)", new String[]{mData.getBookId(), mData.getFileType(), mData.getChapterNumber(), mData.getUuid(), Constants.PENDING, Constants.DWNLDING});
    if (cursor.getCount() <= 0) {
      cursor.close();
      db.close();
      return false;
    }
    cursor.close();
    db.close();
    return true;
  }

}
