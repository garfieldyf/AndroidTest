package android.ext.cache;

import java.util.Collections;
import java.util.Map;
import android.content.Context;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.util.Printer;

/**
 * Class Caches
 * @author Garfield
 * @version 1.0
 */
public final class Caches {
    /**
     * Returns a type-safe empty {@link Cache} associated with this class.
     * @return An empty <tt>Cache</tt>.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> emptyCache() {
        return (Cache<K, V>)EmptyCache.sInstance;
    }

    /**
     * Returns a new {@link FileCache} instance.
     * @param context The <tt>Context</tt>.
     * @param name A relative path within the cache directory, such as <tt>"file_cache"</tt>.
     * @param maxFileSize The maximum number of files in the file cache. Pass <tt>0</tt> will
     * be returned the {@link SimpleFileCache} instance.
     * @return A new {@link FileCache} instance.
     */
    public static FileCache createFileCache(Context context, String name, int maxFileSize) {
        return (maxFileSize > 0 ? new LruFileCache(context, name, maxFileSize) : new SimpleFileCache(context, name));
    }

    /**
     * Returns a new <tt>Bitmap</tt> {@link Cache} instance.
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this
     * application maximum memory of the current device.
     * @param maxBitmapSize The maximum number of bitmaps in the bitmap pool to recycle the evicted
     * bitmap from the bitmap cache. Pass <tt>0</tt> that the bitmap cache has no bitmap pool.
     * @return A new <tt>Bitmap</tt> {@link Cache} instance.
     */
    public static <K> Cache<K, Bitmap> createBitmapCache(float scaleMemory, int maxBitmapSize) {
        return (Float.compare(scaleMemory, +0.0f) > 0 ? (maxBitmapSize > 0 ? new LruBitmapCache2<K>(scaleMemory, new LinkedBitmapPool(maxBitmapSize)) : new LruBitmapCache<K>(scaleMemory)) : null);
    }

    public static void dumpCache(Object cache, Context context, Printer printer) {
        if (cache instanceof SimpleLruCache) {
            ((SimpleLruCache<?, ?>)cache).dump(context, printer);
        } else if (cache instanceof SimpleFileCache) {
            ((SimpleFileCache)cache).dump(context, printer);
        } else if (cache instanceof LruImageCache) {
            ((LruImageCache<?, ?>)cache).dump(context, printer);
        }
    }

    /**
     * Class <tt>EmptyCache</tt> is an implementation of a {@link Cache}.
     */
    private static final class EmptyCache implements Cache<Object, Object> {
        public static final EmptyCache sInstance = new EmptyCache();

        @Override
        public void clear() {
        }

        @Override
        public Map<Object, Object> snapshot() {
            return Collections.emptyMap();
        }

        @Override
        public Object remove(Object key) {
            DebugUtils.__checkError(key == null, "key == null");
            return null;
        }

        @Override
        public Object get(Object key) {
            DebugUtils.__checkError(key == null, "key == null");
            return null;
        }

        @Override
        public Object put(Object key, Object value) {
            DebugUtils.__checkError(key == null || value == null, "key == null || value == null");
            return null;
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Caches() {
    }
}
