package android.ext.cache;

import android.content.Context;
import android.ext.util.DebugUtils;
import android.ext.util.Optional;
import android.graphics.Bitmap;
import android.util.ArrayMap;
import android.util.Printer;
import java.util.Map;

/**
 * Class <tt>LruImageCache</tt> is an implementation of a {@link LruCache}.
 * @author Garfield
 */
public final class LruImageCache<K> implements Cache<K, Object> {
    private final Cache<K, Object> mImageCache;
    private final Cache<K, Bitmap> mBitmapCache;

    /**
     * Constructor
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this application maximum
     * memory of the current device. Pass <tt>0</tt> indicates this cache has no bitmap cache.
     * @param maxImageSize The maximum image size in this cache. Pass <tt>0</tt> indicates this cache has no image cache.
     * @param maxPoolSize The maximum number of bitmaps to allow in the internal {@link BitmapPool} of the bitmap cache.
     * Pass <tt>0</tt> indicates this cache has no {@link BitmapPool}.
     * @see #LruImageCache(Cache, Cache)
     */
    public LruImageCache(float scaleMemory, int maxImageSize, int maxPoolSize) {
        this(createBitmapCache(scaleMemory, maxPoolSize), (maxImageSize > 0 ? new LruCache<K, Object>(maxImageSize) : null));
    }

    /**
     * Constructor
     * @param bitmapCache May be <tt>null</tt>. The {@link Cache} to store the bitmaps.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the images.
     * @see #LruImageCache(float, int, int)
     */
    public LruImageCache(Cache<K, Bitmap> bitmapCache, Cache<K, Object> imageCache) {
        mImageCache  = Optional.ofNullable(imageCache);
        mBitmapCache = Optional.ofNullable(bitmapCache);
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
    public Map<K, Object> snapshot() {
        final Map<K, Object> result = new ArrayMap<K, Object>();
        result.putAll(mBitmapCache.snapshot());
        result.putAll(mImageCache.snapshot());
        return result;
    }

    @Override
    public BitmapPool getBitmapPool() {
        return mBitmapCache.getBitmapPool();
    }

    public final void dump(Context context, Printer printer) {
        Caches.dumpCache(mBitmapCache, context, printer);
        Caches.dumpCache(mImageCache, context, printer);
    }

    private static <K> Cache<K, Bitmap> createBitmapCache(float scaleMemory, int maxPoolSize) {
        DebugUtils.__checkError(Float.compare(scaleMemory, 1.0f) >= 0, "scaleMemory >= 1.0");
        return (Float.compare(scaleMemory, +0.0f) > 0 ? (maxPoolSize > 0 ? new LruBitmapCache2<K>(scaleMemory, maxPoolSize) : new LruBitmapCache<K>(scaleMemory)) : null);
    }
}
