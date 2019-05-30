package android.ext.cache;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.text.format.Formatter;
import android.util.Printer;

/**
 * Class <tt>LruFileCache</tt> is an implementation of a {@link LruCache}.
 * @author Garfield
 */
public class LruFileCache extends LruCache<String, File> implements FileCache {
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

    @Override
    public synchronized File get(String key) {
        final File result = map.get(key);
        return (result != null ? result : buildCacheFile(key));
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, File oldFile, File newFile) {
        if (evicted || !oldFile.equals(newFile)) {
            oldFile.delete();
        }
    }

    /**
     * Builds the cache file with the specified <em>key</em>.
     * @param key The key.
     * @return The absolute path of the cache <tt>File</tt>. Never <tt>null</tt>.
     */
    protected File buildCacheFile(String key) {
        final File result = new File(mCacheDir, new StringBuilder(key.length() + 3).append('/').append(key.charAt(0)).append('/').append(key).toString());
        if (result.exists()) {
            put(key, result);
        }

        return result;
    }

    @Override
    /* package */ void dump(Context context, Printer printer) {
        final StringBuilder result = new StringBuilder(256);
        final Collection<File> files = entries().values();
        dumpSummary(context, printer, result, files.size());

        for (File file : files) {
            result.setLength(0);
            printer.println(result.append("  ").append(file).append(" { size = ").append(Formatter.formatFileSize(context, file.length())).append(" }").toString());
        }

        dumpCacheFiles(context, printer, result);
    }

    /* package */ void dumpSummary(Context context, Printer printer, StringBuilder result, int count) {
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ size = %d, maxSize = %d ] ", getClass().getSimpleName(), count, maxSize());
    }

    private void dumpCacheFiles(Context context, Printer printer, StringBuilder result) {
        final File[] files = mCacheDir.listFiles();
        final int size = ArrayUtils.getSize(files);
        result.setLength(0);
        if (size > 0) {
            Arrays.sort(files);
        }

        long fileCount = 0, fileLength = 0;
        final long[] fileCounts = new long[2];
        for (int i = 0, index = 0; i < size; ++i) {
            final File file = files[i];
            if (file.isDirectory()) {
                ++index;
                getFileCount(file, fileCounts);
                result.append("  ").append(file.getName()).append(" { files = ").append(fileCounts[0]).append(", size = ").append(Formatter.formatFileSize(context, fileCounts[1])).append(" }");

                fileCount  += fileCounts[0];
                fileLength += fileCounts[1];
            }

            if ((index % 4) == 0) {
                result.append('\n');
            }
        }

        DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping %s disk cache [ dirs = %d, files = %d, size = %s ] ", getClass().getSimpleName(), size, fileCount, Formatter.formatFileSize(context, fileLength));
        if (result.length() > 0) {
            printer.println(result.toString());
        }
    }

    private static void getFileCount(File directory, long[] outCounts) {
        final File[] files  = directory.listFiles();
        final int fileCount = ArrayUtils.getSize(files);

        long fileLength = 0;
        for (int i = 0; i < fileCount; ++i) {
            fileLength += files[i].length();
        }

        outCounts[0] = fileCount;
        outCounts[1] = fileLength;
    }
}
