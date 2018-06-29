package android.ext.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Printer;

/**
 * Class <tt>LruImageCache</tt> is an implementation of a {@link LruCache}.
 * @author Garfield
 * @version 1.0
 */
public final class LruImageCache<K, Image> implements Cache<K, Object> {
    private final Cache<K, Image> mImageCache;
    private final Cache<K, Bitmap> mBitmapCache;

    /**
     * Constructor
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this application maximum
     * memory of the current device. Pass <tt>0</tt> that this cache has no bitmap cache.
     * @param maxImageSize The maximum number of images in this cache. Pass <tt>0</tt> that this cache has no image cache.
     * @see #LruImageCache(Cache, Cache)
     * @see #LruImageCache(float, int, int)
     */
    public LruImageCache(float scaleMemory, int maxImageSize) {
        this(Caches.<K>createBitmapCache(scaleMemory, 0), (maxImageSize > 0 ? new LruCache<K, Image>(maxImageSize) : null));
    }

    /**
     * Constructor
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this application maximum
     * memory of the current device. Pass <tt>0</tt> that this cache has no bitmap cache.
     * @param maxImageSize The maximum number of images in this cache. Pass <tt>0</tt> that this cache has no image cache.
     * @param maxBitmapSize The maximum number of bitmaps in the bitmap pool to recycle the evicted bitmap from the bitmap
     * cache. If the <em>scaleMemory <= 0</em> that this cache has no bitmap pool.
     * @see #LruImageCache(float, int)
     * @see #LruImageCache(Cache, Cache)
     */
    public LruImageCache(float scaleMemory, int maxImageSize, int maxBitmapSize) {
        this(Caches.<K>createBitmapCache(scaleMemory, maxBitmapSize), (maxImageSize > 0 ? new LruCache<K, Image>(maxImageSize) : null));
    }

    /**
     * Constructor
     * @param bitmapCache May be <tt>null</tt>. The {@link Cache} to store the bitmaps.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the images.
     * @see #LruImageCache(float, int)
     * @see #LruImageCache(float, int, int)
     */
    public LruImageCache(Cache<K, Bitmap> bitmapCache, Cache<K, Image> imageCache) {
        mImageCache  = (imageCache != null ? imageCache : Caches.<K, Image>emptyCache());
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
    @SuppressWarnings("unchecked")
    public Object put(K key, Object value) {
        return (value instanceof Bitmap ? mBitmapCache.put(key, (Bitmap)value) : mImageCache.put(key, (Image)value));
    }

    /**
     * Returns the {@link BitmapPool} associated with this cache.
     * @return The <tt>BitmapPool</tt> or <tt>null</tt>.
     */
    public final BitmapPool getBitmapPool() {
        return (mBitmapCache instanceof LruBitmapCache2 ? ((LruBitmapCache2<?>)mBitmapCache).getBitmapPool() : null);
    }

    /* package */ final void dump(Context context, Printer printer) {
        Caches.dumpCache(mBitmapCache, context, printer);
        Caches.dumpCache(mImageCache, context, printer);
    }
}