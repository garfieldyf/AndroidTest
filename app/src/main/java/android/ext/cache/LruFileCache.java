package android.ext.cache;

import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.util.Printer;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

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

    /**
     * Clears this cache and all cache files will be delete from filesystem,
     * but do not call {@link #entryRemoved} on each removed entry.
     */
    public synchronized void clearCache() {
        super.clear();
        FileUtils.deleteFiles(mCacheDir.getPath(), false);
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    @Override
    public File get(String key) {
        File result = super.get(key);
        if (result == null && (result = buildCacheFile(key)).exists()) {
            put(key, result);
        }

        return result;
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, File oldFile, File newFile) {
        if (evicted || !oldFile.equals(newFile)) {
            oldFile.delete();
        }
    }

    @Override
    /* package */ File removeImpl(String key) {
        File result = super.removeImpl(key);
        if (result != null) {
            return result;
        }

        result = buildCacheFile(key);
        return (result.exists() ? result : null);
    }

    /**
     * Builds the cache file with the specified <em>key</em>.
     */
    private File buildCacheFile(String key) {
        return new File(mCacheDir, new StringBuilder(key.length() + 3).append('/').append(key.charAt(0)).append('/').append(key).toString());
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
