package android.ext.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import android.content.Context;
import android.ext.graphics.BitmapUtils;
import android.ext.util.FileUtils.Dirent;
import android.graphics.Bitmap;
import android.text.format.Formatter;
import android.util.Pair;
import android.util.Printer;

/**
 * Class Caches
 * @author Garfield
 * @version 4.0
 */
public final class Caches {
    /**
     * An interface for a pool that allows users to reuse {@link Bitmap} objects.
     */
    public static interface BitmapPool {
        /**
         * Removes all bitmaps from this pool, leaving it empty.
         */
        void clear();

        /**
         * Retrieves the <tt>Bitmap</tt> in this pool that it's allocation
         * bytes greater than or equal to the given the <em>size</em>.
         * @param size The bytes to match.
         * @return The <tt>Bitmap</tt> or <tt>null</tt> if there is no match
         * the bitmap.
         */
        Bitmap get(int size);

        /**
         * Sets the specified <em>bitmap</em> to this pool.
         * @param bitmap The <tt>Bitmap</tt>.
         */
        void put(Bitmap bitmap);
    }

    /**
     * A <tt>Cache</tt> is a data structure consisting of a set
     * of keys and values in which each key is mapped to a value.
     */
    public static interface Cache<K, V> {
        /**
         * Removes all elements from this cache, leaving it empty.
         * @see #remove(K)
         */
        void clear();

        /**
         * Removes the value for the specified <em>key</em>.
         * @param key The key to remove.
         * @return The value mapped by <em>key</em>
         * or <tt>null</tt> if there was no mapping.
         * @see #clear()
         */
        V remove(K key);

        /**
         * Returns the value of the mapping with the specified <em>key</em>.
         * @param key The key to find.
         * @return The value or <tt>null</tt> if there was no mapping.
         * @see #put(K, V)
         */
        V get(K key);

        /**
         * Maps the specified <em>key</em> to the specified <tt>value</tt>.
         * @param key The key.
         * @param value The value.
         * @return The previous value mapped by <em>key</em>
         * or <tt>null</tt> if there was no mapping.
         * @see #get(K)
         */
        V put(K key, V value);
    }

    /**
     * A <tt>FileCache</tt> is a data structure consisting of a set
     * of keys and files in which each key is mapped to a single file.
     */
    public static interface FileCache extends Cache<String, String> {
        /**
         * Returns the absolute path of the cache directory.
         * @return The absolute path of the cache directory.
         */
        String getCacheDir();

        /**
         * Removes all files from this cache, leaving it empty,
         * but do <b>not</b> delete cache file from filesystem.
         * @see #remove(String)
         */
        void clear();

        /**
         * Removes the cache file for the specified <em>key</em> in this cache.
         * The cache file will be delete from filesystem.
         * @param key The key to remove.
         * @return The cache file mapped by <em>key</em> or <tt>null</tt> if
         * there was no mapping.
         * @see #clear()
         */
        String remove(String key);

        /**
         * Returns the cache file of the mapping with the specified <em>key</em>
         * in this cache. If no mapping for the specified <em>key</em> is found,
         * returns a <tt>String</tt> contains the absolute path of the cache file.
         * @param key The key to find.
         * @return The absolute path of the cache file. Never <tt>null</tt>.
         * @see #put(String, String)
         */
        String get(String key);

        /**
         * Maps the specified <em>key</em> to the specified <em>cacheFile</em>.
         * The previous cache file will be delete from filesystem.
         * @param key The key.
         * @param cacheFile The absolute path of the cache file.
         * @return The previous cache file mapped by <em>key</em> or <tt>null</tt>
         * if there was no mapping.
         * @see #get(String)
         */
        String put(String key, String cacheFile);
    }

    /**
     * A <tt>SimpleLruCache</tt> that holds strong references to a limited number
     * of values. Each time a value is accessed, it is moved to the head of a queue.
     * When a value is added to a full cache, the value at the end of that queue is
     * evicted and may become eligible for garbage collection. Unlike {@link LruCache}
     * this class is <b>not</b> thread-safely.
     */
    public static class SimpleLruCache<K, V> implements Cache<K, V> {
        /* package */ final int maxSize;
        /* package */ final Map<K, V> map;

        /**
         * Constructor
         * @param maxSize The maximum number of values to allow in this cache.
         */
        public SimpleLruCache(int maxSize) {
            DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
            this.maxSize = maxSize;
            this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
        }

        /**
         * Returns the number of entries in this cache.
         * @return The number of entries in this cache.
         * @see #maxSize()
         */
        public int size() {
            return map.size();
        }

        /**
         * Returns the maximum size in this cache in user-defined units.
         * @return The maximum size.
         * @see #size()
         */
        public final int maxSize() {
            return maxSize;
        }

        /**
         * Clears this cache, but do not call {@link #entryRemoved} on each removed entry.
         * @see #evictAll()
         * @see #trimToSize(int)
         */
        @Override
        public void clear() {
            map.clear();
        }

        /**
         * @see #put(K, V)
         * @see #remove(K)
         */
        @Override
        public V get(K key) {
            DebugUtils.__checkError(key == null, "key == null");
            return map.get(key);
        }

        /**
         * Maps the specified <em>key</em> to the specified <tt>value</tt>.
         * The <tt>value</tt> is moved to the head of the queue.
         * @param key The key.
         * @param value The value.
         * @return The previous value mapped by <em>key</em> or <tt>null</tt>
         * if there was no mapping.
         * @see #get(K)
         * @see #remove(K)
         */
        @Override
        public V put(K key, V value) {
            DebugUtils.__checkError(key == null || value == null, "key == null || value == null");
            final V previous = putImpl(key, value);
            if (previous != null) {
                entryRemoved(false, key, previous, value);
            }

            trimToSize(maxSize, false);
            return previous;
        }

        /**
         * @see #get(K)
         * @see #put(K, V)
         */
        @Override
        public V remove(K key) {
            DebugUtils.__checkError(key == null, "key == null");
            final V previous = removeImpl(key);
            if (previous != null) {
                entryRemoved(false, key, previous, null);
            }

            return previous;
        }

        /**
         * Clears this cache, calling {@link #entryRemoved} on each removed entry.
         * @see #clear()
         * @see #trimToSize(int)
         */
        public void evictAll() {
            trimToSize(-1, true);
        }

        /**
         * Remove the eldest entries until the total of remaining entries is
         * at or below the requested size.
         * @param maxSize The maximum size of the cache. May be <tt>-1</tt>
         * to evict all entries.
         * @see #clear()
         * @see #evictAll()
         */
        public void trimToSize(int maxSize) {
            trimToSize(maxSize, true);
        }

        /**
         * Returns all entries of this cache.
         * @return An unmodifiable {@link Map} of the entries.
         */
        public Map<K, V> entries() {
            return Collections.unmodifiableMap(map);
        }

        @Override
        public String toString() {
            return new StringBuilder(64)
                .append(getClass().getSimpleName())
                .append(" { size = ").append(map.size())
                .append(", maxSize = ").append(maxSize)
                .append(" }").toString();
        }

        /**
         * Called for entries that have been evicted or removed. This method is invoked when a value
         * is evicted to make space, removed by a call to {@link #remove}, or replaced by a call to
         * {@link #put}. The default implementation does nothing. <p>The method is called without
         * synchronization: other threads may access the cache while this method is executing.</p>
         * @param evicted If <tt>true</tt> the entry is being removed to make space, <tt>false</tt>
         * if the removal was caused by a {@link #put} or {@link #remove}.
         * @param key The key.
         * @param oldValue The old value for <em>key</em>.
         * @param newValue The new value for <em>key</em> or <tt>null</tt>.
         */
        protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
        }

        /**
         * Removes the entry for the specified <em>key</em>.
         */
        /* package */ V removeImpl(K key) {
            return map.remove(key);
        }

        /**
         * Maps the specified <em>key</em> to the specified <tt>value</tt>.
         */
        /* package */ V putImpl(K key, V value) {
            return map.put(key, value);
        }

        /**
         * Remove the eldest entries until the total of remaining entries
         * is at or below the requested size.
         */
        /* package */ void trimToSize(int maxSize, boolean evicted) {
            final Iterator<Entry<K, V>> itor = map.entrySet().iterator();
            while (itor.hasNext() && map.size() > maxSize) {
                final Entry<K, V> toEvict = itor.next();
                itor.remove();
                entryRemoved(evicted, toEvict.getKey(), toEvict.getValue(), null);
            }
        }

        /* package */ final void dump(Printer printer, Map<?, ?> map) {
            final StringBuilder result = new StringBuilder(256);
            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ count = %d, size = %d, maxSize = %d ] ", getClass().getSimpleName(), map.size(), size(), maxSize());
            for (Entry<?, ?> entry : map.entrySet()) {
                result.setLength(0);
                printer.println(result.append("  ").append(entry.getKey()).append(" ==> ").append(entry.getValue()).toString());
            }
        }
    }

    /**
     * Like as {@link SimpleLruCache}, but this class is thread-safely.
     * By default, this cache size is the number of entries. Overrides
     * {@link #sizeOf} to size this cache in different units.
     */
    public static class LruCache<K, V> extends SimpleLruCache<K, V> {
        /**
         * The size of this cache in units. Not necessarily the number of entries.
         */
        private int size;

        /**
         * Constructor
         * @param maxSize For caches that do not override {@link #sizeOf}, this is
         * the maximum number of entries to allow in this cache. For all other caches,
         * this is the maximum sum of the sizes of the entries to allow in this cache.
         */
        public LruCache(int maxSize) {
            super(maxSize);
        }

        /**
         * For caches that do not override {@link #sizeOf}, this returns
         * the number of entries in the cache. For all other caches, this
         * returns the sum of the sizes of the entries in this cache.
         * @return The size.
         */
        @Override
        public synchronized int size() {
            return size;
        }

        @Override
        public synchronized void clear() {
            size = 0;
            map.clear();
        }

        @Override
        public synchronized V get(K key) {
            DebugUtils.__checkError(key == null, "key == null");
            return map.get(key);
        }

        @Override
        public synchronized Map<K, V> entries() {
            return new LinkedHashMap<K, V>(map);
        }

        @Override
        public String toString() {
            return toString(96).append(" }").toString();
        }

        /**
         * Returns the size of the entry for <tt>key</tt> and <tt>value</tt> in
         * user-defined units. The default implementation returns 1 so that size
         * is the number of entries and max size is the maximum number of entries.
         * @param key The key.
         * @param value The value.
         * @return The size of the entry, must be <tt>>= 0</tt>.
         */
        protected int sizeOf(K key, V value) {
            return 1;
        }

        @Override
        /* package */ synchronized V removeImpl(K key) {
            final V previous = map.remove(key);
            if (previous != null) {
                size -= sizeOf(key, previous);
            }

            return previous;
        }

        @Override
        /* package */ synchronized V putImpl(K key, V value) {
            size += sizeOf(key, value);
            final V previous = map.put(key, value);
            if (previous != null) {
                size -= sizeOf(key, previous);
            }

            return previous;
        }

        @Override
        /* package */ void trimToSize(int maxSize, boolean evicted) {
            K key;
            V value;
            while (true) {
                synchronized (this) {
                    if (size <= maxSize || map.isEmpty()) {
                        break;
                    }

                    final Iterator<Entry<K, V>> itor = map.entrySet().iterator();
                    final Entry<K, V> toEvict = itor.next();
                    key = toEvict.getKey();
                    value = toEvict.getValue();
                    itor.remove();
                    size -= sizeOf(key, value);
                }

                entryRemoved(evicted, key, value, null);
            }
        }

        /* package */ synchronized final StringBuilder toString(int capacity) {
            return new StringBuilder(capacity)
                .append(getClass().getSimpleName())
                .append(" { count = ").append(map.size())
                .append(", size = ").append(size)
                .append(", maxSize = ").append(maxSize);
        }
    }

    /**
     * Class <tt>LruBitmapCache</tt> is an implementation of a {@link LruCache}.
     */
    public static class LruBitmapCache<K> extends LruCache<K, Bitmap> {
        /**
         * Constructor
         * @param maxSize The maximum the number of bytes to allow in this cache.
         * @see #LruBitmapCache(float)
         */
        public LruBitmapCache(int maxSize) {
            super(maxSize);
        }

        /**
         * Constructor
         * @param scaleMemory The scale of memory, expressed as a percentage
         * of this application maximum memory of the current device.
         * @see #LruBitmapCache(int)
         */
        public LruBitmapCache(float scaleMemory) {
            super((int)(Runtime.getRuntime().maxMemory() * scaleMemory + 0.5f));
        }

        @Override
        protected int sizeOf(K key, Bitmap value) {
            return value.getAllocationByteCount();
        }

        /* package */ final void dump(Context context, Printer printer) {
            final Set<Entry<K, Bitmap>> entries = entries().entrySet();
            final StringBuilder result = new StringBuilder(384);

            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ count = %d, size = %s, maxSize = %s, appMaxSize = %s ] ", getClass().getSimpleName(), entries.size(), Formatter.formatFileSize(context, size()), Formatter.formatFileSize(context, maxSize()), Formatter.formatFileSize(context, Runtime.getRuntime().maxMemory()));
            for (Entry<K, Bitmap> entry : entries) {
                result.setLength(0);
                final Bitmap value = entry.getValue();
                printer.println(BitmapUtils.dumpBitmap(context, result.append("  ").append(entry.getKey()).append(" ==> ").append(value), value).toString());
            }
        }
    }

    /**
     * Like as {@link LruBitmapCache}, but this class has a
     * {@link BitmapPool} to recycle the evicted bitmap to reused.
     */
    public static class LruBitmapCache2<K> extends LruBitmapCache<K> {
        protected final BitmapPool mBitmapPool;

        /**
         * Constructor
         * @param maxSize The maximum the number of bytes to allow in this cache.
         * @param bitmapPool The {@link BitmapPool} to recycle the evicted bitmap
         * from this cache.
         * @see #LruBitmapCache2(float, BitmapPool)
         */
        public LruBitmapCache2(int maxSize, BitmapPool bitmapPool) {
            super(maxSize);
            mBitmapPool = bitmapPool;
        }

        /**
         * Constructor
         * @param scaleMemory The scale of memory, expressed as a percentage
         * of this application maximum memory of the current device.
         * @param bitmapPool The {@link BitmapPool} to recycle the evicted
         * bitmap from this cache.
         * @see #LruBitmapCache2(int, BitmapPool)
         */
        public LruBitmapCache2(float scaleMemory, BitmapPool bitmapPool) {
            super(scaleMemory);
            mBitmapPool = bitmapPool;
        }

        /**
         * Returns the {@link BitmapPool} associated with this cache.
         * @return The <tt>BitmapPool</tt>.
         */
        public final BitmapPool getBitmapPool() {
            return mBitmapPool;
        }

        @Override
        public void clear() {
            super.clear();
            mBitmapPool.clear();
        }

        @Override
        protected void entryRemoved(boolean evicted, K key, Bitmap oldValue, Bitmap newValue) {
            if (!evicted && oldValue != newValue) {
                mBitmapPool.put(oldValue);
            }
        }

        /* package */ final void dump(Context context, Printer printer, Object reserved) {
            dump(context, printer);
            if (mBitmapPool instanceof LinkedBitmapPool) {
                ((LinkedBitmapPool)mBitmapPool).dump(context, printer);
            }
        }
    }

    /**
     * Class <tt>LruImageCache</tt> is an implementation of a {@link Cache}.
     */
    public static final class LruImageCache<K, Image> implements Cache<K, Object> {
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
            dumpCache(mBitmapCache, context, printer);
            dumpCache(mImageCache, context, printer);
        }
    }

    /**
     * Class <tt>SimpleFileCache</tt> is an implementation of a {@link FileCache}.
     * This class is an <b>unlimited-size</b> cache.
     */
    public static final class SimpleFileCache implements FileCache {
        private final String mCacheDir;

        /**
         * Constructor
         * @param cacheDir The absolute path of the cache directory.
         */
        public SimpleFileCache(String cacheDir) {
            mCacheDir = cacheDir;
        }

        @Override
        public void clear() {
        }

        @Override
        public String getCacheDir() {
            return mCacheDir;
        }

        @Override
        public String put(String key, String cacheFile) {
            return null;
        }

        @Override
        public String get(String key) {
            DebugUtils.__checkError(key == null, "key == null");
            return FileUtils.buildPath(mCacheDir, key);
        }

        @Override
        public String remove(String key) {
            DebugUtils.__checkError(key == null, "key == null");
            final String result = FileUtils.buildPath(mCacheDir, key);
            return (FileUtils.deleteFiles(result, false) == 0 ? result : null);
        }

        /* package */ final void dump(Context context, Printer printer) {
            final StringBuilder result = new StringBuilder(136);
            final List<Dirent> dirents = FileUtils.listFiles(mCacheDir, 0);
            final int size = ArrayUtils.getSize(dirents);

            DebugUtils.dumpSummary(printer, result, 130, " Dumping SimpleFileCache Storage Cache [ files = %d ] ", size);
            for (int i = 0; i < size; ++i) {
                final Dirent dirent = dirents.get(i);
                result.setLength(0);
                printer.println(result.append("  ").append(dirent.path).append(" [ length = ").append(Formatter.formatFileSize(context, dirent.length())).append(" ]").toString());
            }
        }
    }

    /**
     * Class <tt>LruFileCache</tt> is an implementation of a {@link FileCache}.
     */
    public static class LruFileCache extends LruCache<String, String> implements FileCache {
        protected final String mCacheDir;

        /**
         * Constructor
         * @param cacheDir The absolute path of the cache directory.
         * @param maxSize The maximum number of files to allow in this cache.
         */
        public LruFileCache(String cacheDir, int maxSize) {
            super(maxSize);
            mCacheDir = cacheDir;
        }

        @Override
        public String getCacheDir() {
            return mCacheDir;
        }

        @Override
        public String get(String key) {
            String result = super.get(key);
            if (result == null) {
                result = buildCacheFile(key);
                if (FileUtils.access(result, FileUtils.F_OK) == 0) {
                    put(key, result);
                }
            }

            return result;
        }

        @Override
        public String toString() {
            return toString(160).append(", cacheDir = ").append(mCacheDir).append(" }").toString();
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, String oldValue, String newValue) {
            if (evicted || !oldValue.equals(newValue)) {
                FileUtils.deleteFiles(oldValue, false);
            }
        }

        /**
         * Builds the cache file with the specified <em>key</em>.
         * @param key The key.
         * @return The absolute path of the cache file. Never <tt>null</tt>.
         */
        protected String buildCacheFile(String key) {
            return new StringBuilder(mCacheDir.length() + key.length() + 3).append(mCacheDir).append('/').append(key.charAt(0)).append('/').append(key).toString();
        }

        /* package */ final void dump(Context context, Printer printer) {
            final String className = getClass().getSimpleName();
            final StringBuilder result = new StringBuilder(256);
            final Collection<String> files = entries().values();

            if (this instanceof LruFileCache2) {
                DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ count = %d, size = %s, maxSize = %s ] ", className, files.size(), Formatter.formatFileSize(context, size()), Formatter.formatFileSize(context, maxSize()));
            } else {
                DebugUtils.dumpSummary(printer, result, 130, " Dumping %s memory cache [ size = %d, maxSize = %d ] ", className, files.size(), maxSize());
            }

            for (String file : files) {
                result.setLength(0);
                printer.println(result.append("  ").append(file).append(" [ size = ").append(Formatter.formatFileSize(context, FileUtils.getFileLength(file))).append(" ]").toString());
            }

            dumpCachedFiles(context, printer, result, className);
        }

        private void dumpCachedFiles(Context context, Printer printer, StringBuilder result, String className) {
            final List<Dirent> dirents = FileUtils.listFiles(mCacheDir, 0);
            final int size = ArrayUtils.getSize(dirents);
            result.setLength(0);
            if (size > 0) {
                Collections.sort(dirents);
            }

            long fileCount = 0, fileBytes = 0;
            for (int i = 0, index = 0; i < size; ++i) {
                final Dirent dirent = dirents.get(i);
                if (dirent.isDirectory()) {
                    ++index;
                    final Pair<Integer, Long> pair = getFileCount(dirent);
                    result.append("  ").append(dirent.getName()).append(" [ files = ").append(pair.first).append(", size = ").append(Formatter.formatFileSize(context, pair.second)).append(" ]");

                    fileCount += pair.first;
                    fileBytes += pair.second;
                }

                if ((index % 4) == 0) {
                    result.append('\n');
                }
            }

            DebugUtils.dumpSummary(printer, new StringBuilder(130), 130, " Dumping %s disk cache [ dirs = %d, files = %d, size = %s ] ", className, size, fileCount, Formatter.formatFileSize(context, fileBytes));
            if (result.length() > 0) {
                printer.println(result.toString());
            }
        }

        private static Pair<Integer, Long> getFileCount(Dirent dirent) {
            final List<Dirent> dirents = dirent.listFiles();
            final int size = ArrayUtils.getSize(dirents);

            long fileBytes = 0;
            for (int i = 0; i < size; ++i) {
                fileBytes += dirents.get(i).length();
            }

            return new Pair<Integer, Long>(size, fileBytes);
        }
    }

    /**
     * Like as {@link LruFileCache}, but the {@link #maxSize()}
     * is the maximum sum of the bytes of the files in this cache.
     */
    public static class LruFileCache2 extends LruFileCache {
        /**
         * Constructor
         * @param cacheDir The absolute path of the cache directory.
         * @param maxSize The maximum sum of the bytes of the files
         * to allow in this cache.
         */
        public LruFileCache2(String cacheDir, int maxSize) {
            super(cacheDir, maxSize);
        }

        @Override
        protected int sizeOf(String key, String value) {
            return (int)FileUtils.getFileLength(value);
        }
    }

    /**
     * Class <tt>LinkedBitmapPool</tt> is an implementation of a {@link BitmapPool}.
     */
    public static class LinkedBitmapPool implements BitmapPool, Comparator<Bitmap> {
        protected final int mMaxSize;
        protected final LinkedList<Bitmap> mBitmaps;

        /**
         * Constructor
         * @param maxSize The maximum number of bitmaps to allow in this pool.
         */
        public LinkedBitmapPool(int maxSize) {
            DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
            mMaxSize = maxSize;
            mBitmaps = new LinkedList<Bitmap>();
        }

        @Override
        public synchronized void clear() {
            mBitmaps.clear();
        }

        @Override
        public synchronized Bitmap get(int size) {
            final Iterator<Bitmap> itor = mBitmaps.iterator();
            while (itor.hasNext()) {
                final Bitmap bitmap = itor.next();
                if (bitmap.getAllocationByteCount() >= size) {
                    itor.remove();
                    return bitmap;
                }
            }

            return null;
        }

        @Override
        public synchronized void put(Bitmap bitmap) {
            if (bitmap != null && !bitmap.isRecycled()) {
                // Inserts the bitmap into the mBitmaps at the appropriate position.
                ArrayUtils.insert(mBitmaps, bitmap, this);

                // Removes the smallest bitmaps until the mBitmaps.size() is less the mMaxSize.
                while (mBitmaps.size() > mMaxSize) {
                    mBitmaps.removeFirst();
                }
            }
        }

        @Override
        public int compare(Bitmap one, Bitmap another) {
            return (one.getAllocationByteCount() - another.getAllocationByteCount());
        }

        @Override
        public synchronized String toString() {
            return new StringBuilder(64)
                .append(getClass().getSimpleName())
                .append(" { size = ").append(mBitmaps.size())
                .append(", maxSize = ").append(mMaxSize)
                .append(" }").toString();
        }

        /* package */ final void dump(Context context, Printer printer) {
            final List<Bitmap> bitmaps = new ArrayList<Bitmap>(mBitmaps);
            final StringBuilder result = new StringBuilder(288);

            DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d, maxSize = %d ] ", getClass().getSimpleName(), bitmaps.size(), mMaxSize);
            for (int i = 0, size = bitmaps.size(); i < size; ++i) {
                result.setLength(0);
                final Bitmap bitmap = bitmaps.get(i);
                printer.println(BitmapUtils.dumpBitmap(context, result.append("  ").append(bitmap), bitmap).toString());
            }
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
     * @param cacheName A relative path within the cache directory, such as <tt>"file_cache"</tt>.
     * @param maxFileSize The maximum number of files in the file cache. Pass <tt>0</tt> will be
     * returned the {@link SimpleFileCache} instance.
     * @return A new <tt>FileCache</tt> instance.
     */
    public static FileCache createFileCache(Context context, String cacheName, int maxFileSize) {
        final String cacheDir = FileUtils.getCacheDir(context, cacheName).getPath();
        return (maxFileSize > 0 ? new LruFileCache(cacheDir, maxFileSize) : new SimpleFileCache(cacheDir));
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
        if (cache instanceof LruBitmapCache2) {
            ((LruBitmapCache2<?>)cache).dump(context, printer, null);
        } else if (cache instanceof LruBitmapCache) {
            ((LruBitmapCache<?>)cache).dump(context, printer);
        } else if (cache instanceof LruImageCache) {
            ((LruImageCache<?, ?>)cache).dump(context, printer);
        } else if (cache instanceof LruFileCache) {
            ((LruFileCache)cache).dump(context, printer);
        } else if (cache instanceof SimpleFileCache) {
            ((SimpleFileCache)cache).dump(context, printer);
        } else if (cache instanceof SimpleLruCache) {
            ((SimpleLruCache<?, ?>)cache).dump(printer, ((SimpleLruCache<?, ?>)cache).entries());
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Caches() {
    }
}
