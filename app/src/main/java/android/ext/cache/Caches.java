package android.ext.cache;

import android.content.Context;
import android.ext.image.params.Parameters;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.Printer;
import java.util.Collections;
import java.util.Map;

/**
 * Class Caches
 * @author Garfield
 */
public final class Caches {
    /**
     * Returns a type-safe empty {@link Cache}. An empty
     * <tt>Cache</tt> all methods implementation do nothing.
     * @return An empty <tt>Cache</tt>.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> emptyCache() {
        return (Cache<K, V>)EmptyCache.sInstance;
    }

    /**
     * Returns an empty {@link BitmapPool}. An empty <tt>BitmapPool</tt>
     * all methods implementation do nothing.
     * @return An empty <tt>BitmapPool</tt>.
     */
    public static BitmapPool emptyBitmapPool() {
        return EmptyBitmapPool.sInstance;
    }

    /**
     * Returns a new <tt>Bitmap</tt> {@link Cache} instance.
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this
     * application maximum memory of the current device.
     * @param maxPoolSize The maximum number of bitmaps to allow in the internal {@link BitmapPool}
     * of the bitmap cache. Pass <tt>0</tt> indicates the bitmap cache has no {@link BitmapPool}.
     * @return A new <tt>Bitmap Cache</tt> instance or <tt>null</tt> if the <tt>scaleMemory <= 0.
     */
    public static <K> Cache<K, Bitmap> createBitmapCache(float scaleMemory, int maxPoolSize) {
        DebugUtils.__checkError(Float.compare(scaleMemory, 1.0f) >= 0, "scaleMemory >= 1.0");
        return (Float.compare(scaleMemory, +0.0f) > 0 ? (maxPoolSize > 0 ? new LruBitmapCache2<K>(scaleMemory, maxPoolSize) : new LruBitmapCache<K>(scaleMemory)) : null);
    }

    public static void dumpCache(Object cache, Context context, Printer printer) {
        if (cache instanceof LruFileCache) {
            ((LruFileCache)cache).dump(context, printer);
        } else if (cache instanceof SimpleLruCache) {
            ((SimpleLruCache<?, ?>)cache).dump(context, printer);
        } else if (cache instanceof LruImageCache) {
            ((LruImageCache<?>)cache).dump(context, printer);
        }
    }

    /**
     * Class <tt>EmptyCache</tt> is an implementation of a {@link Cache}.
     */
    private static final class EmptyCache implements Cache<Object, Object> {
        public static final EmptyCache sInstance = new EmptyCache();

        /**
         * Always returns an empty (<tt>0-size</tt>), immutable {@link Map}.
         * @return An immutable <tt>Map</tt>.
         */
        @Override
        public Map<Object, Object> snapshot() {
            return Collections.emptyMap();
        }

        @Override
        public void clear() {
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
     * Class <tt>EmptyBitmapPool</tt> is an implementation of a {@link BitmapPool}.
     */
    private static final class EmptyBitmapPool implements BitmapPool {
        public static final BitmapPool sInstance = new EmptyBitmapPool();

        @Override
        public void clear() {
        }

        @Override
        public Bitmap get(int size) {
            return null;
        }

        @Override
        public Bitmap get(Context context, Parameters parameters, Options opts) {
            return null;
        }

        @Override
        public void put(Bitmap bitmap) {
            DebugUtils.__checkError(bitmap == null, "bitmap == null");
            DebugUtils.__checkWarning(!bitmap.isMutable(), "EmptyBitmapPool", "The bitmap is immutable, couldn't recycle to reused.");
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Caches() {
    }
}
