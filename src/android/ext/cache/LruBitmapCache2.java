package android.ext.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Printer;

/**
 * Like as {@link LruBitmapCache}, but this class has a {@link BitmapPool}
 * to recycle the evicted bitmap to reused.
 * @author Garfield
 */
public class LruBitmapCache2<K> extends LruBitmapCache<K> implements ImageCache<K, Bitmap> {
    protected final BitmapPool mBitmapPool;

    /**
     * Constructor
     * @param maxSize The maximum the number of bytes to allow in this cache.
     * @param bitmapPool The {@link BitmapPool} to recycle the evicted bitmap
     * from this cache.
     * @see #LruBitmapCache2(float, int)
     */
    public LruBitmapCache2(int maxSize, BitmapPool bitmapPool) {
        super(maxSize);
        mBitmapPool = bitmapPool;
    }

    /**
     * Constructor
     * @param scaleMemory The scale of memory, expressed as a percentage
     * of this application maximum memory of the current device.
     * @param maxPoolSize The maximum number of bitmaps to allow in the
     * internal {@link BitmapPool}.
     * @see #LruBitmapCache2(int, BitmapPool)
     */
    public LruBitmapCache2(float scaleMemory, int maxPoolSize) {
        super(scaleMemory);
        mBitmapPool = new LinkedBitmapPool(maxPoolSize);
    }

    @Override
    public void clear() {
        super.clear();
        mBitmapPool.clear();
    }

    @Override
    public BitmapPool getBitmapPool() {
        return mBitmapPool;
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
