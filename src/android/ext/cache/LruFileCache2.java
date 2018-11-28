package android.ext.cache;

import android.ext.util.FileUtils;
import android.os.StatFs;

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
     * @see #LruFileCache2(String, float)
     */
    public LruFileCache2(String cacheDir, int maxSize) {
        super(cacheDir, maxSize);
    }

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param scaleStorage The scale of storage, expressed as a
     * percentage of the total number of bytes supported by the
     * current device.
     * @see #LruFileCache2(String, int)
     */
    public LruFileCache2(String cacheDir, float scaleStorage) {
        super(cacheDir, Math.min((int)(new StatFs(cacheDir).getTotalBytes() * scaleStorage), 300 * 1024 * 1024));
    }

    @Override
    protected int sizeOf(String key, String value) {
        return (int)FileUtils.getFileLength(value);
    }
}
