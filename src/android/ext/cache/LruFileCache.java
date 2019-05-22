package android.ext.cache;

import java.io.File;
import java.util.Collection;
import android.content.Context;
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
    public File get(String key) {
        final File result = super.get(key);
        return (result != null ? result : buildCacheFile(key));
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, File oldFile, File newFile) {
        if (evicted || !oldFile.equals(newFile)) {
            oldFile.delete();
        }
    }

    /**
     * Builds the cache file with the specified <em>key</em>. <p>The method is called without
     * synchronization: other threads may access the cache while this method is executing.</p>
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

        Caches.dumpCacheFiles(context, printer, mCacheDir, result, getClass().getSimpleName());
    }

    /* package */ void dumpSummary(Context context, Printer printer, StringBuilder result, int count) {
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ size = %d, maxSize = %d ] ", getClass().getSimpleName(), count, maxSize());
    }
}
