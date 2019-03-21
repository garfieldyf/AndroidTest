package android.ext.cache;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.text.format.Formatter;
import android.util.Pair;
import android.util.Printer;

/**
 * Class Caches
 * @author Garfield
 */
public final class Caches {
    /**
     * Returns a type-safe empty {@link Cache} associated with this class.
     * An empty cache is a placeholder to avoid <tt>NullPointerException</tt>
     * and all methods implementation do nothing.
     * @return An empty <tt>Cache</tt>.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> emptyCache() {
        return (Cache<K, V>)EmptyCache.sInstance;
    }

    /**
     * Returns a new <tt>Bitmap</tt> {@link Cache} instance.
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this
     * application maximum memory of the current device.
     * @param maxPoolSize May be <tt>0</tt>. The maximum number of bitmaps to allow in the internal
     * bitmap pool of the bitmap cache.
     * @return A new <tt>Bitmap Cache</tt> instance or <tt>null</tt> if the <tt>scaleMemory <= 0.
     */
    public static <K> Cache<K, Bitmap> createBitmapCache(float scaleMemory, int maxPoolSize) {
        return (Float.compare(scaleMemory, +0.0f) > 0 ? (maxPoolSize > 0 ? new LruBitmapCache2<K>(scaleMemory, maxPoolSize) : new LruBitmapCache<K>(scaleMemory)) : null);
    }

    public static void dumpCache(Object cache, Context context, Printer printer) {
        if (cache instanceof SimpleLruCache) {
            ((SimpleLruCache<?, ?>)cache).dump(context, printer);
        } else if (cache instanceof SimpleFileCache) {
            ((SimpleFileCache)cache).dump(context, printer);
        } else if (cache instanceof LruImageCache) {
            ((LruImageCache<?>)cache).dump(context, printer);
        } else if (cache instanceof ArrayMapCache) {
            ((ArrayMapCache<?, ?>)cache).dump(context, printer);
        }
    }

    /* package */ static void dumpCacheFiles(Context context, Printer printer, File cacheDir, StringBuilder result, String className) {
        final File[] files = cacheDir.listFiles();
        final int size = ArrayUtils.getSize(files);
        result.setLength(0);
        if (size > 0) {
            Arrays.sort(files);
        }

        long fileCount = 0, fileLength = 0;
        for (int i = 0, index = 0; i < size; ++i) {
            final File file = files[i];
            if (file.isDirectory()) {
                ++index;
                final Pair<Integer, Long> pair = getFileCount(file);
                result.append("  ").append(file.getName()).append(" { files = ").append(pair.first).append(", size = ").append(Formatter.formatFileSize(context, pair.second)).append(" }");

                fileCount  += pair.first;
                fileLength += pair.second;
            }

            if ((index % 4) == 0) {
                result.append('\n');
            }
        }

        DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping %s disk cache [ dirs = %d, files = %d, size = %s ] ", className, size, fileCount, Formatter.formatFileSize(context, fileLength));
        if (result.length() > 0) {
            printer.println(result.toString());
        }
    }

    private static Pair<Integer, Long> getFileCount(File dir) {
        final File[] files  = dir.listFiles();
        final int fileCount = ArrayUtils.getSize(files);

        long fileLength = 0;
        for (int i = 0; i < fileCount; ++i) {
            fileLength += files[i].length();
        }

        return new Pair<Integer, Long>(fileCount, fileLength);
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
     * This utility class cannot be instantiated.
     */
    private Caches() {
    }
}
