package android.ext.cache;

import static android.ext.util.FileUtils.FLAG_IGNORE_HIDDEN_FILE;
import static android.ext.util.FileUtils.FLAG_SCAN_FOR_DESCENDENTS;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.ScanCallback;
import android.os.Process;
import android.util.Printer;
import java.io.File;
import java.util.Arrays;

/**
 * Class <tt>SimpleFileCache</tt> is an implementation of a {@link FileCache}.
 * By default, this cache size is the number of entries. Overrides {@link #sizeOf}
 * to size this cache in different units.
 * @author Garfield
 */
public class SimpleFileCache implements FileCache, ScanCallback {
    private int mSize;
    private final int mMaxSize;
    private final File mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize For caches that do not override {@link #sizeOf}, this is
     * the maximum number of files to allow in this cache. For all other caches,
     * this is the maximum sum of the sizes of the files to allow in this cache.
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
     * @param maxSize For caches that do not override {@link #sizeOf}, this is the maximum
     * number of files to allow in this cache. For all other caches, this is the maximum sum
     * of the sizes of the files to allow in this cache.
     * @see #SimpleFileCache(File, int)
     */
    public SimpleFileCache(Context context, String name, int maxSize) {
        DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
        mMaxSize  = maxSize;
        mCacheDir = FileUtils.getCacheDir(context, name);
    }

    /**
     * Returns the maximum size in this cache in user-defined units.
     * @return The maximum size.
     */
    public final int maxSize() {
        return mMaxSize;
    }

    /**
     * Returns the total number of bytes of all cache files.
     * @return The total number of bytes.
     */
    public final long getCacheSize() {
        return FileUtils.computeFileBytes(mCacheDir);
    }

    /**
     * Clears this cache and deletes all cache files from the filesystem.
     */
    public void clear() {
        DebugUtils.__checkStartMethodTracing();
        FileUtils.deleteFiles(mCacheDir.getPath(), false);
        DebugUtils.__checkStopMethodTracing("SimpleFileCache", "clear");
    }

    /**
     * Remove the cache files until the total of remaining files is
     * at or below the maximum size, do not call this method directly.
     * @hide
     */
    public void trimToSize() {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            DebugUtils.__checkStartMethodTracing();
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            mSize = 0;
            FileUtils.scanFiles(mCacheDir.getPath(), this, FLAG_IGNORE_HIDDEN_FILE | FLAG_SCAN_FOR_DESCENDENTS, null);
            DebugUtils.__checkStopMethodTracing("SimpleFileCache", "trimToSize size = " + mSize + ", maxSize = " + mMaxSize);
        } finally {
            Process.setThreadPriority(priority);
        }
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    @Override
    public File get(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        return new File(mCacheDir, new StringBuilder(key.length() + 3).append('/').append(key.charAt(0)).append('/').append(key).toString());
    }

    @Override
    public File put(String key, File cacheFile) {
        return null;
    }

    @Override
    public File remove(String key) {
        final File cacheFile = get(key);
        return (cacheFile.delete() ? cacheFile : null);
    }

    @Override
    public int onScanFile(String path, int type, Object cookie) {
        if (type == Dirent.DT_REG) {
            if (mSize < mMaxSize) {
                final int size = sizeOf(path);
                DebugUtils.__checkError(size < 0, "Negative size: " + path);
                mSize += size;
            } else {
                FileUtils.deleteFiles(path, false);
                DebugUtils.__checkDebug(true, "SimpleFileCache", "delete cache file - " + path);
            }
        }

        return SC_CONTINUE;
    }

    /**
     * Returns the size of the cache file for <em>cacheFile</em> in user-defined
     * units. The default implementation returns 1 so that size is the number of
     * files and max size is the maximum number of files.
     * @param cacheFile The cache file.
     * @return The size of the cache file, must be <tt>>= 0</tt>.
     */
    protected int sizeOf(String cacheFile) {
        return 1;
    }

    /* package */ final void dump(Context context, Printer printer) {
        final StringBuilder result = new StringBuilder(256);
        final File[] files = mCacheDir.listFiles();
        final int size = ArrayUtils.getSize(files);
        result.setLength(0);
        if (size > 0) {
            Arrays.sort(files);
        }

        long fileCounts = 0, fileLengths = 0;
        final long[] results  = new long[2];
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

        DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping SimpleFileCache [ dirs = %d, files = %d, size = %s ] ", size, fileCounts, FileUtils.formatFileSize(fileLengths));
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
