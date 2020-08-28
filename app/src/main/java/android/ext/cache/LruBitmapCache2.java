package android.ext.cache;

import android.ext.util.DebugUtils;
import android.graphics.Bitmap;

/**
 * Like as {@link LruBitmapCache}, but this class has a {@link BitmapPool}
 * to recycle the evicted bitmap to reused.
 * @author Garfield
 */
public class LruBitmapCache2<K> extends LruBitmapCache<K> {
    private final BitmapPool mBitmapPool;

    /**
     * Constructor
     * @param maxSize The maximum the number of bytes to allow in this cache.
     * @param bitmapPool The {@link BitmapPool} to recycle the evicted bitmap
     * from this cache.
     */
    public LruBitmapCache2(int maxSize, BitmapPool bitmapPool) {
        super(maxSize);
        mBitmapPool = bitmapPool;
        DebugUtils.__checkError(bitmapPool == null, "Invalid parameter - bitmapPool == null");
    }

    /**
     * Returns the {@link BitmapPool} associated with this cache.
     * @return The <tt>BitmapPool</tt>.
     */
    public final BitmapPool getBitmapPool() {
        return mBitmapPool;
    }

    @Override
    protected void entryRemoved(boolean evicted, K key, Bitmap oldValue, Bitmap newValue) {
        if (!evicted && oldValue != newValue) {
            mBitmapPool.put(oldValue);
        }
    }
}
