package com.tencent.temp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.ext.cache.FileCache;
import android.ext.cache.LruCache;
import android.ext.database.DatabaseUtils;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.os.Process;
import android.util.Printer;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Class <tt>LruFileCache</tt> is an implementation of a {@link FileCache}.
 * @author Garfield
 */
public class LruFileCache extends LruCache<String, File> implements FileCache {
    private final File mCacheDir;
    private final Object[] mBindArgs;
    private final SQLiteDatabase mDatabase;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param name A relative path within the cache directory, such as <tt>"file_cache"</tt>.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #LruFileCache(Context, File, int)
     */
    public LruFileCache(Context context, String name, int maxSize) {
        this(context, FileUtils.getCacheDir(context, name), maxSize);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #LruFileCache(Context, String, int)
     */
    public LruFileCache(Context context, File cacheDir, int maxSize) {
        super(maxSize);
        mCacheDir = cacheDir;
        mBindArgs = new Object[1];
        mDatabase = openDatabase(context);
        DebugUtils.__checkError(cacheDir == null, "cacheDir == null");
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    /**
     * Clears this cache and deletes all cache files from the filesystem,
     * but do not call {@link #entryRemoved} on each removed entry.
     */
    @Override
    public synchronized void clear() {
        DebugUtils.__checkStartMethodTracing();
        //size = 0;
        //map.clear();
        super.clear();
        mDatabase.execSQL("DELETE FROM caches");
        FileUtils.deleteFiles(mCacheDir.getPath(), false);
        DebugUtils.__checkStopMethodTracing("LruFileCache", "clear");
    }

    @Override
    public File get(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        final File cacheFile = super.get(key);
        return (cacheFile != null ? cacheFile : getCacheFile(key));
    }

    /**
     * Initialize this file cache from the filesystem, do not call this method directly.
     */
    public synchronized final void initialize() {
        final int priority = Process.getThreadPriority(Process.myTid());
        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);

        DebugUtils.__checkStartMethodTracing();
        final Cursor cursor = mDatabase.rawQuery("SELECT _data FROM caches", null);
        try {
            while (cursor.moveToNext()) {
                final String key = cursor.getString(0);
                final File value = getCacheFile(key);
                //map.put(key, value);
                //size += sizeOf(key, value);
                put(key, value);
            }
        } finally {
            cursor.close();
            Process.setThreadPriority(priority);
            //DebugUtils.__checkStopMethodTracing("LruFileCache", "initialize maxSize = " + maxSize() + ", size = " + size() + ", count = " + map.size());
            DebugUtils.__checkStopMethodTracing("LruFileCache", "initialize maxSize = " + maxSize() + ", size = " + size());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            mDatabase.close();
        } finally {
            super.finalize();
        }
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, File oldFile, File newFile) {
        if (evicted || !oldFile.equals(newFile)) {
            oldFile.delete();
            mBindArgs[0] = key;
            mDatabase.execSQL("DELETE FROM caches WHERE _data=?", mBindArgs);
        }
    }

//    @Override
//    /* package */ synchronized File putImpl(String key, File value) {
//        mBindArgs[0] = key;
//        mDatabase.execSQL("INSERT OR IGNORE INTO caches VALUES(?)", mBindArgs);
//        return super.putImpl(key, value);
//    }

    /**
     * Open the cache database associated with this cache.
     */
    private SQLiteDatabase openDatabase(Context context) {
        final String name;
        try {
            final byte[] digests = MessageDigests.computeString(mCacheDir.getCanonicalPath(), Algorithm.SHA1);
            name = StringUtils.toHexString(new StringBuilder(41).append('.'), digests, 0, digests.length).toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to resolve canonical path - " + mCacheDir);
        }

        final SQLiteDatabase db = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null, null);
        if (db.getVersion() == 0) {
            try {
                db.beginTransaction();
                db.execSQL("CREATE TABLE IF NOT EXISTS caches (_data TEXT PRIMARY KEY)");
                db.setVersion(1);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        return db;
    }

    /**
     * Returns the cache file with the specified <em>key</em>.
     */
    private File getCacheFile(String key) {
        return new File(mCacheDir, new StringBuilder(key.length() + 3).append('/').append(key.charAt(0)).append('/').append(key).toString());
    }

    /* package */ final void dump(Context context, Printer printer) {
        final StringBuilder result = new StringBuilder(256);
        final Collection<File> files = snapshot().values();
        dumpSummary(printer, result, files.size());

        for (File file : files) {
            result.setLength(0);
            printer.println(result.append("  ").append(file).append(" { size = ").append(FileUtils.formatFileSize(file.length())).append(" }").toString());
        }

        dumpCacheFiles(printer, result);
    }

    /* package */ void dumpSummary(Printer printer, StringBuilder result, int count) {
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ size = %d, maxSize = %d ] ", getClass().getSimpleName(), count, maxSize());
    }

    private void dumpCacheFiles(Printer printer, StringBuilder result) {
        final File[] files = mCacheDir.listFiles();
        final int size = ArrayUtils.getSize(files);
        result.setLength(0);
        if (size > 0) {
            Arrays.sort(files);
        }

        long fileCounts = 0, fileLengths = 0;
        final long[] results = new long[2];
        for (int i = 0, index = 0; i < size; ++i) {
            final File file = files[i];
            if (file.isDirectory()) {
                ++index;
                getFileInfo(file, results);
                result.append("  ").append(file.getName()).append(" { files = ").append(results[0]).append(", size = ").append(FileUtils.formatFileSize(results[1])).append(" }");

                fileCounts  += results[0];
                fileLengths += results[1];
            }

            if ((index % 4) == 0) {
                result.append('\n');
            }
        }

        DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping %s disk cache [ dirs = %d, files = %d, items = %d, size = %s ] ", getClass().getSimpleName(), size, fileCounts, DatabaseUtils.simpleQueryLong(mDatabase, "SELECT COUNT(_data) FROM caches", (Object[])null), FileUtils.formatFileSize(fileLengths));
        if (result.length() > 0) {
            printer.println(result.toString());
        }
    }

    private static void getFileInfo(File directory, long[] outResults) {
        final File[] files  = directory.listFiles();
        final int fileCount = ArrayUtils.getSize(files);

        long fileLength = 0;
        for (int i = 0; i < fileCount; ++i) {
            fileLength += files[i].length();
        }

        outResults[0] = fileCount;
        outResults[1] = fileLength;
    }
}
