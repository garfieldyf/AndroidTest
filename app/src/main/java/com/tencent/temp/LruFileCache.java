package com.tencent.temp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.ext.cache.FileCache;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class <tt>LruFileCache</tt> is an implementation of a {@link FileCache}.
 * @author Garfield
 */
public class LruFileCache implements FileCache {
    private int mSize;
    private final int mMaxSize;
    private boolean mInitialized;

    private final File mCacheDir;
    private final Map<String, File> mCache;

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
        DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
        DebugUtils.__checkError(cacheDir == null || cacheDir.getPath().isEmpty(), "cacheDir == null || cacheDir.length() == 0");
        mMaxSize  = maxSize;
        mCacheDir = cacheDir;
        mBindArgs = new Object[1];
        mDatabase = openDatabase(context);
        mCache = new LinkedHashMap<String, File>(0, 0.75f, true);
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    /**
     * Returns the maximum size in this cache in user-defined units.
     * @return The maximum size.
     * @see #size()
     */
    public final int maxSize() {
        return mMaxSize;
    }

    /**
     * For caches that do not override {@link #sizeOf}, this returns the
     * number of cache files in the cache. For all other caches, this
     * returns the sum of the sizes of the cache files in this cache.
     * @return The size.
     */
    public synchronized int size() {
        initialize();
        return mSize;
    }

    /**
     * Clears this cache and all cache files will be delete from filesystem.
     */
    @Override
    public synchronized void clear() {
        DebugUtils.__checkStartMethodTracing();
        mSize = 0;
        mCache.clear();
        mDatabase.execSQL("DELETE FROM caches");
        FileUtils.deleteFiles(mCacheDir.getPath(), false);
        DebugUtils.__checkStopMethodTracing("LruFileCache", "clearFileCache");
    }

    @Override
    public synchronized File remove(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        initialize();
        final File oldFile = mCache.remove(key);
        if (oldFile != null) {
            mSize -= sizeOf(key, oldFile);
            entryRemoved(key, oldFile, null);
        }

        return oldFile;
    }

    @Override
    public synchronized File get(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        initialize();
        final File cacheFile = mCache.get(key);
        return (cacheFile != null ? cacheFile : getCacheFile(key));
    }

    @Override
    public synchronized File put(String key, File cacheFile) {
        // Initialize this file cache from the cache database.
        DebugUtils.__checkError(key == null || cacheFile == null, "key == null || cacheFile == null");
        initialize();

        final int size = sizeOf(key, cacheFile);
        DebugUtils.__checkError(size < 0, "Negative size: " + key + " = " + cacheFile);
        mSize += size;

        // Inserts the key into caches table if not exists.
        mBindArgs[0] = key;
        mDatabase.execSQL("INSERT OR IGNORE INTO caches VALUES(?)", mBindArgs);

        // Sets the cacheFile into file cache.
        final File oldFile = mCache.put(key, cacheFile);
        if (oldFile != null) {
            mSize -= sizeOf(key, oldFile);
            entryRemoved(key, oldFile, cacheFile);
        }

        removeEldest();
        return oldFile;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            mDatabase.close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Returns the size of the cache file for <tt>key</tt> and <tt>cacheFile</tt>
     * in user-defined units. The default implementation returns 1 so that size is
     * the number of cache files and max size is the maximum number of cache files.
     * @param key The key.
     * @param cacheFile The cache file.
     * @return The size of the cache file, must be <tt>>= 0</tt>.
     */
    protected int sizeOf(String key, File cacheFile) {
        return 1;
    }

    /**
     * Called for cache files that have been evicted or removed. This method is invoked
     * when a cache file is evicted to make space, removed by a call to {@link #remove},
     * or replaced by a call to {@link #put}.
     * @param key The key.
     * @param oldFile The old cache file for <em>key</em>.
     * @param newFile The new cache file for <em>key</em> or <tt>null</tt>.
     */
    protected void entryRemoved(String key, File oldFile, File newFile) {
        if (!oldFile.equals(newFile)) {
            oldFile.delete();
            mBindArgs[0] = key;
            mDatabase.execSQL("DELETE FROM caches WHERE _data=?", mBindArgs);
        }
    }

    /**
     * Initialize this file cache from the cache database.
     */
    private void initialize() {
        if (mInitialized) {
            return;
        }

        final int priority = Process.getThreadPriority(Process.myTid());
        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);

        DebugUtils.__checkStartMethodTracing();
        final Cursor cursor = mDatabase.rawQuery("SELECT _data FROM caches", null);
        try {
            while (cursor.moveToNext()) {
                final String key = cursor.getString(0);
                final File value = getCacheFile(key);
                mCache.put(key, value);
                mSize += sizeOf(key, value);
            }
        } finally {
            cursor.close();
            mInitialized = true;
            Process.setThreadPriority(priority);
            DebugUtils.__checkStopMethodTracing("LruFileCache", "initialize maxSize = " + mMaxSize + ", size = " + mSize + ", count = " + mCache.size());
        }
    }

    /**
     * Remove the eldest cache files until the total of remaining
     * cache files is at or below the requested <em>mMaxSize</em>.
     */
    private void removeEldest() {
        final Iterator<Entry<String, File>> itor = mCache.entrySet().iterator();
        while (mSize > mMaxSize && itor.hasNext()) {
            final Entry<String, File> toEvict = itor.next();
            final String key = toEvict.getKey();
            final File value = toEvict.getValue();
            itor.remove();

            final int size = sizeOf(key, value);
            DebugUtils.__checkError(size < 0, "Negative size: " + key + " = " + value);
            mSize -= size;
            entryRemoved(key, value, null);
        }
    }

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
     * Returns a copy of the current contents of this cache.
     */
    private synchronized Map<String, File> snapshot() {
        return new LinkedHashMap<String, File>(mCache);
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
