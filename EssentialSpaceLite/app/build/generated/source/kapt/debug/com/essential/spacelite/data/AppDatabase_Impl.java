package com.essential.spacelite.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.essential.spacelite.data.dao.CaptureEntryDao;
import com.essential.spacelite.data.dao.CaptureEntryDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CaptureEntryDao _captureEntryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `capture_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `screenshot_path` TEXT NOT NULL, `thumbnail_path` TEXT NOT NULL, `text_note` TEXT, `voice_note_path` TEXT, `voice_note_duration_ms` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `reminder_at` INTEGER, `ai_summary` TEXT, `app_name` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7893e019e58b6af26053f41cfdceb312')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `capture_entries`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCaptureEntries = new HashMap<String, TableInfo.Column>(10);
        _columnsCaptureEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaptureEntries.put("screenshot_path", new TableInfo.Column("screenshot_path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaptureEntries.put("thumbnail_path", new TableInfo.Column("thumbnail_path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaptureEntries.put("text_note", new TableInfo.Column("text_note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaptureEntries.put("voice_note_path", new TableInfo.Column("voice_note_path", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaptureEntries.put("voice_note_duration_ms", new TableInfo.Column("voice_note_duration_ms", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaptureEntries.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaptureEntries.put("reminder_at", new TableInfo.Column("reminder_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaptureEntries.put("ai_summary", new TableInfo.Column("ai_summary", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCaptureEntries.put("app_name", new TableInfo.Column("app_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCaptureEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCaptureEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCaptureEntries = new TableInfo("capture_entries", _columnsCaptureEntries, _foreignKeysCaptureEntries, _indicesCaptureEntries);
        final TableInfo _existingCaptureEntries = TableInfo.read(db, "capture_entries");
        if (!_infoCaptureEntries.equals(_existingCaptureEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "capture_entries(com.essential.spacelite.data.entity.CaptureEntry).\n"
                  + " Expected:\n" + _infoCaptureEntries + "\n"
                  + " Found:\n" + _existingCaptureEntries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "7893e019e58b6af26053f41cfdceb312", "ae94910223d61ecce20c37d80629b3f6");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "capture_entries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `capture_entries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CaptureEntryDao.class, CaptureEntryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CaptureEntryDao captureEntryDao() {
    if (_captureEntryDao != null) {
      return _captureEntryDao;
    } else {
      synchronized(this) {
        if(_captureEntryDao == null) {
          _captureEntryDao = new CaptureEntryDao_Impl(this);
        }
        return _captureEntryDao;
      }
    }
  }
}
