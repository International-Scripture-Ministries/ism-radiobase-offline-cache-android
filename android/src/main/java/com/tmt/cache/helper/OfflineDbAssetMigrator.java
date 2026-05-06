package com.tmt.cache.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class OfflineDbAssetMigrator {
  private static final String TAG = "OfflineDbMigration";
  private static final String MIGRATION_PREFS = "offline_db_migration";
  private static final String OFFLINE_DB_ASSET_VERSION = "offline_db_asset_version";
  private static final Object REFRESH_LOCK = new Object();
  private static Future<?> refreshFuture;

  private static final List<String> BOOKS_ID = Arrays.asList(
    "gdl", "1ch", "1co", "1jn", "1ki", "1pe", "1sa", "1th", "1ti",
    "2ch", "2co", "2jn", "2ki", "2pe", "2sa", "2th", "2ti", "3jn",
    "act", "amo", "col", "dan", "deu", "ecc", "eph", "est", "exo",
    "ezk", "ezr", "gal", "gen", "hab", "hag", "heb", "hos", "isa",
    "jas", "jdg", "jer", "jhn", "job", "joel", "jon", "jos", "jud",
    "lam", "lev", "luk", "mal", "mat", "mic", "mrk", "nam", "neh",
    "num", "oba", "phm", "php", "pro", "psa", "rev", "rom", "rut",
    "sng", "tit", "zec", "zep"
  );

  private final Context context;

  public OfflineDbAssetMigrator(Context context) {
    this.context = context.getApplicationContext();
  }

  public void refreshInBackground() {
    synchronized (REFRESH_LOCK) {
      if (refreshFuture != null && !refreshFuture.isDone()) {
        return;
      }

      ExecutorService executor = Executors.newSingleThreadExecutor();
      refreshFuture = executor.submit(() -> {
        try {
          refreshOfflineDatabases();
        } catch (Exception e) {
          Log.e(TAG, "Offline database refresh failed", e);
        } finally {
          executor.shutdown();
        }
      });
    }
  }

  public static void awaitRefresh() {
    Future<?> future;
    synchronized (REFRESH_LOCK) {
      future = refreshFuture;
    }

    if (future == null || future.isDone()) {
      return;
    }

    try {
      future.get();
    } catch (Exception e) {
      Log.e(TAG, "Unable to wait for offline database refresh", e);
    }
  }

  private void refreshOfflineDatabases() throws IOException {
    File databaseDirectory = context.getDatabasePath(BOOKS_ID.get(0)).getParentFile();
    if (databaseDirectory != null && !databaseDirectory.exists() && !databaseDirectory.mkdirs()) {
      throw new IOException("Unable to create database directory: " + databaseDirectory.getAbsolutePath());
    }

    long currentAssetVersion = getCurrentVersionCode();
    SharedPreferences preferences = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE);
    long migratedAssetVersion = preferences.getLong(OFFLINE_DB_ASSET_VERSION, -1L);
    boolean shouldRefreshPackagedContent = migratedAssetVersion != currentAssetVersion;

    for (String dbName : BOOKS_ID) {
      File databaseFile = context.getDatabasePath(dbName);
      if (shouldRefreshPackagedContent || !databaseFile.exists()) {
        refreshSingleDatabase(dbName, databaseFile);
      }
    }

    preferences.edit().putLong(OFFLINE_DB_ASSET_VERSION, currentAssetVersion).apply();
  }

  private long getCurrentVersionCode() {
    try {
      PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return packageInfo.getLongVersionCode();
      }
      return packageInfo.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      Log.w(TAG, "Unable to read app version code", e);
      return -1L;
    }
  }

  private void refreshSingleDatabase(String dbName, File databaseFile) throws IOException {
    PreservedDownloadState preservedState = databaseFile.exists()
      ? readPreservedDownloadState(databaseFile)
      : new PreservedDownloadState();

    File tempFile = new File(databaseFile.getAbsolutePath() + ".tmp");
    File backupFile = new File(databaseFile.getAbsolutePath() + ".bak");

    deleteIfExists(tempFile);
    deleteIfExists(backupFile);

    copyAssetToFile(dbName, tempFile);
    applyPreservedDownloadState(tempFile, preservedState);
    replaceDatabaseFile(databaseFile, tempFile, backupFile);
  }

  private PreservedDownloadState readPreservedDownloadState(File databaseFile) {
    PreservedDownloadState preservedState = new PreservedDownloadState();
    SQLiteDatabase database = null;
    try {
      database = SQLiteDatabase.openDatabase(
        databaseFile.getAbsolutePath(),
        null,
        SQLiteDatabase.OPEN_READONLY
      );
      readAudioDownloadState(database, preservedState);
      readTeachingDownloadState(database, preservedState);
    } catch (Exception e) {
      Log.w(TAG, "Unable to read preserved state from " + databaseFile.getName(), e);
    } finally {
      if (database != null && database.isOpen()) {
        database.close();
      }
    }
    return preservedState;
  }

  private void readAudioDownloadState(SQLiteDatabase database, PreservedDownloadState preservedState) {
    if (!tableExists(database, DBHelper.TABLE_AUDIO)) {
      return;
    }

    boolean hasDownloadId = columnExists(database, DBHelper.TABLE_AUDIO, DBHelper.DOWNLOAD_ID);
    String downloadIdColumn = hasDownloadId ? DBHelper.DOWNLOAD_ID : "NULL AS " + DBHelper.DOWNLOAD_ID;
    String downloadIdPredicate = hasDownloadId
      ? " OR (" + DBHelper.DOWNLOAD_ID + " IS NOT NULL AND " + DBHelper.DOWNLOAD_ID + " != '')"
      : "";
    String query = "SELECT " + DBHelper.BOOK_ID + ", " + DBHelper.NUMBER + ", "
      + DBHelper.AUDIO_PATH + ", " + downloadIdColumn
      + " FROM " + DBHelper.TABLE_AUDIO
      + " WHERE (" + DBHelper.AUDIO_PATH + " IS NOT NULL AND " + DBHelper.AUDIO_PATH + " != '')"
      + downloadIdPredicate;

    Cursor cursor = null;
    try {
      cursor = database.rawQuery(query, null);
      while (cursor.moveToNext()) {
        String bookId = cursor.getString(0);
        String number = cursor.getString(1);
        if (!hasUsefulValue(bookId) || !hasUsefulValue(number)) {
          continue;
        }
        preservedState.audioRows.add(new AudioDownloadState(
          bookId,
          number,
          cursor.getString(2),
          cursor.getString(3)
        ));
      }
    } catch (Exception e) {
      Log.w(TAG, "Unable to read AUDIO download state", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private void readTeachingDownloadState(SQLiteDatabase database, PreservedDownloadState preservedState) {
    if (!tableExists(database, DBHelper.TABLE_TEACHING)) {
      return;
    }

    boolean hasDownloadId = columnExists(database, DBHelper.TABLE_TEACHING, DBHelper.DOWNLOAD_ID);
    String downloadIdColumn = hasDownloadId ? DBHelper.DOWNLOAD_ID : "NULL AS " + DBHelper.DOWNLOAD_ID;
    String downloadIdPredicate = hasDownloadId
      ? " OR (" + DBHelper.DOWNLOAD_ID + " IS NOT NULL AND " + DBHelper.DOWNLOAD_ID + " != '')"
      : "";
    String query = "SELECT " + DBHelper.UUID + ", " + DBHelper.AUDIO_PATH + ", " + downloadIdColumn
      + " FROM " + DBHelper.TABLE_TEACHING
      + " WHERE (" + DBHelper.AUDIO_PATH + " IS NOT NULL AND " + DBHelper.AUDIO_PATH + " != '')"
      + downloadIdPredicate;

    Cursor cursor = null;
    try {
      cursor = database.rawQuery(query, null);
      while (cursor.moveToNext()) {
        String uuid = cursor.getString(0);
        if (!hasUsefulValue(uuid)) {
          continue;
        }
        preservedState.teachingRows.add(new TeachingDownloadState(
          uuid,
          cursor.getString(1),
          cursor.getString(2)
        ));
      }
    } catch (Exception e) {
      Log.w(TAG, "Unable to read TEACHING download state", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private void applyPreservedDownloadState(File databaseFile, PreservedDownloadState preservedState) {
    if (preservedState.audioRows.isEmpty() && preservedState.teachingRows.isEmpty()) {
      return;
    }

    SQLiteDatabase database = null;
    try {
      database = SQLiteDatabase.openDatabase(
        databaseFile.getAbsolutePath(),
        null,
        SQLiteDatabase.OPEN_READWRITE
      );
      database.beginTransaction();

      applyAudioDownloadState(database, preservedState.audioRows);
      applyTeachingDownloadState(database, preservedState.teachingRows);

      database.setTransactionSuccessful();
    } catch (Exception e) {
      throw new RuntimeException("Unable to apply preserved download state to " + databaseFile.getName(), e);
    } finally {
      if (database != null) {
        if (database.inTransaction()) {
          database.endTransaction();
        }
        if (database.isOpen()) {
          database.close();
        }
      }
    }
  }

  private void applyAudioDownloadState(SQLiteDatabase database, List<AudioDownloadState> rows) {
    if (rows.isEmpty() || !tableExists(database, DBHelper.TABLE_AUDIO)) {
      return;
    }

    boolean hasDownloadId = columnExists(database, DBHelper.TABLE_AUDIO, DBHelper.DOWNLOAD_ID);
    SQLiteStatement audioPathStatement = database.compileStatement(
      "UPDATE " + DBHelper.TABLE_AUDIO + " SET " + DBHelper.AUDIO_PATH
        + " = ? WHERE " + DBHelper.BOOK_ID + " = ? AND " + DBHelper.NUMBER + " = ?"
    );
    SQLiteStatement downloadIdStatement = hasDownloadId
      ? database.compileStatement(
        "UPDATE " + DBHelper.TABLE_AUDIO + " SET " + DBHelper.DOWNLOAD_ID
          + " = ? WHERE " + DBHelper.BOOK_ID + " = ? AND " + DBHelper.NUMBER + " = ?"
      )
      : null;

    try {
      for (AudioDownloadState row : rows) {
        if (hasUsefulValue(row.audioPath)) {
          bindAndExecute(audioPathStatement, row.audioPath, row.bookId, row.number);
        }
        if (downloadIdStatement != null && hasUsefulValue(row.downloadId)) {
          bindAndExecute(downloadIdStatement, row.downloadId, row.bookId, row.number);
        }
      }
    } finally {
      audioPathStatement.close();
      if (downloadIdStatement != null) {
        downloadIdStatement.close();
      }
    }
  }

  private void applyTeachingDownloadState(SQLiteDatabase database, List<TeachingDownloadState> rows) {
    if (rows.isEmpty() || !tableExists(database, DBHelper.TABLE_TEACHING)) {
      return;
    }

    boolean hasDownloadId = columnExists(database, DBHelper.TABLE_TEACHING, DBHelper.DOWNLOAD_ID);
    SQLiteStatement audioPathStatement = database.compileStatement(
      "UPDATE " + DBHelper.TABLE_TEACHING + " SET " + DBHelper.AUDIO_PATH
        + " = ? WHERE " + DBHelper.UUID + " = ?"
    );
    SQLiteStatement downloadIdStatement = hasDownloadId
      ? database.compileStatement(
        "UPDATE " + DBHelper.TABLE_TEACHING + " SET " + DBHelper.DOWNLOAD_ID
          + " = ? WHERE " + DBHelper.UUID + " = ?"
      )
      : null;

    try {
      for (TeachingDownloadState row : rows) {
        if (hasUsefulValue(row.audioPath)) {
          bindAndExecute(audioPathStatement, row.audioPath, row.uuid);
        }
        if (downloadIdStatement != null && hasUsefulValue(row.downloadId)) {
          bindAndExecute(downloadIdStatement, row.downloadId, row.uuid);
        }
      }
    } finally {
      audioPathStatement.close();
      if (downloadIdStatement != null) {
        downloadIdStatement.close();
      }
    }
  }

  private void bindAndExecute(SQLiteStatement statement, String value, String firstKey, String secondKey) {
    statement.clearBindings();
    statement.bindString(1, value);
    statement.bindString(2, firstKey);
    statement.bindString(3, secondKey);
    statement.executeUpdateDelete();
  }

  private void bindAndExecute(SQLiteStatement statement, String value, String key) {
    statement.clearBindings();
    statement.bindString(1, value);
    statement.bindString(2, key);
    statement.executeUpdateDelete();
  }

  private boolean tableExists(SQLiteDatabase database, String tableName) {
    Cursor cursor = null;
    try {
      cursor = database.rawQuery(
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
        new String[]{tableName}
      );
      return cursor.moveToFirst();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private boolean columnExists(SQLiteDatabase database, String tableName, String columnName) {
    Cursor cursor = null;
    try {
      cursor = database.rawQuery("PRAGMA table_info(" + tableName + ")", null);
      while (cursor.moveToNext()) {
        if (columnName.equalsIgnoreCase(cursor.getString(1))) {
          return true;
        }
      }
      return false;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private boolean hasUsefulValue(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private void copyAssetToFile(String assetName, File targetFile) throws IOException {
    File directory = targetFile.getParentFile();
    if (directory != null && !directory.exists() && !directory.mkdirs()) {
      throw new IOException("Unable to create database directory: " + directory.getAbsolutePath());
    }

    AssetManager assetManager = context.getAssets();
    try (
      InputStream input = assetManager.open(assetName);
      OutputStream output = new FileOutputStream(targetFile)
    ) {
      byte[] buffer = new byte[8192];
      int length;
      while ((length = input.read(buffer)) != -1) {
        output.write(buffer, 0, length);
      }
      output.flush();
    }
  }

  private void replaceDatabaseFile(File databaseFile, File tempFile, File backupFile) throws IOException {
    boolean hasBackup = false;

    if (databaseFile.exists()) {
      if (!databaseFile.renameTo(backupFile)) {
        throw new IOException("Unable to backup database: " + databaseFile.getName());
      }
      hasBackup = true;
    }

    if (!tempFile.renameTo(databaseFile)) {
      if (hasBackup && backupFile.exists() && !backupFile.renameTo(databaseFile)) {
        Log.e(TAG, "Unable to restore database backup: " + databaseFile.getName());
      }
      throw new IOException("Unable to replace database: " + databaseFile.getName());
    }

    if (hasBackup) {
      deleteIfExists(backupFile);
    }
  }

  private void deleteIfExists(File file) throws IOException {
    if (file.exists() && !file.delete()) {
      throw new IOException("Unable to delete file: " + file.getAbsolutePath());
    }
  }

  private static class PreservedDownloadState {
    final ArrayList<AudioDownloadState> audioRows = new ArrayList<>();
    final ArrayList<TeachingDownloadState> teachingRows = new ArrayList<>();
  }

  private static class AudioDownloadState {
    final String bookId;
    final String number;
    final String audioPath;
    final String downloadId;

    AudioDownloadState(String bookId, String number, String audioPath, String downloadId) {
      this.bookId = bookId;
      this.number = number;
      this.audioPath = audioPath;
      this.downloadId = downloadId;
    }
  }

  private static class TeachingDownloadState {
    final String uuid;
    final String audioPath;
    final String downloadId;

    TeachingDownloadState(String uuid, String audioPath, String downloadId) {
      this.uuid = uuid;
      this.audioPath = audioPath;
      this.downloadId = downloadId;
    }
  }
}
