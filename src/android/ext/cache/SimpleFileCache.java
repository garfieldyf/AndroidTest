package android.ext.cache;

import java.util.List;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.Dirent;
import android.text.format.Formatter;
import android.util.Printer;

/**
 * Class <tt>SimpleFileCache</tt> is an implementation of a {@link FileCache}.
 * This class is an <b>unlimited-size</b> cache.
 * @author Garfield
 * @version 1.0
 */
public final class SimpleFileCache implements FileCache {
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
