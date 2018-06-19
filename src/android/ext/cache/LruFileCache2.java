package android.ext.cache;

import android.ext.util.FileUtils;

/**
 * Like as {@link LruFileCache}, but the {@link #maxSize()}
 * is the maximum sum of the bytes of the files in this cache.
 * @author Garfield
 * @version 1.0
 */
public class LruFileCache2 extends LruFileCache {
    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @param maxSize The maximum sum of the bytes of the files
     * to allow in this cache.
     */
    public LruFileCache2(String cacheDir, int maxSize) {
        super(cacheDir, maxSize);
    }

    @Override
    protected int sizeOf(String key, String value) {
        return (int)FileUtils.getFileLength(value);
    }
}
