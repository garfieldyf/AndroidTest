package android.ext.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.Dirent;
import android.text.format.Formatter;
import android.util.Pair;
import android.util.Printer;

/**
 * Class <tt>LruFileCache</tt> is an implementation of a {@link LruCache}.
 * @author Garfield
 * @version 4.0
 */
public class LruFileCache extends LruCache<String, String> implements FileCache {
    protected final String mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum number of files to allow in this cache.
     */
    public LruFileCache(String cacheDir, int maxSize) {
        super(maxSize);
        mCacheDir = cacheDir;
    }

    @Override
    public String getCacheDir() {
        return mCacheDir;
    }

    @Override
    public String get(String key) {
        String result = super.get(key);
        if (result == null) {
            result = buildCacheFile(key);
            if (FileUtils.access(result, FileUtils.F_OK) == 0) {
                put(key, result);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return toString(160).append(", cacheDir = ").append(mCacheDir).append(" }").toString();
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, String oldValue, String newValue) {
        if (evicted || !oldValue.equals(newValue)) {
            FileUtils.deleteFiles(oldValue, false);
        }
    }

    /**
     * Builds the cache file with the specified <em>key</em>. <p>The method is called without
     * synchronization: other threads may access the cache while this method is executing.</p>
     * @param key The key.
     * @return The absolute path of the cache file. Never <tt>null</tt>.
     */
    protected String buildCacheFile(String key) {
        return new StringBuilder(mCacheDir.length() + key.length() + 3).append(mCacheDir).append('/').append(key.charAt(0)).append('/').append(key).toString();
    }

    /* package */ final void dump(Context context, Printer printer) {
        final String className = getClass().getSimpleName();
        final StringBuilder result = new StringBuilder(256);
        final Collection<String> files = entries().values();

        if (this instanceof LruFileCache2) {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ count = %d, size = %s, maxSize = %s ] ", className, files.size(), Formatter.formatFileSize(context, size()), Formatter.formatFileSize(context, maxSize()));
        } else {
            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ size = %d, maxSize = %d ] ", className, files.size(), maxSize());
        }

        for (String file : files) {
            result.setLength(0);
            printer.println(result.append("  ").append(file).append(" [ size = ").append(Formatter.formatFileSize(context, FileUtils.getFileLength(file))).append(" ]").toString());
        }

        dumpCachedFiles(context, printer, result, className);
    }

    private void dumpCachedFiles(Context context, Printer printer, StringBuilder result, String className) {
        final List<Dirent> dirents = FileUtils.listFiles(mCacheDir, 0);
        final int size = ArrayUtils.getSize(dirents);
        result.setLength(0);
        if (size > 0) {
            Collections.sort(dirents);
        }

        long fileCount = 0, fileBytes = 0;
        for (int i = 0, index = 0; i < size; ++i) {
            final Dirent dirent = dirents.get(i);
            if (dirent.isDirectory()) {
                ++index;
                final Pair<Integer, Long> pair = getFileCount(dirent);
                result.append("  ").append(dirent.getName()).append(" [ files = ").append(pair.first).append(", size = ").append(Formatter.formatFileSize(context, pair.second)).append(" ]");

                fileCount += pair.first;
                fileBytes += pair.second;
            }

            if ((index % 4) == 0) {
                result.append('\n');
            }
        }

        DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping %s disk cache [ dirs = %d, files = %d, size = %s ] ", className, size, fileCount, Formatter.formatFileSize(context, fileBytes));
        if (result.length() > 0) {
            printer.println(result.toString());
        }
    }

    private static Pair<Integer, Long> getFileCount(Dirent dirent) {
        final List<Dirent> dirents = dirent.listFiles();
        final int size = ArrayUtils.getSize(dirents);

        long fileBytes = 0;
        for (int i = 0; i < size; ++i) {
            fileBytes += dirents.get(i).length();
        }

        return new Pair<Integer, Long>(size, fileBytes);
    }
}
