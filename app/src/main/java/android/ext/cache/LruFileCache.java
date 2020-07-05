package android.ext.cache;

import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.os.Process;
import android.util.Printer;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Class <tt>LruFileCache</tt> is an implementation of a {@link FileCache}.
 * @author Garfield
 */
public final class LruFileCache implements FileCache, Runnable, Comparator<File> {
    private final int mMaxSize;
    private final File mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #LruFileCache(Context, String, int)
     */
    public LruFileCache(File cacheDir, int maxSize) {
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
     * @see #LruFileCache(File, int)
     */
    public LruFileCache(Context context, String name, int maxSize) {
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
     * Clears this cache and deletes all cache files from the filesystem.
     */
    public final void clear() {
        DebugUtils.__checkStartMethodTracing();
        FileUtils.deleteFiles(mCacheDir.getPath(), false);
        DebugUtils.__checkStopMethodTracing("LruFileCache", "clear");
    }

    /**
     * Returns the total number of bytes of all cache files.
     * @return The total number of bytes.
     */
    public final long getCacheSize() {
        DebugUtils.__checkStartMethodTracing();
        final long result = FileUtils.computeFileSizes(mCacheDir.getPath());
        DebugUtils.__checkStopMethodTracing("LruFileCache", "getCacheSize = " + result + "(" + FileUtils.formatFileSize(result) + ")");
        return result;
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    @Override
    public File remove(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        final File cacheFile = new File(mCacheDir, key);
        return (cacheFile.delete() ? cacheFile : null);
    }

    @Override
    public File get(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        final File cacheFile = new File(mCacheDir, key);
        cacheFile.setLastModified(System.currentTimeMillis());
        return cacheFile;
    }

    @Override
    public File put(String key, File cacheFile) {
        DebugUtils.__checkError(key == null || cacheFile == null, "key == null || cacheFile == null");
        return null;
    }

    @Override
    public final void run() {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            DebugUtils.__checkStartMethodTracing();
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            final File[] files = mCacheDir.listFiles();
            final int size = ArrayUtils.getSize(files);
            if (size > mMaxSize) {
                Arrays.sort(files, this);
                for (int i = mMaxSize; i < size; ++i) {
                    files[i].delete();
                    DebugUtils.__checkDebug(true, "LruFileCache", "deleteFile = " + files[i].getName());
                }
            }
            DebugUtils.__checkStopMethodTracing("LruFileCache", "trimToSize size = " + size + ", maxSize = " + mMaxSize + (size > mMaxSize ? ", deleteSize = " + (size - mMaxSize) : ""));
        } finally {
            Process.setThreadPriority(priority);
        }
    }

    @Override
    public final int compare(File one, File another) {
        // Sort by descending order.
        return Long.compare(another.lastModified(), one.lastModified());
    }

    public final void dump(Printer printer) {
        final File[] files = mCacheDir.listFiles();
        final int size = ArrayUtils.getSize(files);

        long length = 0;
        for (int i = 0; i < size; ++i) {
            length += files[i].length();
        }

        final StringBuilder result = new StringBuilder(100);
        DebugUtils.dumpSummary(printer, result, 100, " Dumping LruFileCache [ files = %d, size = %s ] ", size, FileUtils.formatFileSize(length));
        result.setLength(0);
        printer.println(result.append("  cacheDir = ").append(mCacheDir.getPath()).toString());
    }
}
