package android.ext.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Printer;

/**
 * Class <tt>LruImageCache</tt> is an implementation of a {@link Cache}.
 * @author Garfield
 */
public final class LruImageCache<K> implements Cache<K, Object> {
    private final Cache<K, Object> mImageCache;
    private final Cache<K, Bitmap> mBitmapCache;

    /**
     * Constructor
     * @param bitmapCache May be <tt>null</tt>. The {@link Cache} to store the bitmaps.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the images.
     */
    public LruImageCache(Cache<K, Bitmap> bitmapCache, Cache<K, Object> imageCache) {
        mImageCache  = (imageCache != null ? imageCache : Caches.emptyCache());
        mBitmapCache = (bitmapCache != null ? bitmapCache : Caches.emptyCache());
    }

    @Override
    public void clear() {
        mImageCache.clear();
        mBitmapCache.clear();
    }

    @Override
    public Object remove(K key) {
        final Object value = mBitmapCache.remove(key);
        return (value != null ? value : mImageCache.remove(key));
    }

    @Override
    public Object get(K key) {
        final Object value = mBitmapCache.get(key);
        return (value != null ? value : mImageCache.get(key));
    }

    @Override
    public Object put(K key, Object value) {
        return (value instanceof Bitmap ? mBitmapCache.put(key, (Bitmap)value) : mImageCache.put(key, value));
    }

    @Override
    public void trimMemory(int level) {
        mImageCache.trimMemory(level);
        mBitmapCache.trimMemory(level);
    }

    @Override
    public BitmapPool getBitmapPool() {
        return mBitmapCache.getBitmapPool();
    }

    public final void dump(Context context, Printer printer) {
        Caches.dumpCache(mBitmapCache, context, printer);
        Caches.dumpCache(mImageCache, context, printer);
    }
}
