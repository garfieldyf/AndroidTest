package android.ext.cache;

import java.io.File;
import android.content.Context;
import android.ext.util.DebugUtils;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Printer;

/**
 * Like as {@link LruFileCache}, but the {@link #maxSize()}
 * is the maximum sum of the bytes of the files in this cache.
 * @author Garfield
 */
public class LruFileCache2 extends LruFileCache {
    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum sum of the bytes of the files
     * to allow in this cache.
     * @see #LruFileCache2(File, float)
     */
    public LruFileCache2(File cacheDir, int maxSize) {
        super(cacheDir, maxSize);
    }

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param scaleStorage The scale of storage, expressed as a
     * percentage of the total number of bytes supported by the
     * current device.
     * @see #LruFileCache2(File, int)
     */
    public LruFileCache2(File cacheDir, float scaleStorage) {
        super(cacheDir, Math.min((int)(new StatFs(cacheDir.getPath()).getTotalBytes() * scaleStorage), 300 * 1024 * 1024));
    }

    @Override
    protected int sizeOf(String key, File file) {
        return (int)file.length();
    }

    @Override
    /* package */ void dumpSummary(Context context, Printer printer, StringBuilder result, int count) {
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ count = %d, size = %s, maxSize = %s ] ", getClass().getSimpleName(), count, Formatter.formatFileSize(context, size()), Formatter.formatFileSize(context, maxSize()));
    }
}
