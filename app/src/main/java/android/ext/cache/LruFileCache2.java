package android.ext.cache;

import android.content.Context;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.util.Printer;
import java.io.File;

/**
 * Like as {@link LruFileCache}, but the {@link #maxSize()}
 * is the maximum sum of the bytes of the files in this cache.
 * @author Garfield
 */
public class LruFileCache2 extends LruFileCache {
    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum sum of the bytes of the files to allow in this cache.
     * @see #LruFileCache2(Context, String, int)
     */
    public LruFileCache2(File cacheDir, int maxSize) {
        super(cacheDir, maxSize);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param name A relative path within the cache directory, such as <tt>"file_cache"</tt>.
     * @param maxSize The maximum sum of the bytes of the files to allow in this cache.
     * @see #LruFileCache2(File, int)
     */
    public LruFileCache2(Context context, String name, int maxSize) {
        super(FileUtils.getCacheDir(context, name), maxSize);
    }

    @Override
    protected int sizeOf(String key, File file) {
        return (int)file.length();
    }

    @Override
    /* package */ void dumpSummary(Printer printer, StringBuilder result, int count) {
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ count = %d, size = %s, maxSize = %s ] ", getClass().getSimpleName(), count, FileUtils.formatFileSize(size()), FileUtils.formatFileSize(maxSize()));
    }
}
