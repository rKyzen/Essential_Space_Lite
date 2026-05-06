package com.essential.spacelite.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.essential.spacelite.data.entity.CaptureEntry;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CaptureEntryDao_Impl implements CaptureEntryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CaptureEntry> __insertionAdapterOfCaptureEntry;

  private final EntityDeletionOrUpdateAdapter<CaptureEntry> __deletionAdapterOfCaptureEntry;

  private final EntityDeletionOrUpdateAdapter<CaptureEntry> __updateAdapterOfCaptureEntry;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public CaptureEntryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCaptureEntry = new EntityInsertionAdapter<CaptureEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `capture_entries` (`id`,`screenshot_path`,`thumbnail_path`,`text_note`,`voice_note_path`,`voice_note_duration_ms`,`timestamp`,`reminder_at`,`ai_summary`,`app_name`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CaptureEntry entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getScreenshotPath() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getScreenshotPath());
        }
        if (entity.getThumbnailPath() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getThumbnailPath());
        }
        if (entity.getTextNote() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTextNote());
        }
        if (entity.getVoiceNotePath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getVoiceNotePath());
        }
        statement.bindLong(6, entity.getVoiceNoteDurationMs());
        statement.bindLong(7, entity.getTimestamp());
        if (entity.getReminderAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getReminderAt());
        }
        if (entity.getAiSummary() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getAiSummary());
        }
        if (entity.getAppName() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getAppName());
        }
      }
    };
    this.__deletionAdapterOfCaptureEntry = new EntityDeletionOrUpdateAdapter<CaptureEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `capture_entries` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CaptureEntry entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfCaptureEntry = new EntityDeletionOrUpdateAdapter<CaptureEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `capture_entries` SET `id` = ?,`screenshot_path` = ?,`thumbnail_path` = ?,`text_note` = ?,`voice_note_path` = ?,`voice_note_duration_ms` = ?,`timestamp` = ?,`reminder_at` = ?,`ai_summary` = ?,`app_name` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CaptureEntry entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getScreenshotPath() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getScreenshotPath());
        }
        if (entity.getThumbnailPath() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getThumbnailPath());
        }
        if (entity.getTextNote() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTextNote());
        }
        if (entity.getVoiceNotePath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getVoiceNotePath());
        }
        statement.bindLong(6, entity.getVoiceNoteDurationMs());
        statement.bindLong(7, entity.getTimestamp());
        if (entity.getReminderAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getReminderAt());
        }
        if (entity.getAiSummary() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getAiSummary());
        }
        if (entity.getAppName() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getAppName());
        }
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM capture_entries WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM capture_entries";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final CaptureEntry entry, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCaptureEntry.insertAndReturnId(entry);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final CaptureEntry entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCaptureEntry.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final CaptureEntry entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCaptureEntry.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CaptureEntry>> getAllEntries() {
    final String _sql = "SELECT * FROM capture_entries ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"capture_entries"}, new Callable<List<CaptureEntry>>() {
      @Override
      @NonNull
      public List<CaptureEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfScreenshotPath = CursorUtil.getColumnIndexOrThrow(_cursor, "screenshot_path");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_path");
          final int _cursorIndexOfTextNote = CursorUtil.getColumnIndexOrThrow(_cursor, "text_note");
          final int _cursorIndexOfVoiceNotePath = CursorUtil.getColumnIndexOrThrow(_cursor, "voice_note_path");
          final int _cursorIndexOfVoiceNoteDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "voice_note_duration_ms");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfReminderAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_at");
          final int _cursorIndexOfAiSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "ai_summary");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "app_name");
          final List<CaptureEntry> _result = new ArrayList<CaptureEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CaptureEntry _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpScreenshotPath;
            if (_cursor.isNull(_cursorIndexOfScreenshotPath)) {
              _tmpScreenshotPath = null;
            } else {
              _tmpScreenshotPath = _cursor.getString(_cursorIndexOfScreenshotPath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final String _tmpTextNote;
            if (_cursor.isNull(_cursorIndexOfTextNote)) {
              _tmpTextNote = null;
            } else {
              _tmpTextNote = _cursor.getString(_cursorIndexOfTextNote);
            }
            final String _tmpVoiceNotePath;
            if (_cursor.isNull(_cursorIndexOfVoiceNotePath)) {
              _tmpVoiceNotePath = null;
            } else {
              _tmpVoiceNotePath = _cursor.getString(_cursorIndexOfVoiceNotePath);
            }
            final long _tmpVoiceNoteDurationMs;
            _tmpVoiceNoteDurationMs = _cursor.getLong(_cursorIndexOfVoiceNoteDurationMs);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Long _tmpReminderAt;
            if (_cursor.isNull(_cursorIndexOfReminderAt)) {
              _tmpReminderAt = null;
            } else {
              _tmpReminderAt = _cursor.getLong(_cursorIndexOfReminderAt);
            }
            final String _tmpAiSummary;
            if (_cursor.isNull(_cursorIndexOfAiSummary)) {
              _tmpAiSummary = null;
            } else {
              _tmpAiSummary = _cursor.getString(_cursorIndexOfAiSummary);
            }
            final String _tmpAppName;
            if (_cursor.isNull(_cursorIndexOfAppName)) {
              _tmpAppName = null;
            } else {
              _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            }
            _item = new CaptureEntry(_tmpId,_tmpScreenshotPath,_tmpThumbnailPath,_tmpTextNote,_tmpVoiceNotePath,_tmpVoiceNoteDurationMs,_tmpTimestamp,_tmpReminderAt,_tmpAiSummary,_tmpAppName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllEntriesSnapshot(final Continuation<? super List<CaptureEntry>> $completion) {
    final String _sql = "SELECT * FROM capture_entries ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CaptureEntry>>() {
      @Override
      @NonNull
      public List<CaptureEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfScreenshotPath = CursorUtil.getColumnIndexOrThrow(_cursor, "screenshot_path");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_path");
          final int _cursorIndexOfTextNote = CursorUtil.getColumnIndexOrThrow(_cursor, "text_note");
          final int _cursorIndexOfVoiceNotePath = CursorUtil.getColumnIndexOrThrow(_cursor, "voice_note_path");
          final int _cursorIndexOfVoiceNoteDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "voice_note_duration_ms");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfReminderAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_at");
          final int _cursorIndexOfAiSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "ai_summary");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "app_name");
          final List<CaptureEntry> _result = new ArrayList<CaptureEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CaptureEntry _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpScreenshotPath;
            if (_cursor.isNull(_cursorIndexOfScreenshotPath)) {
              _tmpScreenshotPath = null;
            } else {
              _tmpScreenshotPath = _cursor.getString(_cursorIndexOfScreenshotPath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final String _tmpTextNote;
            if (_cursor.isNull(_cursorIndexOfTextNote)) {
              _tmpTextNote = null;
            } else {
              _tmpTextNote = _cursor.getString(_cursorIndexOfTextNote);
            }
            final String _tmpVoiceNotePath;
            if (_cursor.isNull(_cursorIndexOfVoiceNotePath)) {
              _tmpVoiceNotePath = null;
            } else {
              _tmpVoiceNotePath = _cursor.getString(_cursorIndexOfVoiceNotePath);
            }
            final long _tmpVoiceNoteDurationMs;
            _tmpVoiceNoteDurationMs = _cursor.getLong(_cursorIndexOfVoiceNoteDurationMs);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Long _tmpReminderAt;
            if (_cursor.isNull(_cursorIndexOfReminderAt)) {
              _tmpReminderAt = null;
            } else {
              _tmpReminderAt = _cursor.getLong(_cursorIndexOfReminderAt);
            }
            final String _tmpAiSummary;
            if (_cursor.isNull(_cursorIndexOfAiSummary)) {
              _tmpAiSummary = null;
            } else {
              _tmpAiSummary = _cursor.getString(_cursorIndexOfAiSummary);
            }
            final String _tmpAppName;
            if (_cursor.isNull(_cursorIndexOfAppName)) {
              _tmpAppName = null;
            } else {
              _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            }
            _item = new CaptureEntry(_tmpId,_tmpScreenshotPath,_tmpThumbnailPath,_tmpTextNote,_tmpVoiceNotePath,_tmpVoiceNoteDurationMs,_tmpTimestamp,_tmpReminderAt,_tmpAiSummary,_tmpAppName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getEntryById(final long id, final Continuation<? super CaptureEntry> $completion) {
    final String _sql = "SELECT * FROM capture_entries WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CaptureEntry>() {
      @Override
      @Nullable
      public CaptureEntry call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfScreenshotPath = CursorUtil.getColumnIndexOrThrow(_cursor, "screenshot_path");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_path");
          final int _cursorIndexOfTextNote = CursorUtil.getColumnIndexOrThrow(_cursor, "text_note");
          final int _cursorIndexOfVoiceNotePath = CursorUtil.getColumnIndexOrThrow(_cursor, "voice_note_path");
          final int _cursorIndexOfVoiceNoteDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "voice_note_duration_ms");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfReminderAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reminder_at");
          final int _cursorIndexOfAiSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "ai_summary");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "app_name");
          final CaptureEntry _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpScreenshotPath;
            if (_cursor.isNull(_cursorIndexOfScreenshotPath)) {
              _tmpScreenshotPath = null;
            } else {
              _tmpScreenshotPath = _cursor.getString(_cursorIndexOfScreenshotPath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final String _tmpTextNote;
            if (_cursor.isNull(_cursorIndexOfTextNote)) {
              _tmpTextNote = null;
            } else {
              _tmpTextNote = _cursor.getString(_cursorIndexOfTextNote);
            }
            final String _tmpVoiceNotePath;
            if (_cursor.isNull(_cursorIndexOfVoiceNotePath)) {
              _tmpVoiceNotePath = null;
            } else {
              _tmpVoiceNotePath = _cursor.getString(_cursorIndexOfVoiceNotePath);
            }
            final long _tmpVoiceNoteDurationMs;
            _tmpVoiceNoteDurationMs = _cursor.getLong(_cursorIndexOfVoiceNoteDurationMs);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Long _tmpReminderAt;
            if (_cursor.isNull(_cursorIndexOfReminderAt)) {
              _tmpReminderAt = null;
            } else {
              _tmpReminderAt = _cursor.getLong(_cursorIndexOfReminderAt);
            }
            final String _tmpAiSummary;
            if (_cursor.isNull(_cursorIndexOfAiSummary)) {
              _tmpAiSummary = null;
            } else {
              _tmpAiSummary = _cursor.getString(_cursorIndexOfAiSummary);
            }
            final String _tmpAppName;
            if (_cursor.isNull(_cursorIndexOfAppName)) {
              _tmpAppName = null;
            } else {
              _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            }
            _result = new CaptureEntry(_tmpId,_tmpScreenshotPath,_tmpThumbnailPath,_tmpTextNote,_tmpVoiceNotePath,_tmpVoiceNoteDurationMs,_tmpTimestamp,_tmpReminderAt,_tmpAiSummary,_tmpAppName);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM capture_entries";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
