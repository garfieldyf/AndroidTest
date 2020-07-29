package android.ext.cache;

import android.content.Context;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.util.Printer;

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
    public void dump(Context context, Printer printer) {
        super.dump(context, printer);
        if (mBitmapPool instanceof LinkedBitmapPool) {
            ((LinkedBitmapPool)mBitmapPool).dump(context, printer);
        }
    }

    @Override
    protected void trimToSize(int maxSize, boolean evicted) {
        super.trimToSize(maxSize, evicted);
        mBitmapPool.clear();
    }

    @Override
    protected void entryRemoved(boolean evicted, K key, Bitmap oldValue, Bitmap newValue) {
        if (!evicted && oldValue != newValue && oldValue.isMutable() && !oldValue.isRecycled()) {
            mBitmapPool.put(oldValue);
        }
    }
}
