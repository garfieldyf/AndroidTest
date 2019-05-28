package android.ext.cache;

import java.util.Collections;
import java.util.Map;
import android.content.Context;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.util.ArrayMap;
import android.util.Printer;

/**
 * Class Caches
 * @author Garfield
 */
public final class Caches {
    /**
     * Returns a type-safe empty {@link Cache}. An empty cache
     * is a placeholder to avoid <tt>NullPointerException</tt>
     * and all methods implementation do nothing.
     * @return An empty <tt>Cache</tt>.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> emptyCache() {
        return (Cache<K, V>)EmptyCache.sInstance;
    }

    /**
     * Returns a wrapper on the specified <em>cache</em> which synchronizes
     * all access to the cache.
     * @param cache The {@link Cache} to wrap in a synchronized cache.
     * @return A synchronized <tt>Cache</tt>.
     */
    public static <K, V> Cache<K, V> synchronizedCache(Cache<K, V> cache) {
        DebugUtils.__checkError(cache == null, "cache == null");
        return new SynchronizedCache<K, V>(cache);
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

    public static void dumpCache(Cache<?, ?> cache, Context context, Printer printer) {
        if (cache instanceof SimpleLruCache) {
            ((SimpleLruCache<?, ?>)cache).dump(context, printer);
        } else if (cache instanceof LruImageCache) {
            ((LruImageCache<?>)cache).dump(context, printer);
        } else if (cache instanceof ArrayMapCache) {
            ((ArrayMapCache<?, ?>)cache).dump(context, printer);
        } else if (cache instanceof SynchronizedCache) {
            ((SynchronizedCache<?, ?>)cache).dump(context, printer);
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
        public Map<Object, Object> entries() {
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
     * Class <tt>SynchronizedCache</tt> is an implementation of a {@link Cache}.
     */
    private static final class SynchronizedCache<K, V> implements Cache<K, V> {
        private final Cache<K, V> mCache;

        public SynchronizedCache(Cache<K, V> cache) {
            DebugUtils.__checkError(cache == null, "cache == null");
            mCache = cache;
        }

        @Override
        public synchronized void clear() {
            mCache.clear();
        }

        @Override
        public synchronized V remove(K key) {
            return mCache.remove(key);
        }

        @Override
        public synchronized V get(K key) {
            return mCache.get(key);
        }

        @Override
        public synchronized V put(K key, V value) {
            return mCache.put(key, value);
        }

        @Override
        public synchronized Map<K, V> entries() {
            final Map<K, V> result = new ArrayMap<K, V>();
            result.putAll(mCache.entries());
            return Collections.unmodifiableMap(result);
        }

        public synchronized void dump(Context context, Printer printer) {
            dumpCache(mCache, context, printer);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Caches() {
    }
}
