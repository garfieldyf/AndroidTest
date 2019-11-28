package android.ext.cache;

import static android.ext.util.FileUtils.Dirent.DT_REG;
import static android.ext.util.FileUtils.FLAG_IGNORE_HIDDEN_FILE;
import static android.ext.util.FileUtils.FLAG_SCAN_FOR_DESCENDENTS;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.ScanCallback;
import android.os.Process;
import android.util.Printer;
import java.io.File;
import java.util.Arrays;

/**
 * Class <tt>SimpleFileCache</tt> is an implementation of a {@link FileCache}.
 * @author Garfield
 */
public final class SimpleFileCache implements FileCache {
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
     * Returns the maximum number of files in this cache.
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
        return FileUtils.computeFileBytes(mCacheDir);
    }

    /**
     * Clears this cache and deletes all cache files from the filesystem.
     */
    public synchronized void clear() {
        DebugUtils.__checkStartMethodTracing();
        FileUtils.deleteFiles(mCacheDir.getPath(), false);
        DebugUtils.__checkStopMethodTracing("SimpleFileCache", "clear");
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    @Override
    public File put(String key, File cacheFile) {
        return null;
    }

    @Override
    public File get(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        return new File(mCacheDir, new StringBuilder(key.length() + 3).append('/').append(key.charAt(0)).append('/').append(key).toString());
    }

    @Override
    public synchronized File remove(String key) {
        final File cacheFile = get(key);
        return (cacheFile.delete() ? cacheFile : null);
    }

    /**
     * Initialize this file cache from the filesystem, do not call this method directly.
     */
    public synchronized void initialize() {
        final int priority = Process.getThreadPriority(Process.myTid());
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            new FileScanner().scanFiles(mCacheDir.getPath(), mMaxSize);
        } finally {
            Process.setThreadPriority(priority);
        }
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

    /**
     * Class <tt>FileScanner</tt> is an implementation of a {@link ScanCallback}.
     */
    /* package */ static final class FileScanner implements ScanCallback {
        private int size;
        private int maxSize;

        public final void scanFiles(String cacheDir, int maxSize) {
            DebugUtils.__checkStartMethodTracing();
            this.maxSize = maxSize;
            FileUtils.scanFiles(cacheDir, this, FLAG_IGNORE_HIDDEN_FILE | FLAG_SCAN_FOR_DESCENDENTS, null);
            DebugUtils.__checkStopMethodTracing("SimpleFileCache", "initialize size = " + size + ", maxSize = " + maxSize);
        }

        @Override
        public int onScanFile(String path, int type, Object cookie) {
            if (type == DT_REG) {
                if (size < maxSize) {
                    ++size;
                } else {
                    FileUtils.deleteFiles(path, false);
                    DebugUtils.__checkDebug(true, "SimpleFileCache", "delete cache file - " + path);
                }
            }

            return SC_CONTINUE;
        }
    }
}
