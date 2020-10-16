package android.ext.cache;

import static android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN;
import android.ext.content.AsyncTask;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.ScanCallback;
import android.os.Process;
import android.util.Printer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class <tt>LruFileCache</tt> is an implementation of a {@link FileCache}.
 * @author Garfield
 */
public final class LruFileCache implements FileCache, ScanCallback, Runnable, Comparator<String> {
    private final int mMaxSize;
    private final File mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum number of files to allow in this cache.
     */
    public LruFileCache(File cacheDir, int maxSize) {
        DebugUtils.__checkError(cacheDir == null || maxSize <= 0, "Invalid parameters - cacheDir == null || maxSize(" + maxSize + ") <= 0");
        mMaxSize  = maxSize;
        mCacheDir = cacheDir;
    }

    /**
     * Returns the maximum number of files allow in this cache.
     * @return The maximum number of files.
     */
    public final int maxSize() {
        return mMaxSize;
    }

    /**
     * Returns the total number of bytes of all cache files.
     * @return The total number of bytes.
     */
    public final long getCacheSize() {
        DebugUtils.__checkStartMethodTracing();
        final long result = FileUtils.computeFiles(mCacheDir.getPath());
        DebugUtils.__checkStopMethodTracing("LruFileCache", "getCacheSize = " + result + "(" + FileUtils.formatFileSize(result) + ")");
        return result;
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    @Override
    public void clear() {
        DebugUtils.__checkStartMethodTracing();
        FileUtils.deleteFiles(mCacheDir.getPath(), false);
        DebugUtils.__checkStopMethodTracing("LruFileCache", "clear");
    }

    @Override
    public File remove(String key) {
        DebugUtils.__checkError(key == null, "Invalid parameter - key == null");
        final File cacheFile = new File(mCacheDir, key);
        return (cacheFile.delete() ? cacheFile : null);
    }

    @Override
    public File get(String key) {
        DebugUtils.__checkError(key == null, "Invalid parameter - key == null");
        final File cacheFile = new File(mCacheDir, key);
        cacheFile.setLastModified(System.currentTimeMillis());
        return cacheFile;
    }

    @Override
    public File put(String key, File cacheFile) {
        DebugUtils.__checkError(key == null || cacheFile == null, "Invalid parameters - key == null || cacheFile == null");
        return null;
    }

    @Override
    public void trimMemory(int level) {
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            // The app's UI is no longer visible.
            // Remove the oldest files of this cache.
            AsyncTask.THREAD_POOL_EXECUTOR.execute(this);
        }
    }

    @Override
    public final void run() {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            DebugUtils.__checkStartMethodTracing();
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            final List<String> files = listFiles();
            final int size = files.size();
            if (size > mMaxSize) {
                Collections.sort(files, this);
                for (int i = mMaxSize; i < size; ++i) {
                    FileUtils.deleteFiles(files.get(i), false);
                }
            }
            DebugUtils.__checkStopMethodTracing("LruFileCache", "trimToSize size = " + size + ", maxSize = " + mMaxSize + (size > mMaxSize ? ", deleteSize = " + (size - mMaxSize) : ""));
        } finally {
            Process.setThreadPriority(priority);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final int onScanFile(String path, int type, Object cookie) {
        ((List<Object>)cookie).add(path);
        return SC_CONTINUE;
    }

    @Override
    public final int compare(String one, String another) {
        // Sort by descending order.
        return Long.compare(FileUtils.getLastModified(another), FileUtils.getLastModified(one));
    }

    private List<String> listFiles() {
        DebugUtils.__checkDebug(true, "LruFileCache", "listFiles initialCapacity = " + (mMaxSize / 10));
        final List<String> files = new ArrayList<String>(mMaxSize / 10);
        FileUtils.scanFiles(mCacheDir.getPath(), this, 0, files);
        return files;
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(100);
        DeviceUtils.dumpSummary(printer, result, 100, " Dumping LruFileCache [ files = %d, size = %s ] ", listFiles().size(), FileUtils.formatFileSize(FileUtils.computeFiles(mCacheDir.getPath())));
        result.setLength(0);
        printer.println(result.append("  cacheDir = ").append(mCacheDir).toString());
    }
}
