package android.ext.cache;

import java.io.File;

/**
 * A <tt>FileCache</tt> is a data structure consisting of a set of
 * keys and files in which each key is mapped to a single {@link File}.
 * @author Garfield
 */
public interface FileCache {
    /**
     * Returns the absolute path of the cache directory.
     * @return The absolute path of the cache directory.
     */
    File getCacheDir();

    /**
     * Removes all files from this cache, leaving it empty.
     * @see #remove(String)
     */
    void clear();

    /**
     * Removes the cache file for the specified <em>key</em> in this cache.
     * The cache file will be delete from filesystem.
     * @param key The key to remove.
     * @return The cache <tt>File</tt> mapped by <em>key</em> or <tt>null</tt>
     * if there was no mapping.
     * @see #clear()
     */
    File remove(String key);

    /**
     * Returns the cache file of the mapping with the specified <em>key</em>
     * in this cache. If no mapping for the specified <em>key</em> is found,
     * returns a <tt>File</tt> contains the absolute path of the cache file.
     * @param key The key to find.
     * @return The cache <tt>File</tt>. Never <tt>null</tt>.
     * @see #put(String, File)
     */
    File get(String key);

    /**
     * Maps the specified <em>key</em> to the specified <em>cacheFile</em>.
     * The previous cache file will be delete from filesystem.
     * @param key The key.
     * @param cacheFile The absolute path of the cache <tt>File</tt>.
     * @return The previous cache <tt>File</tt> mapped by <em>key</em> or
     * <tt>null</tt> if there was no mapping.
     * @see #get(String)
     */
    File put(String key, File cacheFile);
}
