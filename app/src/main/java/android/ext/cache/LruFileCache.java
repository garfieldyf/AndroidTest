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
import android.util.Log;
import android.util.Printer;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * Class <tt>LruFileCache</tt> is an implementation of a {@link LruCache}.
 * @author Garfield
 */
public class LruFileCache extends LruCache<String, File> implements FileCache, ScanCallback {
    private boolean mInitialized;
    private final File mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #LruFileCache(Context, String, int)
     */
    public LruFileCache(File cacheDir, int maxSize) {
        super(maxSize);
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
        super(maxSize);
        mCacheDir = FileUtils.getCacheDir(context, name);
    }

    public synchronized void initialize() {
        if (!mInitialized) {
            final int priority = Process.getThreadPriority(Process.myTid());
            try {
                DebugUtils.__checkStartMethodTracing();
                Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
                FileUtils.scanFiles(mCacheDir.getPath(), this, FLAG_IGNORE_HIDDEN_FILE | FLAG_SCAN_FOR_DESCENDENTS, null);
                DebugUtils.__checkStopMethodTracing("LruFileCache", "initialize size = " + size);
            } finally {
                mInitialized = true;
                Process.setThreadPriority(priority);
            }
        }
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    /**
     * Clears this cache and all cache files will be delete from filesystem,
     * but do not call {@link #entryRemoved} on each removed entry.
     */
    @Override
    public synchronized void clear() {
        size = 0;
        map.clear();
        FileUtils.deleteFiles(mCacheDir.getPath(), false);
    }

    @Override
    public synchronized File get(String key) {
        final File cacheFile = map.get(key);
        return (cacheFile != null ? cacheFile : new File(mCacheDir, new StringBuilder(key.length() + 3).append('/').append(key.charAt(0)).append('/').append(key).toString()));
    }

    @Override
    public int onScanFile(String path, int type, Object cookie) {
        if (type == DT_REG) {
            final File value = new File(path);
            final String key = value.getName();
            map.put(key, value);
            size += sizeOf(key, value);
        }

        return SC_CONTINUE;
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, File oldFile, File newFile) {
        if (evicted || !oldFile.equals(newFile)) {
            oldFile.delete();
        }
    }

    @Override
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
