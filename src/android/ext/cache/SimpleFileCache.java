package android.ext.cache;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import android.content.Context;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.util.Printer;

/**
 * Class <tt>SimpleFileCache</tt> is an implementation of a {@link FileCache}.
 * This class is an <b>unlimited-size</b> cache.
 * @author Garfield
 */
public final class SimpleFileCache implements FileCache {
    private final File mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @see #SimpleFileCache(Context, String)
     */
    public SimpleFileCache(File cacheDir) {
        mCacheDir = cacheDir;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param name A relative path within the cache directory,
     * such as <tt>"simple_file_cache"</tt>.
     * @see #SimpleFileCache(File)
     */
    public SimpleFileCache(Context context, String name) {
        mCacheDir = FileUtils.getCacheDir(context, name);
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    @Override
    public void clear() {
    }

    @Override
    public File remove(String key) {
        final File result = get(key);
        return (result.delete() ? result : null);
    }

    /**
     * @return Always returns an empty (<tt>0-size</tt>), unmodifiable {@link Map}.
     */
    @Override
    public Map<String, File> entries() {
        return Collections.emptyMap();
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

    /* package */ final void dump(Context context, Printer printer) {
        Caches.dumpCacheFiles(context, printer, mCacheDir, new StringBuilder(130), getClass().getSimpleName());
    }
}
