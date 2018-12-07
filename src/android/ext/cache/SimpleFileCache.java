package android.ext.cache;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.Dirent;
import android.text.format.Formatter;
import android.util.Pair;
import android.util.Printer;

/**
 * Class <tt>SimpleFileCache</tt> is an implementation of a {@link FileCache}.
 * This class is an <b>unlimited-size</b> cache.
 * @author Garfield
 */
public final class SimpleFileCache implements FileCache {
    private final String mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @see #SimpleFileCache(Context, String)
     */
    public SimpleFileCache(String cacheDir) {
        mCacheDir = cacheDir;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param name A relative path within the cache directory,
     * such as <tt>"simple_file_cache"</tt>.
     * @see #SimpleFileCache(String)
     */
    public SimpleFileCache(Context context, String name) {
        mCacheDir = FileUtils.getCacheDir(context, name).getPath();
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
        return buildCacheFile(key);
    }

    @Override
    public String remove(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        final String result = buildCacheFile(key);
        return (FileUtils.deleteFiles(result, false) == 0 ? result : null);
    }

    /**
     * @return Always returns an empty (<tt>0-size</tt>) {@link Map}.
     */
    @Override
    public Map<String, String> snapshot() {
        return Collections.emptyMap();
    }

    private String buildCacheFile(String key) {
        return new StringBuilder(mCacheDir.length() + key.length() + 3).append(mCacheDir).append('/').append(key.charAt(0)).append('/').append(key).toString();
    }

    /* package */ final void dump(Context context, Printer printer) {
        dumpCachedFiles(context, printer, mCacheDir, new StringBuilder(130), getClass().getSimpleName());
    }

    /* package */ static void dumpCachedFiles(Context context, Printer printer, String cacheDir, StringBuilder result, String className) {
        final List<Dirent> dirents = FileUtils.listFiles(cacheDir, 0);
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
