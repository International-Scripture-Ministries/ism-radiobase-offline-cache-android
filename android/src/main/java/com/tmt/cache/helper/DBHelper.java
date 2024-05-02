package com.tmt.cache.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tmt.cache.model.Book;
import com.tmt.cache.model.BookDownload;
import com.tmt.cache.model.BookOnly;
import com.tmt.cache.model.Chapters;
import com.tmt.cache.model.DownloadData;
import com.tmt.cache.model.MNVerse;
import com.tmt.cache.model.MVerse;
import com.tmt.cache.model.Teaching;
import com.tmt.cache.model.TotalDownload;
import com.tmt.cache.model.URLData;
import com.tmt.cache.model.Verse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
  ////////////////////////////////////////// TABLE_DATA //////////////////////////////////////////
  public static final String TABLE_BOOK = "BOOKS";
  public static final String TABLE_VERSES = "VERSES";
  public static final String TABLE_TEACHING = "TEACHING";
  public static final String TABLE_AUDIO = "AUDIO";

  ////////////////////////////////////////// BOOK_DATA ///////////////////////////////////////////
  public static final String BIBLE_ID = "BIBLE_ID";
  public static final String ABBREVIATION = "ABBREVIATION";
  public static final String BIBLE = "BIBLE";
  public static final String BOOKORDER = "BOOKORDER";
  public static final String NAME = "NAME";
  public static final String NAMELONG = "NAMELONG";
  public static final String TESTAMENT = "TESTAMENT";
  public static final String NUMBER_OF_CHAPTERS = "NUMBER_OF_CHAPTERS";
  public static final String BOOK_NAME_ENG = "BOOK_NAME_ENG";
  public static final String IMAGE_PATH = "IMAGE_PATH";

  ////////////////////////////////////////// VERSE_DATA //////////////////////////////////////////
  public static final String VERSE_ID = "VERSE_ID";
  public static final String BOOK_ID = "BOOK_ID";
  public static final String CHAPTER_ID = "CHAPTER_ID";
  public static final String CONTENT = "CONTENT";
  public static final String REFERENCE = "REFERENCE";
  public static final String VERSENUMBER = "VERSENUMBER";
  public static final String CHAPTERNUMBER = "CHAPTERNUMBER";


  ////////////////////////////////////////// VERSE_DATA //////////////////////////////////////////
  public static final String UUID = "UUID";
  public static final String AUDIO_DURATION = "AUDIO_DURATION";
  public static final String AUDIO_FORMAT = "AUDIO_FORMAT";
  public static final String BIBLE_BOOK = "BIBLE_BOOK";
  public static final String BIBLE_CHAPTER_END = "BIBLE_CHAPTER_END";
  public static final String BIBLE_CHAPTER_START = "BIBLE_CHAPTER_START";
  public static final String BIBLE_VERSE_END = "BIBLE_VERSE_END";
  public static final String BIBLE_VERSE_START = "BIBLE_VERSE_START";
  public static final String CREATED = "CREATED";
  public static final String CREATED_BY = "CREATED_BY";
  public static final String DESCRIPTION = "DESCRIPTION";
  public static final String LANGUAGE = "LANGUAGE";
  public static final String MIME_TYPE = "MIME_TYPE";
  public static final String SCHEDULED_DATE = "SCHEDULED_DATE";
  public static final String AUDIO_PATH = "AUDIO_PATH";

  ////////////////////////////////////////// AUDIO_DATA //////////////////////////////////////////
  public static final String NUMBER = "NUMBER";
  public static final String UPDATED = "UPDATED";
  public static final String DOWNLOAD_ID = "DOWNLOAD_ID";
  public static final String URL = "URL";

  public DBHelper(Context context, String databaseName) {
    super(context, databaseName, null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BOOK + "(id integer primary key, " + BIBLE_ID + " text," + BOOK_ID + " text," + ABBREVIATION + " text," + BIBLE + " text," + BOOKORDER + " text," + NAME + " text," + NAMELONG + " text," + TESTAMENT + " text," + NUMBER_OF_CHAPTERS + " text," + BOOK_NAME_ENG + " text," + IMAGE_PATH + " text)");
    db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_VERSES + "(id integer primary key, " + BIBLE_ID + " text," + VERSE_ID + " text," + BOOK_ID + " text, " + CHAPTER_ID + " text," + CONTENT + " text," + REFERENCE + " text," + VERSENUMBER + " text," + CHAPTERNUMBER + " text," + UUID + " text)");
    db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TEACHING + "(id integer primary key, " + UUID + " text, " + AUDIO_DURATION + " text, " + AUDIO_FORMAT + " text, " + BIBLE_BOOK + " text, " + BIBLE_CHAPTER_END + " text, " + BIBLE_CHAPTER_START + " text," + BIBLE_VERSE_END + " text," + BIBLE_VERSE_START + " text, " + CREATED + " text, " + CREATED_BY + " text, " + DESCRIPTION + " text, " + LANGUAGE + " text, " + MIME_TYPE + " text, " + NAME + " text, " + SCHEDULED_DATE + " text, " + CHAPTERNUMBER + " text, " + AUDIO_PATH + " text)");
    db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_AUDIO + "(id integer primary key, " + BIBLE_ID + " text, " + VERSE_ID + " text, " + BOOK_ID + " text, " + CREATED + " text, " + NUMBER + " text, " + REFERENCE + " text," + UPDATED + " text," + URL + " text, " + DOWNLOAD_ID + " text, " + AUDIO_PATH + " text)");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOK + "");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSES + "");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHING + "");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDIO + "");
    onCreate(db);
  }

  public void deleteDownloads(String file_type) {
    SQLiteDatabase db = this.getWritableDatabase();

    switch (file_type) {
      case Constants.CHAPTER:
        db.execSQL("UPDATE " + TABLE_AUDIO + " SET " + AUDIO_PATH + " = ''");
        break;
      case Constants.TEACHING:
        db.execSQL("UPDATE " + TABLE_TEACHING + " SET " + AUDIO_PATH + " = ''");
        break;
    }

    db.close();
  }

  public void deleteDownloads(String file_type, String chapterNumber, String uuid) {
    SQLiteDatabase db = this.getWritableDatabase();

    String mTable = "", whereClause = "";
    try {
      ContentValues values = new ContentValues();
      String[] whereArgs = null;
      values.put(AUDIO_PATH, "");
      switch (file_type) {
        case Constants.CHAPTER:
          mTable = TABLE_AUDIO;
          whereClause = NUMBER + " = ?";
          whereArgs = new String[]{chapterNumber};
          break;
        case Constants.TEACHING:
          mTable = TABLE_TEACHING;
          whereClause = UUID + " = ?";
          whereArgs = new String[]{uuid};
          break;
      }

      db.update(mTable, values, whereClause, whereArgs);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    db.close();
  }


  public void deleteAllDownloads() {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("UPDATE " + TABLE_AUDIO + " SET " + AUDIO_PATH + " = ''");
    db.execSQL("UPDATE " + TABLE_TEACHING + " SET " + AUDIO_PATH + " = ''");
    db.close();
  }
  public void deleteAllChapters() {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("UPDATE " + TABLE_AUDIO + " SET " + AUDIO_PATH + " = ''");
    db.close();
  }
  public void deleteAllTeaching() {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("UPDATE " + TABLE_TEACHING + " SET " + AUDIO_PATH + " = ''");
    db.close();
  }

  public TotalDownload getAtleastDownloaded(String book_id) {
    TotalDownload mData = null;

    SQLiteDatabase db = this.getWritableDatabase();

    boolean atleastOne = false;

    String query = "SELECT COUNT(*) FROM " + TABLE_AUDIO + " WHERE " + AUDIO_PATH + " != NULL OR " + AUDIO_PATH + " != ''";

    Cursor cursor = db.rawQuery(query, null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        if(cursor.getInt(0) != 0) {
          atleastOne = true;
        }
      }
      cursor.close();
    }

    query = "SELECT COUNT(*) FROM " + TABLE_TEACHING + " WHERE " + AUDIO_PATH + " != NULL OR " + AUDIO_PATH + " != ''";

    cursor = db.rawQuery(query, null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        if(cursor.getInt(0) != 0) {
          atleastOne = true;
        }
      }
      cursor.close();
    }

    if (atleastOne) {
      String bookName = "";
      cursor = db.rawQuery("Select " + NAME + " from " + TABLE_BOOK, null);
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          bookName = cursor.getString(cursor.getColumnIndex(NAME));
        }
      }
      cursor.close();
      mData = new TotalDownload(book_id, bookName);
    }

    db.close();

    return mData;
  }

  public void updateDownloadedData(JSONObject mData) {
    SQLiteDatabase db = this.getWritableDatabase();

    String file_name, file_type, whereValue, local_path;

    try {
      file_name = mData.getString("file_name");
      file_type = file_name.split("_")[1];
      whereValue = file_name.split("_")[2].split("\\.")[0];
      local_path = mData.getString("local_path").replace("file:///", "");

      String whereClause= "", mTable = "";

      ContentValues values = new ContentValues();
      String[] whereArgs = null;
      values.put(AUDIO_PATH, local_path);

      if (file_type.equals(Constants.CHAPTER)) {
//        String rawQuery = "UPDATE "+TABLE_AUDIO+" SET \'"+AUDIO_PATH+"\' = "+local_path+" WHERE "+NUMBER+" = "+ chapterNumber;
        mTable = TABLE_AUDIO;
        whereClause = NUMBER + " = ?";
        whereArgs = new String[]{ whereValue };
      } else if(file_type.equals(Constants.TEACHING)) {
//        String rawQuery = "UPDATE "+TABLE_TEACHING+" SET '"+AUDIO_PATH+"' = "+local_path+" WHERE "+CHAPTERNUMBER+" = "+ chapterNumber;
        mTable = TABLE_TEACHING;
        whereClause = UUID + " = ?";
        whereArgs = new String[]{ whereValue };
      }

      db.update(mTable, values, whereClause, whereArgs);

    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    db.close();
  }
  public void updateDownloadColumn() {
    SQLiteDatabase db = getWritableDatabase();

    // Construct the SQL ALTER TABLE statement to add the new column
    String alterTableSQL = "ALTER TABLE " + TABLE_TEACHING + " ADD COLUMN " + DOWNLOAD_ID + " " + "TEXT";

    try {
      // Execute the SQL statement to add the new column
      db.execSQL(alterTableSQL);
    } catch (SQLException e) {
      // Handle any errors that may occur during the execution of the SQL statement
      e.printStackTrace();
    }

// Close the database
    db.close();
  }

  public Cursor getData(int id) {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor res = db.rawQuery("select * from contacts where id=" + id + "", null);
//        res.close();
    return res;
  }

  public int numberOfBooks() {
    SQLiteDatabase db = this.getReadableDatabase();
    int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_BOOK);
    return numRows;
  }

  public int getAudioCount() {
    int count = 0;
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("Select * from " + TABLE_VERSES, null);
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      String verseUrl = cursor.getString(10); // position of TEACHING_AUDIO
      if (verseUrl.startsWith("file:///")) {
        count++;
      }
      cursor.moveToNext();
    }
    cursor.close();
    return count;
  }

  public int getChaptersCount() {
    SQLiteDatabase db = this.getReadableDatabase();
    int emptyRowCount = 0;

    try {
    String query = "SELECT COUNT(*) FROM " + TABLE_AUDIO;

    Cursor cursor = db.rawQuery(query, null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        emptyRowCount = cursor.getInt(0);
      }
      if(!cursor.isClosed()) {
        cursor.close();
      }
    }

    } catch (Exception e) {
    e.printStackTrace();
  }finally{
      if(db.isOpen()) {
        db.close();
      }
    }

    return emptyRowCount;
  }

  public int getTeachingCount() {
    SQLiteDatabase db = this.getReadableDatabase();
    int emptyRowCount = 0;
    try {

    String query = "SELECT COUNT(*) FROM " + TABLE_TEACHING;

    Cursor cursor = db.rawQuery(query, null);

    if (cursor != null) {
      if (cursor.moveToFirst()) {
        emptyRowCount = cursor.getInt(0);
        cursor.close();
      }
    }
  } catch (Exception e) {
    e.printStackTrace();
  }finally{
      if(db.isOpen()) {
        db.close();
      }
    }

    return emptyRowCount;
  }

  public int getDownloadCompleted(String table) {
    SQLiteDatabase db = this.getReadableDatabase();
    int toBeDownload = 0;
    try {
    String query = "SELECT COUNT(*) FROM " + table + " WHERE " + AUDIO_PATH + " != NULL OR " + AUDIO_PATH + " != ''";

    Cursor cursor = db.rawQuery(query, null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        toBeDownload = cursor.getInt(0);
        cursor.close();
      }
    }
    } catch (Exception e) {
    e.printStackTrace();
  }finally{
    if(db.isOpen()) {

      if(db.isOpen()) {
        db.close();
      }
    }
  }

    return toBeDownload;
  }

  public boolean bookExist(String book_id) {
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("Select * from " + TABLE_BOOK + " WHERE " + BOOK_ID + "=?", new String[]{book_id});
    if (cursor.getCount() <= 0) {
      cursor.close();
      db.close();
      return false;
    }
    cursor.close();
    db.close();
    return true;
  }

  public Teaching getTeach(String mTeachId) {
    Teaching mTeach = null;
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor res = db.rawQuery("Select * from " + TABLE_VERSES + " WHERE " + TABLE_TEACHING + "=?", new String[]{mTeachId});
    if (res != null && res.moveToFirst()) {
      mTeach = new Teaching(res.getString(1), res.getString(2), res.getString(9), res.getString(10), res.getString(11), res.getString(12), res.getString(13), res.getString(14), res.getString(15));
      res.close();
    }
    return mTeach;
  }

  public ArrayList<DownloadData> getPendingChapters() {
    ArrayList<DownloadData> mChapters = new ArrayList<>();
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor res = db.rawQuery("Select * from " + TABLE_AUDIO + " WHERE " + DOWNLOAD_ID + "=? AND " + AUDIO_PATH + "= ?", new String[]{"", ""});
    if (res != null && res.moveToFirst()) {
      while (!res.isAfterLast()) {
        mChapters.add(new DownloadData(0L, res.getString(3), Constants.CHAPTER, res.getString(8), "", res.getString(6), res.getString(5), "",  Constants.PENDING));
        res.moveToNext();
      }
    }
    res.close();
    return mChapters;
  }

  public ArrayList<DownloadData> getPendingTeaching() {
    ArrayList<DownloadData> mChapters = new ArrayList<>();
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor res = db.rawQuery("Select * from " + TABLE_TEACHING + " WHERE " + DOWNLOAD_ID + "=? AND " + AUDIO_PATH + "= ?", new String[]{"", ""});
    if (res != null && res.moveToFirst()) {
      while (!res.isAfterLast()) {
        mChapters.add(new DownloadData(0L, res.getString(4).toLowerCase(Locale.getDefault()), Constants.TEACHING, Constants.TEACHING_URL + res.getString(1), "", res.getString(14), res.getString(16), res.getString(1),  Constants.PENDING));
        res.moveToNext();
      }
    }
    res.close();
    return mChapters;
  }

  public ArrayList<Teaching> getTeachingOnly() {
    ArrayList<Teaching> mTeaching = new ArrayList<>();
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor res = db.rawQuery("Select * from " + TABLE_VERSES, null);
    if (res != null && res.moveToFirst()) {
      while (!res.isAfterLast()) {
        mTeaching.add(new Teaching(res.getString(1), res.getString(2), res.getString(9), res.getString(10), res.getString(11), res.getString(12), res.getString(13), res.getString(14), res.getString(15)));
        res.moveToNext();
      }
    }
    res.close();
    return mTeaching;
  }

  public BookOnly getAllBooks() {
    BookOnly mBook;
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor res = db.rawQuery("Select * from " + TABLE_BOOK, null);
    res.moveToFirst();
    mBook = new BookOnly(res.getString(1), res.getString(2), res.getString(3), res.getString(4), Integer.parseInt(res.getString(5)), res.getString(6), res.getString(7), res.getString(8), Integer.parseInt(res.getString(9)), res.getString(10), res.getString(11));
    res.close();
    return mBook;
  }

  public Book getBook(String book) {
    String mBookId = book.substring(0, 1).toUpperCase() + book.substring(1);

    SQLiteDatabase db = this.getWritableDatabase();

    Cursor cursor = db.rawQuery("Select * from " + CHAPTER_ID, null);
    cursor.moveToFirst();

//        SQLiteDatabase db = this.getReadableDatabase();
    Cursor res = db.rawQuery("select * from " + TABLE_BOOK + " WHERE " + BOOK_ID + "=?", new String[]{mBookId});
    if (res != null && res.moveToFirst()) {
      int totalChap = Integer.parseInt(res.getString(5));
      ArrayList<Chapters> mChapList = new ArrayList<>();
      for (int i = 1; i <= totalChap; i++) {
        ArrayList<Verse> mVerseList = new ArrayList<>();

        db = this.getReadableDatabase();
        res = db.rawQuery("select * from " + TABLE_VERSES + " WHERE " + CHAPTER_ID + "=?", new String[]{i + ""});
        res.moveToFirst();
        while (!res.isAfterLast()) {
          mVerseList.add(new Verse(res.getString(1), res.getString(2), res.getString(3), res.getString(4), res.getString(5), res.getString(6), res.getString(7), res.getString(8), res.getString(9), res.getString(10), res.getString(11), res.getString(12), res.getString(13), res.getString(14), res.getString(15), res.getString(16), res.getString(17), res.getString(18)));
          res.moveToNext();
        }

        db = this.getReadableDatabase();
        res = db.rawQuery("select * from " + CHAPTER_ID + " WHERE " + CHAPTER_ID + "=?", new String[]{i + ""});
        res.moveToFirst();
        while (!res.isAfterLast()) {
          mChapList.add(new Chapters(res.getString(1), res.getString(2), res.getString(3), mVerseList));
          res.moveToNext();
        }
      }

//            ArrayList<Book> mBookList = new ArrayList<>();
      Book mBook = null;
      db = this.getReadableDatabase();
      res = db.rawQuery("select * from " + TABLE_BOOK + " WHERE " + BOOK_ID + "=?", new String[]{mBookId});
      res.moveToFirst();
      while (!res.isAfterLast()) {
        mBook = new Book(res.getString(1), res.getString(2), res.getString(3), res.getString(4), res.getString(5), res.getString(6), res.getString(7), mChapList);
//            mBookList.add(new Book(res.getString(1), res.getString(2), res.getString(3), res.getString(4), res.getString(5), res.getString(6), res.getString(7), mChapList));
        res.moveToNext();
      }
      res.close();
      return mBook;
    } else {
      return null;
    }
  }

  public String getBookName() {
    String columnValue = "";

    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("Select " + NAME + " from " + TABLE_BOOK, null);
    if (cursor != null) {
      try {
        if (cursor.moveToFirst()) {
          columnValue = cursor.getString(cursor.getColumnIndex(NAME));
          return columnValue;
        }
      } finally {
        cursor.close();
      }
    }

    db.close();
    return columnValue;
  }

  public String getReference(String chapterNumber) {
    String columnValue = "";

    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("Select " + REFERENCE + " from " + TABLE_AUDIO + " WHERE " + NUMBER + "=?", new String[]{chapterNumber});
    if (cursor != null) {
      try {
        if (cursor.moveToFirst()) {
          columnValue = cursor.getString(cursor.getColumnIndex(REFERENCE));
          return columnValue;
        }
      } finally {
        cursor.close();
      }
    }

    db.close();
    return columnValue;
  }

  public String getTeachingUrl(String uuid, String chapterNumber) {
    String columnValue = Constants.TEACHING_URL;

    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("Select " + UUID + " from " + TABLE_TEACHING + " WHERE " + CHAPTERNUMBER + "=? AND " + UUID + "= ?", new String[]{chapterNumber, uuid});
    if (cursor != null) {
      try {
        if (cursor.moveToFirst()) {
          columnValue += cursor.getString(cursor.getColumnIndex(UUID));
          return columnValue;
        }
      } finally {
        cursor.close();
      }
    }

    db.close();
    return columnValue;
  }

  public String getChapterUrl(String chapterNumber) {
    String columnValue = "";

    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery("Select " + URL + " from " + TABLE_AUDIO + " WHERE " + NUMBER + "=?", new String[]{chapterNumber});
    if (cursor != null) {
      try {
        if (cursor.moveToFirst()) {
          columnValue = cursor.getString(cursor.getColumnIndex(URL));
          return columnValue;
        }
      } finally {
        cursor.close();
      }
    }

    db.close();
    return columnValue;
  }

  public ArrayList<URLData> getAllNotDownloadedUrl(String file_type) {
    ArrayList<URLData> mUrlList = new ArrayList<>();

    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor;
    String query = null;

    switch (file_type) {
      case Constants.CHAPTER:
        query = "SELECT " + URL + ", " + NUMBER + " FROM " + TABLE_AUDIO + " WHERE " + AUDIO_PATH + " == NULL OR " + AUDIO_PATH + " == ''";
        break;
      case Constants.TEACHING:
        query = "SELECT " + UUID + ", " + UUID + " FROM " + TABLE_TEACHING + " WHERE " + AUDIO_PATH + " == NULL OR " + AUDIO_PATH + " == ''";
        break;
    }

    cursor = db.rawQuery(query, null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          mUrlList.add(new URLData(((file_type.equals(Constants.TEACHING)) ? Constants.TEACHING_URL : "" ) + cursor.getString(0), cursor.getString(1)));
          cursor.moveToNext();
        }
      }
      cursor.close();
    }
    db.close();
    return mUrlList;
  }

  public BookDownload getBookDownloaded() {
    ArrayList<BookDownload.Chapter> mChapList = new ArrayList<>();
    ArrayList<BookDownload.Teaching> mTeachList = new ArrayList<>();

    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor;
    String query = null;

    query = "SELECT " + NUMBER + ", " + AUDIO_PATH + " FROM " + TABLE_AUDIO + " WHERE " + AUDIO_PATH + " != NULL OR " + AUDIO_PATH + " != ''";

    cursor = db.rawQuery(query, null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          mChapList.add(new BookDownload.Chapter(cursor.getString(0), cursor.getString(1)));
          cursor.moveToNext();
        }
      }
      cursor.close();
    }
    query = "SELECT " + UUID + ", " + NAME +  ", " + DESCRIPTION +  ", " + AUDIO_DURATION +  ", " + MIME_TYPE +  ", " + SCHEDULED_DATE +  ", " + AUDIO_PATH + " FROM " + TABLE_TEACHING + " WHERE " + AUDIO_PATH + " != NULL OR " + AUDIO_PATH + " != ''";
    cursor = db.rawQuery(query, null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          mTeachList.add(new BookDownload.Teaching(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)));
          cursor.moveToNext();
        }
      }
      cursor.close();
    }
    db.close();
    return new BookDownload(mChapList, mTeachList);
  }
  public ArrayList<Chapters> getChapters(String chapter) {
    ArrayList<Chapters> mChapList = new ArrayList<>();

    SQLiteDatabase db = this.getReadableDatabase();
    Cursor res = db.rawQuery("select * from " + CHAPTER_ID + " WHERE " + CHAPTER_ID + "=?", new String[]{chapter});
    res.moveToFirst();
    while (!res.isAfterLast()) {
      mChapList.add(new Chapters(res.getString(1), res.getString(2), res.getString(3), null));
      res.moveToNext();
    }
    res.close();
    return mChapList;
  }

  public ArrayList<?> getVerses(String bibleId, String chapter) {
    ArrayList<MVerse> mVerseList = new ArrayList<>();
    ArrayList<MNVerse> mNVerseList = new ArrayList<>();

    SQLiteDatabase db = this.getReadableDatabase();
    Cursor verses = db.rawQuery("select * from " + TABLE_VERSES + " WHERE " + BIBLE_ID + "=?" + " AND " + CHAPTERNUMBER + "=?", new String[]{bibleId, chapter});
    verses.moveToFirst();
    while (!verses.isAfterLast()) {
      if (!verses.getString(9).equals("")) {
        Cursor teaching = db.rawQuery("select * from " + TABLE_TEACHING + " WHERE " + UUID + "=?", new String[]{verses.getString(9)});
        teaching.moveToFirst();
        mVerseList.add(new MVerse(verses.getString(1), verses.getString(2), verses.getString(3), verses.getString(4), verses.getString(5), verses.getString(6), verses.getString(7), Integer.parseInt(verses.getString(8)), new MVerse.Teachings(teaching.getString(1), Integer.parseInt(teaching.getString(2)), teaching.getString(3), teaching.getString(4), Integer.parseInt(teaching.getString(5)), Integer.parseInt(teaching.getString(6)), Integer.parseInt(teaching.getString(7)), Integer.parseInt(teaching.getString(8)), teaching.getString(9), teaching.getString(10), teaching.getString(11), teaching.getString(12), teaching.getString(13), teaching.getString(14), teaching.getString(15))));
      } else {
        mNVerseList.add(new MNVerse(verses.getString(1), verses.getString(2), verses.getString(3), verses.getString(4), verses.getString(5), verses.getString(6), verses.getString(7), Integer.parseInt(verses.getString(8))));
      }
      verses.moveToNext();
    }
    verses.close();
    if (!mVerseList.isEmpty()) {
      return mVerseList;
    } else if (!mNVerseList.isEmpty()) {
      return mNVerseList;
    } else {
      return mVerseList;
    }
  }

  public ArrayList<MVerse.Teachings> getTeachings() {
    ArrayList<MVerse.Teachings> mVerseList = new ArrayList<>();

    SQLiteDatabase db = this.getReadableDatabase();
    Cursor teaching = db.rawQuery("select DISTINCT * from " + TABLE_TEACHING, null);
    teaching.moveToFirst();
    while (!teaching.isAfterLast()) {
      mVerseList.add(new MVerse.Teachings(teaching.getString(1), Integer.parseInt(teaching.getString(2)), teaching.getString(3), teaching.getString(4), Integer.parseInt(teaching.getString(5)), Integer.parseInt(teaching.getString(6)), Integer.parseInt(teaching.getString(7)), Integer.parseInt(teaching.getString(8)), teaching.getString(9), teaching.getString(10), teaching.getString(11), teaching.getString(12), teaching.getString(13), teaching.getString(14), teaching.getString(15)));
      teaching.moveToNext();
    }

    teaching.close();
    return mVerseList;
  }

  public ArrayList<MVerse.Teachings> getTeachings(String verseNumber, String chapterNumber) {
    ArrayList<MVerse.Teachings> mVerseList = new ArrayList<>();

    SQLiteDatabase db = this.getReadableDatabase();
    Cursor verse = db.rawQuery("select DISTINCT * from " + TABLE_VERSES + " WHERE " + VERSENUMBER + "=?" + " AND " + CHAPTERNUMBER + "=?", new String[]{verseNumber, chapterNumber});
    verse.moveToFirst();
    Cursor teaching = db.rawQuery("select * from " + TABLE_TEACHING + " WHERE " + UUID + "=?", new String[]{verse.getString(9)});
    teaching.moveToFirst();
    while (!teaching.isAfterLast()) {
      mVerseList.add(new MVerse.Teachings(teaching.getString(1), Integer.parseInt(teaching.getString(2)), teaching.getString(3), teaching.getString(4), Integer.parseInt(teaching.getString(5)), Integer.parseInt(teaching.getString(6)), Integer.parseInt(teaching.getString(7)), Integer.parseInt(teaching.getString(8)), teaching.getString(9), teaching.getString(10), teaching.getString(11), teaching.getString(12), teaching.getString(13), teaching.getString(14), teaching.getString(15)));
      teaching.moveToNext();
    }

    teaching.close();
    return mVerseList;
  }
}
