package android.ext.util;

import android.ext.cache.BitmapPool;
import android.ext.cache.Cache;
import android.ext.cache.Caches;

/**
 * Class Optional
 * @author Garfield
 */
public final class Optional {
    /**
     * Returns a {@link Cancelable} from the specified <em>cancelable</em>,
     * if non-null. Otherwise returns an empty {@code Cancelable}.
     */
    public static Cancelable ofNullable(Cancelable cancelable) {
        return (cancelable != null ? cancelable : EmptyCancelable.sInstance);
    }

    /**
     * Returns a {@link Cache} from the specified <em>cache</em>,
     * if non-null. Otherwise returns an empty {@code Cache}.
     * @see Caches#emptyCache()
     */
    public static <K, V> Cache<K, V> ofNullable(Cache<K, V> cache) {
        return (cache != null ? cache : Caches.emptyCache());
    }

    /**
     * Returns a {@link BitmapPool} from the specified <em>bitmapPool</em>,
     * if non-null. Otherwise returns an empty {@code BitmapPool}.
     * @see Caches#emptyBitmapPool()
     */
    public static BitmapPool ofNullable(BitmapPool bitmapPool) {
        return (bitmapPool != null ? bitmapPool : Caches.emptyBitmapPool());
    }

    /**
     * Class <tt>EmptyCancelable</tt> is an implementation of a {@link Cancelable}.
     */
    private static final class EmptyCancelable implements Cancelable {
        public static final Cancelable sInstance = new EmptyCancelable();

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Optional() {
    }
}
