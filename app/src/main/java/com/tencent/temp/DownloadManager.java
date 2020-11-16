package com.tencent.temp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.ext.concurrent.ThreadPoolManager;
import android.ext.concurrent.ThreadPoolManager.Task;
import android.ext.util.FileUtils;
import android.text.TextUtils;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class DownloadManager
 * @author beckyuan
 */
public final class DownloadManager {
    private static final int MAX_WRITTEN_TIMES = 500;
    private static final int BUFFER_SIZE = 2048;

    private String mDownloadDirectory;
    private final Context mContext;
    private final ThreadPoolManager mExecutor;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     */
    public DownloadManager(Context context) {
        mContext  = context.getApplicationContext();
        mExecutor = new ThreadPoolManager(5);
        mDownloadDirectory = FileUtils.getCacheDir(context, "QQDownloadsTV").getPath();
    }

    /**
     * Enqueue a new download. The download will start automatically.
     * @param request The parameters specifying this download.
     * @return An identifier for the download.
     */
    public long download(Request request) {
        request.insert(mContext.getContentResolver());
        mExecutor.execute(new DownloadTask(request));
        return request.id;
    }

    /**
     * Attempts to cancel the download with specified identifier.
     * @param id The identifier of the download to cancel.
     * @return <tt>true</tt> if the download was cancelled,
     * <tt>false</tt> otherwise.
     */
    public boolean cancel(long id) {
        return mExecutor.cancel(id, true);
    }

    /**
     * Removes the download with specified identifier.
     * @param id The identifier of the download to remove.
     * @param filename Whether to delete the downloaded file
     * in file system. This parameter can be <tt>null</tt>.
     */
    public void remove(long id, String filename) {
        // Removes it from running task queue, if
        // the pending task queue is not present.
        mExecutor.cancel(id, false);

        // Removes the download task from the database.
        mContext.getContentResolver().delete(Downloads.CONTENT_URI, Downloads._ID + '=' + id, null);

        // Deletes the downloaded file.
        if (!TextUtils.isEmpty(filename) && FileUtils.isAbsolutePath(filename)) {
            FileUtils.deleteFiles(filename, false);
        }
    }

    /**
     * Removes all the downloads.
     * @param deleteFiles Whether to delete the downloaded file in file system.
     */
    public void removeAll(boolean deleteFiles) {
        mExecutor.cancelAll(false);
        mContext.getContentResolver().delete(Downloads.CONTENT_URI, null, null);
        if (deleteFiles) {
            FileUtils.deleteFiles(mDownloadDirectory, false);
        }
    }

    /**
     * Returns the <tt>Context</tt> associated with this object.
     * @return The application <tt>Context</tt>.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * This class contains all the information necessary to request a new download.
     */
    public static final class Request implements Runnable {
        /**
         * Whether to execute the download file,
         * if the download successful.
         */
        public static final int FLAG_AUTO_EXECUTE = 0x01;

        /**
         * Whether to rename this download filename,
         * if the download file exists.
         */
        public static final int FLAG_AUTO_RENAME = 0x02;

        /**
         * The URI to be downloaded.
         */
        public String uri;

        /**
         * The download request flags, May be any
         * combination of <tt>FLAG_AUTO_XXX</tt>.
         */
        public int flags;

        /**
         * The filename of this download. Can be
         * a relative or an absolute file path.
         */
        public String filename;

        /**
         * The MIME type of the downloaded file.
         */
        public String mimeType;

        /**
         * The title for this download.
         */
        public String title;

        /**
         * The description of this download.
         */
        public String description;

        /**
         * An identifier for this download.
         */
        private long id;

        /**
         * The total size of this download in bytes.
         */
        private long totalSize;

        /**
         * The number of downloaded bytes of this download.
         */
        private long downloadedSize;

        /**
         * The <tt>ContentResolver</tt>.
         */
        private ContentResolver resolver;

        /**
         * The additional HTTP headers.
         */
        private Map<String, String> extraHeaders;

        /**
         * Constructor
         * @param cursor The <tt>Cursor</tt> to initialize this object.
         * @see #Request(String, String, String)
         */
        public Request(Cursor cursor) {
            this.id  = cursor.getLong(cursor.getColumnIndexOrThrow(Downloads._ID));
            this.uri = cursor.getString(cursor.getColumnIndexOrThrow(Downloads.URI));
            this.filename  = cursor.getString(cursor.getColumnIndexOrThrow(Downloads.FILENAME));
            this.totalSize = cursor.getLong(cursor.getColumnIndexOrThrow(Downloads.TOTAL_SIZE));
            this.downloadedSize = cursor.getLong(cursor.getColumnIndexOrThrow(Downloads.DOWNLOADED_SIZE));
        }

        /**
         * Constructor
         * @param uri The URI to be downloaded.
         * @param title The title for this download.
         * @param filename The filename of this download.
         * Can be a relative or an absolute file path.
         * @see #Request(Cursor)
         */
        public Request(String uri, String title, String filename) {
            this.id    = -1;
            this.uri   = uri;
            this.title = title;
            this.flags = FLAG_AUTO_RENAME;
            this.filename = filename;
        }

        /**
         * Add an HTTP header to be included with this download request.
         * The header will be added to the end of the list.
         * @param name The HTTP header name
         * @param value The header value.
         * @return This request.
         */
        public Request addRequestHeader(String name, String value) {
            if (extraHeaders == null) {
                extraHeaders = new HashMap<String, String>();
            }

            extraHeaders.put(name, value);
            return this;
        }

        @Override
        public void run() {
        }

        private void insert(ContentResolver resolver) {
            this.resolver = resolver;
            final ContentValues values = new ContentValues();
            values.put(Downloads.STATUS, Downloads.STATUS_PENDING);
            if (id == -1) {
                // A newly download task.
                values.put(Downloads.URI, uri);
                values.put(Downloads.TITLE, title);
                values.put(Downloads.FILENAME, filename);
                values.put(Downloads.DESCRIPTION, description);
                id = ContentUris.parseId(resolver.insert(Downloads.CONTENT_URI, values));
            } else {
                // A redownload task.
                resolver.update(Downloads.CONTENT_URI, values, Downloads._ID + '=' + id, null);
            }
        }

        private void updateStatus(int status) {
            final ContentValues values = new ContentValues();
            values.put(Downloads.STATUS, status);
            resolver.update(Downloads.CONTENT_URI, values, Downloads._ID + '=' + id, null);
        }

        private void updateDownloadInfo() {
            final ContentValues values = new ContentValues();
            values.put(Downloads.FILENAME, filename);
            values.put(Downloads.MIME_TYPE, mimeType);
            values.put(Downloads.TOTAL_SIZE, totalSize);
            values.put(Downloads.DOWNLOADED_SIZE, downloadedSize);
            resolver.update(Downloads.CONTENT_URI, values, Downloads._ID + '=' + id, null);
        }

        private void updateDownloadedSize(int speed) {
            final ContentValues values = new ContentValues();
            values.put(Downloads.SPEED, speed);
            values.put(Downloads.DOWNLOADED_SIZE, downloadedSize);
            resolver.update(Downloads.CONTENT_URI, values, Downloads._ID + '=' + id, null);
        }

        private void updateDownloadFinished() {
            final ContentValues values = new ContentValues();
            values.put(Downloads.DOWNLOADED_SIZE, downloadedSize);
            if (downloadedSize == totalSize) {
                values.put(Downloads.STATUS, Downloads.STATUS_SUCCESSFUL);
                values.put(Downloads.FINISHED_TIME, System.currentTimeMillis());
                if ((flags & FLAG_AUTO_EXECUTE) == FLAG_AUTO_EXECUTE) {
                    EventBus.getHandler().post(this);
                }
            } else {
                values.put(Downloads.STATUS, Downloads.STATUS_PAUSED);
            }

            resolver.update(Downloads.CONTENT_URI, values, Downloads._ID + '=' + id, null);
        }

        private void addRequestHeaders(HttpURLConnection conn) {
            if (extraHeaders != null) {
                final Set<Entry<String, String>> entries = extraHeaders.entrySet();
                for (Entry<String, String> entry : entries) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        }

        private void createFile(String downloadPath) throws IOException {
            if (!FileUtils.isAbsolutePath(filename)) {
                filename = downloadPath + File.separatorChar + filename;
            }

            if ((flags & FLAG_AUTO_RENAME) == FLAG_AUTO_RENAME) {
                // Creates unique file based on original download filename.
                final String uniqueFile = FileUtils.createUniqueFile(filename, (int)totalSize);
                if (uniqueFile == null) {
                    throw new IOException("Couldn't create file - " + filename);
                }

                filename = uniqueFile;
            }
        }
    }

    /**
     * Nested class DownloadTask
     */
    private final class DownloadTask extends Task {
        private final Request request;
        private volatile boolean cancel;

        public DownloadTask(Request request) {
            this.request = request;
        }

        @Override
        public long getId() {
            return request.id;
        }

        @Override
        protected void onCompletion() {
        }

        @Override
        protected void doInBackground() {
        }
    }
}