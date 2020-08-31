package android.ext.cache;

import android.content.Context;
import android.ext.util.DebugUtils;
import android.util.Printer;

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

    public static void dumpCache(Object cache, Context context, Printer printer) {
        if (cache instanceof ArrayMapCache) {
            ((ArrayMapCache<?, ?>)cache).dump(printer);
        } else if (cache instanceof LruFileCache) {
            ((LruFileCache)cache).dump(printer);
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

        @Override
        public void clear() {
        }

        @Override
        public Object remove(Object key) {
            DebugUtils.__checkError(key == null, "Invalid parameter - key == null");
            return null;
        }

        @Override
        public Object get(Object key) {
            DebugUtils.__checkError(key == null, "Invalid parameter - key == null");
            return null;
        }

        @Override
        public Object put(Object key, Object value) {
            DebugUtils.__checkError(key == null || value == null, "Invalid parameters - key == null || value == null");
            return null;
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Caches() {
    }
}
