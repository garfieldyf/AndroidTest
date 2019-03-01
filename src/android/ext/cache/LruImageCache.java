package android.ext.cache;

import java.util.Collections;
import java.util.Map;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.ArrayMap;
import android.util.Printer;

/**
 * Class <tt>LruImageCache</tt> is an implementation of a {@link LruCache}.
 * @author Garfield
 */
public final class LruImageCache<K> implements ImageCache<K, Object> {
    private final Cache<K, Object> mImageCache;
    private final Cache<K, Bitmap> mBitmapCache;

    /**
     * Constructor
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this application maximum
     * memory of the current device. Pass <tt>0</tt> that this cache has no bitmap cache.
     * @param maxImageSize The maximum image size in this cache. Pass <tt>0</tt> that this cache has no image cache.
     * @see #LruImageCache(Cache, Cache)
     */
    public LruImageCache(float scaleMemory, int maxImageSize) {
        this(Caches.<K>createBitmapCache(scaleMemory, 0), (maxImageSize > 0 ? new LruCache<K, Object>(maxImageSize) : null));
    }

    /**
     * Constructor
     * @param bitmapCache May be <tt>null</tt>. The {@link Cache} to store the bitmaps.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the images.
     * @see #LruImageCache(float, int)
     */
    public LruImageCache(Cache<K, Bitmap> bitmapCache, Cache<K, Object> imageCache) {
        mImageCache  = (imageCache != null ? imageCache : Caches.<K, Object>emptyCache());
        mBitmapCache = (bitmapCache != null ? bitmapCache : Caches.<K, Bitmap>emptyCache());
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
    public Map<K, Object> entries() {
        final Map<K, Object> result = new ArrayMap<K, Object>();
        result.putAll(mBitmapCache.entries());
        result.putAll(mImageCache.entries());
        return Collections.unmodifiableMap(result);
    }

    @Override
    public BitmapPool getBitmapPool() {
        return (mBitmapCache instanceof ImageCache ? ((ImageCache<?, ?>)mBitmapCache).getBitmapPool() : null);
    }

    /* package */ final void dump(Context context, Printer printer) {
        Caches.dumpCache(mBitmapCache, context, printer);
        Caches.dumpCache(mImageCache, context, printer);
    }
}
