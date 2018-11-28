package android.ext.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Printer;

/**
 * Like as {@link LruBitmapCache}, but this class has a {@link BitmapPool}
 * to recycle the evicted bitmap to reused.
 * @author Garfield
 */
public class LruBitmapCache2<K> extends LruBitmapCache<K> {
    protected final BitmapPool mBitmapPool;

    /**
     * Constructor
     * @param maxSize The maximum the number of bytes to allow in this cache.
     * @param bitmapPool The {@link BitmapPool} to recycle the evicted bitmap
     * from this cache.
     * @see #LruBitmapCache2(float, BitmapPool)
     */
    public LruBitmapCache2(int maxSize, BitmapPool bitmapPool) {
        super(maxSize);
        mBitmapPool = bitmapPool;
    }

    /**
     * Constructor
     * @param scaleMemory The scale of memory, expressed as a percentage
     * of this application maximum memory of the current device.
     * @param bitmapPool The {@link BitmapPool} to recycle the evicted
     * bitmap from this cache.
     * @see #LruBitmapCache2(int, BitmapPool)
     */
    public LruBitmapCache2(float scaleMemory, BitmapPool bitmapPool) {
        super(scaleMemory);
        mBitmapPool = bitmapPool;
    }

    /**
     * Returns the {@link BitmapPool} associated with this cache.
     * @return The <tt>BitmapPool</tt>.
     */
    public final BitmapPool getBitmapPool() {
        return mBitmapPool;
    }

    @Override
    public void clear() {
        super.clear();
        mBitmapPool.clear();
    }

    @Override
    protected void entryRemoved(boolean evicted, K key, Bitmap oldValue, Bitmap newValue) {
        if (evicted || oldValue != newValue) {
            mBitmapPool.put(oldValue);
        }
    }

    @Override
    /* package */ void dump(Context context, Printer printer) {
        super.dump(context, printer);
        if (mBitmapPool instanceof LinkedBitmapPool) {
            ((LinkedBitmapPool)mBitmapPool).dump(context, printer);
        }
    }
}
