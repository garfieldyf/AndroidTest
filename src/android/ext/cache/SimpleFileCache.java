package android.ext.cache;

import java.io.File;
import java.util.Arrays;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.text.format.Formatter;
import android.util.Pair;
import android.util.Printer;

/**
 * Class <tt>SimpleFileCache</tt> is an implementation of a {@link FileCache}.
 * This class is an <b>unlimited-size</b> cache.
 * @author Garfield
 */
public final class SimpleFileCache implements FileCache {
    private final File mCacheDir;

    /**
     * Constructor
     * @param cacheDir The absolute path of the cache directory.
     * @see #SimpleFileCache(Context, String)
     */
    public SimpleFileCache(File cacheDir) {
        mCacheDir = cacheDir;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param name A relative path within the cache directory,
     * such as <tt>"simple_file_cache"</tt>.
     * @see #SimpleFileCache(File)
     */
    public SimpleFileCache(Context context, String name) {
        mCacheDir = FileUtils.getCacheDir(context, name);
    }

    @Override
    public void clear() {
    }

    @Override
    public File getCacheDir() {
        return mCacheDir;
    }

    @Override
    public File put(String key, File cacheFile) {
        return null;
    }

    @Override
    public File get(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        return buildCacheFile(key);
    }

    @Override
    public File remove(String key) {
        DebugUtils.__checkError(key == null, "key == null");
        final File result = buildCacheFile(key);
        return (result.delete() ? result : null);
    }

    private File buildCacheFile(String key) {
        final String cacheDir = mCacheDir.getPath();
        return new File(new StringBuilder(cacheDir.length() + key.length() + 3).append(cacheDir).append('/').append(key.charAt(0)).append('/').append(key).toString());
    }

    /* package */ final void dump(Context context, Printer printer) {
        dumpCachedFiles(context, printer, mCacheDir, new StringBuilder(130), getClass().getSimpleName());
    }

    /* package */ static void dumpCachedFiles(Context context, Printer printer, File cacheDir, StringBuilder result, String className) {
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
}
