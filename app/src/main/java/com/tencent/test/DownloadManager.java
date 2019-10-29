package com.tencent.test;

import java.util.Map;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.ext.concurrent.ThreadPoolManager;
import android.ext.concurrent.ThreadPoolManager.Task;
import android.ext.net.DownloadRequest;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;

public final class DownloadManager implements Callback {
    private final ThreadPoolManager mThreadPool;
    private final Handler mHandler;

    public DownloadManager() {
        mThreadPool = new ThreadPoolManager(5);
        final HandlerThread thread = new HandlerThread("DownloadThread");
        thread.start();
        mHandler = new Handler(thread.getLooper(), this);
    }

    public final long execute(Request request) {
        long id = request.values.getAsLong("_id");
        if (id <= 0) {
            id = request.insert(null);
        }

        mThreadPool.execute(new DownloadTask(request));
        return id;
    }

    public final boolean remove(long id) {
        final boolean result = mThreadPool.cancel(id, false);
        return result;
    }

    public final boolean cancelAll() {
        final boolean result = mThreadPool.cancelAll(false, false);
        if (result) {
        }

        return result;
    }

    public final boolean cancel(long id) {
        return mThreadPool.cancel(id, false);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return true;
    }

    public static class Request {
        /* package */ long id;
        public ContentValues values;
        public Map<String, String> extras;

        public Request(ContentValues values) {
            this.values = values;
        }

        /* package */ final long insert(Context context) {
            final long id = ContentUris.parseId(context.getContentResolver().insert(null, values));
            values.put("_id", id);
            return id;
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
        protected void onCancelled() {
        }

        @Override
        protected void onCompletion() {
        }

        @Override
        protected void doInBackground(Thread thread) {
            try {
                new DownloadRequest(request.values.getAsString("url"))
                    .range(0, 0)
                    .readTimeout(60000)
                    .connectTimeout(60000)
                    .requestHeaders(request.extras)
                    .download(null, (Object[])null);
            } catch (Exception e) {
            }
        }
    }
}
