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
 * Class <tt>SimpleFileCache</tt> is an implementation of a {@link FileCache}.
 * @author Garfield
 */
public class SimpleFileCache implements FileCache, Comparator<File> {
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
        DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
        mMaxSize  = maxSize;
        mCacheDir = FileUtils.getCacheDir(context, name);
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
        long result = 0;
        final String[] names = mCacheDir.list();
        for (int i = ArrayUtils.getSize(names) - 1; i >= 0; --i) {
            result += new File(mCacheDir, names[i]).length();
        }

        return result;
    }

    /**
     * Remove the cache files until the total of remaining files is
     * at or below the maximum size, do not call this method directly.
     * @hide
     */
    public final void trimToSize() {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            DebugUtils.__checkStartMethodTracing();
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            final String[] names = mCacheDir.list();
            final int size = ArrayUtils.getSize(names);
            for (int i = mMaxSize; i < size; ++i) {
                new File(mCacheDir, names[i]).delete();
            }
            DebugUtils.__checkStopMethodTracing("SimpleFileCache", "trimToSize size = " + size + ", maxSize = " + mMaxSize + (size > mMaxSize ? ", delete file count = " + (size - mMaxSize) : ""));
        } finally {
            Process.setThreadPriority(priority);
        }
    }

//    /**
//     * Remove the cache files until the total of remaining files is
//     * at or below the maximum size, do not call this method directly.
//     * @hide
//     */
//    public final void trimToSize() {
//        final int priority = Process.getThreadPriority(Process.myTid());
//        try {
//            DebugUtils.__checkStartMethodTracing();
//            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
//            final File[] files = mCacheDir.listFiles();
//            final int size = ArrayUtils.getSize(files);
//            if (size > 0) {
//                Arrays.sort(files, this);
//                for (int i = mMaxSize; i < size; ++i) {
//                    files[i].delete();
//                }
//            }
//            DebugUtils.__checkStopMethodTracing("SimpleFileCache", "trimToSize size = " + size + ", maxSize = " + mMaxSize + (size > mMaxSize ? ", delete file count = " + (size - mMaxSize) : ""));
//        } finally {
//            Process.setThreadPriority(priority);
//        }
//    }

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
        return null;
    }

    @Override
    public File remove(String key) {
        final File cacheFile = new File(mCacheDir, key);
        return (cacheFile.delete() ? cacheFile : null);
    }

    @Override
    public int compare(File one, File another) {
        return (Long.compare(another.lastModified(), one.lastModified()));
    }

    /* package */ final void dump(Printer printer) {
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
