package com.tencent.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.ext.concurrent.ThreadPoolManager;
import android.ext.concurrent.ThreadPoolManager.Task;
import android.ext.util.FileUtils;
import android.provider.BaseColumns;
import java.util.concurrent.Executor;

public final class DownloadManager {
    private Executor mExecutor;
    private final DownloadDatabase mDatabase;
    private final ThreadPoolManager mThreadPool;

    public DownloadManager(Context context) {
        mDatabase   = new DownloadDatabase(context.getApplicationContext());
        mThreadPool = new ThreadPoolManager(5);
    }

    public final long execute(Request request) {
        long id = request.id;
        if (id <= 0) {
            id = mDatabase.insert(request.toContentValues());
        } else {
            mDatabase.update(request.toContentValues(), BaseColumns._ID + "=" + id, null);
        }

        mThreadPool.execute(new DownloadTask(request));
        return id;
    }

    public final void cancelAll() {
        mExecutor.execute(() -> {
            mThreadPool.cancelAll(false);
            mDatabase.update(null, null, null);
        });
    }

    public final void cancel(long id) {
        mExecutor.execute(() -> mThreadPool.cancel(id, false));
    }

    public final void remove(long id) {
        mExecutor.execute(() -> {
            mThreadPool.cancel(id, false);
            mDatabase.delete(id, true);
        });
    }

    public final void removeAll() {
        mExecutor.execute(() -> {
            mThreadPool.cancelAll(false);
            mDatabase.deleteAll(null);
        });
    }

    public static final class Request {
        /* package */ long id;

        public Request(long id) {
            this.id = id;
        }

        /* package */ final ContentValues toContentValues() {
            return null;
        }
    }

    private final class DownloadTask extends Task {
        private final Request request;

        public DownloadTask(Request request) {
            this.request = request;
        }

        @Override
        protected long getId() {
            return request.id;
        }

        @Override
        protected void onCompletion() {
        }

        @Override
        protected void doInBackground(Thread thread) {
        }
    }

    private static final class DownloadDatabase extends SQLiteOpenHelper {
        private static final String TABLE_NAME = "downloads";
        private static final int DATABASE_VERSION = 1;

        public DownloadDatabase(Context context) {
            super(context, "downloads.db", null, DATABASE_VERSION);
        }

        public synchronized long insert(ContentValues values) {
            return getWritableDatabase().insert(TABLE_NAME, null, values);
        }

        public synchronized int deleteAll(String downloadDir) {
            FileUtils.deleteFiles(downloadDir, false);
            return getWritableDatabase().delete(TABLE_NAME, null, null);
        }

        public synchronized int delete(long id, boolean deleteFile) {
            if (deleteFile) {
                // delete the download file from the filesystem.
            }

            return getWritableDatabase().delete(TABLE_NAME, BaseColumns._ID + "=" + id, null);
        }

        public synchronized int update(ContentValues values, String whereClause, String[] whereArgs) {
            return getWritableDatabase().update(TABLE_NAME, values, whereClause, whereArgs);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
