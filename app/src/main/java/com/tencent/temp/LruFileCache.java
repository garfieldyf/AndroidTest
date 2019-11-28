package com.tencent.temp;

import static android.ext.util.FileUtils.Dirent.DT_REG;
import static android.ext.util.FileUtils.FLAG_IGNORE_HIDDEN_FILE;
import static android.ext.util.FileUtils.FLAG_SCAN_FOR_DESCENDENTS;
import android.content.Context;
import android.ext.cache.FileCache;
import android.ext.cache.LruCache;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.ScanCallback;
import android.os.Process;
import android.util.Printer;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * Class <tt>LruFileCache</tt> is an implementation of a {@link FileCache}.
 * @author Garfield
 */
public class LruFileCache extends LruCache<String, File> implements FileCache, ScanCallback {
    protected final File mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #LruFileCache(Context, String, int)
     */
    public LruFileCache(File cacheDir, int maxSize) {
        super(maxSize);
        mCacheDir = cacheDir;
        DebugUtils.__checkError(cacheDir == null, "cacheDir == null");
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param name A relative path within the cache directory, such as <tt>"file_cache"</tt>.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #LruFileCache(File, int)
     */
    public LruFileCache(Context context, String name, int maxSize) {
        super(maxSize);
        mCacheDir = FileUtils.getCacheDir(context, name);
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
//        size = 0;
//        map.clear();
        super.clear();
        FileUtils.deleteFiles(mCacheDir.getPath(), false);
        DebugUtils.__checkStopMethodTracing("LruFileCache", "clear");
    }

    @Override
    public File get(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        final File cacheFile = super.get(key);
        return (cacheFile != null ? cacheFile : new File(mCacheDir, new StringBuilder(key.length() + 3).append('/').append(key.charAt(0)).append('/').append(key).toString()));
    }

    /**
     * Initialize this file cache from the filesystem, do not call this method directly.
     */
    public synchronized void initialize() {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            FileUtils.scanFiles(mCacheDir.getPath(), this, FLAG_IGNORE_HIDDEN_FILE | FLAG_SCAN_FOR_DESCENDENTS, null);
        } finally {
            Process.setThreadPriority(priority);
        }
    }

    @Override
    public int onScanFile(String path, int type, Object cookie) {
        if (type == DT_REG) {
            //if (size < maxSize) {
            if (size() < maxSize()) {
                final File cacheFile = new File(path);
                final String key = cacheFile.getName();
                //map.put(key, cacheFile);
                //size += sizeOf(key, cacheFile);
                put(key, cacheFile);
            } else {
                FileUtils.deleteFiles(path, false);
                DebugUtils.__checkDebug(true, "LruFileCache", "delete cache file - " + path);
            }
        }

        return SC_CONTINUE;
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, File oldFile, File newFile) {
        if (evicted || !oldFile.equals(newFile)) {
            oldFile.delete();
        }
    }

    /* package */ void dump(Context context, Printer printer) {
        final StringBuilder result = new StringBuilder(256);
        final Collection<File> files = snapshot().values();
        dumpSummary(printer, result, files.size());

        for (File file : files) {
            result.setLength(0);
            printer.println(result.append("  ").append(file).append(" { size = ").append(FileUtils.formatFileSize(file.length())).append(" }").toString());
        }

        dumpCacheFiles(printer, result);
    }

    private void dumpSummary(Printer printer, StringBuilder result, int count) {
        if (this instanceof LruFileCache2) {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ count = %d, size = %s, maxSize = %s ] ", getClass().getSimpleName(), count, FileUtils.formatFileSize(size()), FileUtils.formatFileSize(maxSize()));
        } else {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ size = %d, maxSize = %d ] ", getClass().getSimpleName(), count, maxSize());
        }
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

        DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping %s disk cache [ dirs = %d, files = %d, size = %s ] ", getClass().getSimpleName(), size, fileCounts, FileUtils.formatFileSize(fileLengths));
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
