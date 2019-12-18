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

    public static void dumpCache(Object cache, Context context, Printer printer) {
        if (cache instanceof SimpleFileCache) {
            ((SimpleFileCache)cache).dump(context, printer);
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
        public Bitmap get(Parameters parameters, Options opts) {
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
