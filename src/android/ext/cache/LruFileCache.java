package android.ext.cache;

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
public class LruFileCache extends LruCache<String, String> implements FileCache {
    protected final String mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #LruFileCache(Context, String, int)
     */
    public LruFileCache(String cacheDir, int maxSize) {
        super(maxSize);
        mCacheDir = cacheDir;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param name A relative path within the cache directory, such as
     * <tt>"file_cache"</tt>.
     * @param maxSize The maximum number of files to allow in this cache.
     * @see #LruFileCache(String, int)
     */
    public LruFileCache(Context context, String name, int maxSize) {
        super(maxSize);
        mCacheDir = FileUtils.getCacheDir(context, name).getPath();
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

    @Override
    /* package */ void dump(Context context, Printer printer) {
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

        SimpleFileCache.dumpCachedFiles(context, printer, mCacheDir, result, className);
    }
}
