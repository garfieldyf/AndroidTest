package android.ext.cache;

import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.ScanCallback;
import android.os.Process;
import android.util.Printer;
import java.io.File;

/**
 * Class <tt>SimpleFileCache</tt> is an implementation of a {@link FileCache}.
 * @author Garfield
 */
public final class SimpleFileCache implements FileCache, ScanCallback, Runnable {
    private int mSize;
    private final int mMaxSize;
    private final File mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #SimpleFileCache(Context, String, int)
     */
    public SimpleFileCache(File cacheDir, int maxSize) {
        DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
        DebugUtils.__checkError(cacheDir == null, "cacheDir == null");
        mMaxSize  = maxSize;
        mCacheDir = cacheDir;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param name A relative path within the cache directory, such as <tt>"file_cache"</tt>.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #SimpleFileCache(File, int)
     */
    public SimpleFileCache(Context context, String name, int maxSize) {
        this(FileUtils.getCacheDir(context, name), maxSize);
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
        final long result = FileUtils.computeFileSizes(mCacheDir.getPath());
        DebugUtils.__checkStopMethodTracing("SimpleFileCache", "getCacheSize");
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
        DebugUtils.__checkStopMethodTracing("SimpleFileCache", "clear");
    }

    @Override
    public File get(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        return new File(mCacheDir, key);
    }

    @Override
    public File put(String key, File cacheFile) {
        DebugUtils.__checkError(key == null || cacheFile == null, "key == null || cacheFile == null");
        return null;
    }

    @Override
    public File remove(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        final File cacheFile = new File(mCacheDir, key);
        return (cacheFile.delete() ? cacheFile : null);
    }

    @Override
    public final void run() {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            DebugUtils.__checkStartMethodTracing();
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            FileUtils.scanFiles(mCacheDir.getPath(), this, mSize = 0, null);
            DebugUtils.__checkStopMethodTracing("SimpleFileCache", "trimToSize size = " + mSize + ", maxSize = " + mMaxSize + (mSize > mMaxSize ? ", deleteSize = " + (mSize - mMaxSize) : ""));
        } finally {
            Process.setThreadPriority(priority);
        }
    }

    @Override
    public final int onScanFile(String path, int type, Object cookie) {
        if (++mSize > mMaxSize || FileUtils.findFileExtension(path) != -1 /* delete temp file */) {
            FileUtils.deleteFiles(path, false);
            DebugUtils.__checkDebug(true, "SimpleFileCache", "deleteFile = " + path);
        }

        return SC_CONTINUE;
    }

    public final void dump(Printer printer) {
        final String[] names = mCacheDir.list();
        final int size = ArrayUtils.getSize(names);

        long length = 0;
        for (int i = 0; i < size; ++i) {
            length += new File(mCacheDir, names[i]).length();
        }

        final StringBuilder result = new StringBuilder(100);
        DebugUtils.dumpSummary(printer, result, 100, " Dumping SimpleFileCache [ files = %d, size = %s ] ", size, FileUtils.formatFileSize(length));
        result.setLength(0);
        printer.println(result.append("  cacheDir = ").append(mCacheDir.getPath()).toString());
    }
}
