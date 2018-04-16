package android.ext.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import android.content.Context;
import android.content.res.AssetManager;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.SimplePool;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Printer;

/**
 * Class FileUtils
 * @author Garfield
 * @version 4.0
 */
public final class FileUtils {
    /**
     * The file exists, this flag use with {@link #access(String, int)}.
     */
    public static final int F_OK = 0x00;

    /**
     * The file executable, this flag use with {@link #access(String, int)}.
     */
    public static final int X_OK = 0x01;

    /**
     * The file writable, this flag use with {@link #access(String, int)}.
     */
    public static final int W_OK = 0x02;

    /**
     * The file readable, this flag use with {@link #access(String, int)}.
     */
    public static final int R_OK = 0x04;

    /**
     * The file readable, writable and executable, this flag use with {@link #access(String, int)}.
     */
    public static final int A_OK = R_OK | W_OK | X_OK;

    /**
     * This flag use with {@link #mkdirs(String, int)}. If
     * set the last item in path is a file name, so ignore.
     * <P>Example: if path is "/mnt/sdcard/mydir/myfile",
     * only use "/mnt/sdcard/mydir"</P>.
     */
    public static final int FLAG_IGNORE_FILENAME = 0x01;

    /**
     * Ignores the hidden file (start with ".").
     */
    public static final int FLAG_IGNORE_HIDDEN_FILE = 0x01;

    /**
     * This flag use with {@link #scanFiles(String, ScanCallback, int)}.
     * If set the <tt>scanFiles</tt> will scan the descendent files.
     */
    public static final int FLAG_SCAN_FOR_DESCENDENTS = 0x02;

    /**
     * Returns the absolute path to the directory in which the application can place
     * its own files on the filesystem. <p>If the external storage mounted the result
     * path such as <tt>"/storage/emulated/0/Android/data/packagename/files/name"</tt>,
     * otherwise the result path such as <tt>"/data/data/packagename/files/name"</tt>.</p>
     * <p>This is like {@link Context#getFilesDir()} in that these files will be deleted
     * when the application is uninstalled.</p>
     * @param context The <tt>Context</tt>.
     * @param name The name of the directory to retrieve. May be <tt>null</tt> for the root
     * of the <em>files</em> directory.
     * @return The path of the directory holding application files.
     * @see #getCacheDir(Context, String)
     * @see #getExternalCacheDir(Context, String)
     */
    public static File getFilesDir(Context context, String name) {
        File filesDir = context.getExternalFilesDir(name);
        if (filesDir == null) {
            filesDir = context.getFilesDir();
            if (StringUtils.getLength(name) > 0) {
                filesDir = new File(filesDir, name);
                mkdirs(filesDir.getPath(), 0);
            }
        }

        return filesDir;
    }

    /**
     * Returns the absolute path to the directory in which the application can place its
     * own cache files on the filesystem. <p>If the external storage mounted the result
     * path such as <tt>"/storage/emulated/0/Android/data/packagename/cache/name"</tt>,
     * otherwise the result path such as <tt>"/data/data/packagename/cache/name"</tt>.</p>
     * <p>This is like {@link Context#getFilesDir()} in that these files will be deleted
     * when the application is uninstalled.</p>
     * @param context The <tt>Context</tt>.
     * @param name The name of the directory to retrieve. May be <tt>null</tt> for the root
     * of the <em>cache</em> directory.
     * @return The path of the directory holding application cache files.
     * @see #getFilesDir(Context, String)
     * @see #getExternalCacheDir(Context, String)
     */
    public static File getCacheDir(Context context, String name) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }

        if (StringUtils.getLength(name) > 0) {
            cacheDir = new File(cacheDir, name);
            mkdirs(cacheDir.getPath(), 0);
        }

        return cacheDir;
    }

    /**
     * Returns the absolute path to the directory in which the application can place
     * its own cache files on the primary external storage. <p>The result path such
     * as <tt>"/storage/emulated/0/Android/data/packagename/cache/name"</tt></p>
     * <p>This is like {@link Context#getFilesDir()} in that these files will be deleted
     * when the application is uninstalled.</p>
     * @param context The <tt>Context</tt>.
     * @param name The name of the directory to retrieve. May be <tt>null</tt> for the
     * root of the <em>cache</em> directory.
     * @return The path of the directory holding application cache files on the external
     * storage, or <tt>null</tt> if the external storage is not currently mounted.
     * @see #getFilesDir(Context, String)
     * @see #getCacheDir(Context, String)
     */
    public static File getExternalCacheDir(Context context, String name) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null && StringUtils.getLength(name) > 0) {
            cacheDir = new File(cacheDir, name);
            mkdirs(cacheDir.getPath(), 0);
        }

        return cacheDir;
    }

    /**
     * Closes the object and releases any system resources associated with it. If the
     * object is <tt>null</tt> or already closed then invoking this method has no effect.
     * @param c An AutoCloseable is a source or destination of data that can be closed.
     */
    public static void close(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                Log.e(FileUtils.class.getName(), "Couldn't close - " + c.getClass().getName(), e);
            }
        }
    }

    /**
     * Tests if the filename is valid.
     * @param filename The filename to test.
     * @return <tt>true</tt> if the filename is valid, <tt>false</tt> otherwise.
     */
    public static boolean isFilenameValid(CharSequence filename) {
        return (!(StringUtils.getLength(filename) == 0 || ".".contentEquals(filename) || "..".contentEquals(filename)) && RegexPattern.sInstance.matcher(filename).matches());
    }

    /**
     * Tests if the <em>path</em> is linux root directory ('/').
     * @param path The path to test.
     * @return <tt>true</tt> if the path is root directory, <tt>false</tt> otherwise.
     * @see #isAbsolutePath(CharSequence)
     */
    public static boolean isRootDir(CharSequence path) {
        return (StringUtils.getLength(path) == 1 && path.charAt(0) == '/');
    }

    /**
     * Tests if the <em>path</em> is an absolute path.
     * @param path The path to test.
     * @return <tt>true</tt> if the path is an absolute path, <tt>false</tt> otherwise.
     * @see #isRootDir(CharSequence)
     */
    public static boolean isAbsolutePath(CharSequence path) {
        return (StringUtils.getLength(path) > 0 && path.charAt(0) == '/');
    }

    /**
     * Tests if the <em>path</em> is a hidden file.
     * @param path The path to test.
     * @return <tt>true</tt> if the <em>path</em>
     * is a hidden file, <tt>false</tt> otherwise.
     */
    public static boolean isHidden(String path) {
        final int length = StringUtils.getLength(path);
        return (length > 0 && path.charAt(length - 1) != '/' && path.charAt(path.lastIndexOf('/') + 1) == '.');
    }

    /**
     * Builds an absolute file path, adding a file separator if necessary.
     * @param dir The path to the directory.
     * @param name The file's name.
     */
    public static String buildPath(CharSequence dir, CharSequence name) {
        final int length = dir.length() + name.length() + 1;
        final StringBuilder path = new StringBuilder(length).append(dir).append('/').append(name);

        boolean haveSlash = false;
        int newLength = 0;
        for (int i = 0; i < length; ++i) {
            final char ch = path.charAt(i);
            if (ch == '/') {
                if (!haveSlash) {
                    haveSlash = true;
                    path.setCharAt(newLength++, '/');
                }
            } else {
                haveSlash = false;
                path.setCharAt(newLength++, ch);
            }
        }

        return path.substring(0, (haveSlash && newLength > 1 ? newLength - 1 : newLength));
    }

    /**
     * Creates the directory with the specified <em>path</em>.
     * @param path The path to create, must be absolute file path.
     * @param flags Creating flags. Pass 0 or {@link #FLAG_IGNORE_FILENAME}.
     * @return Returns <tt>0</tt> if the necessary directory has been
     * created or the target directory already exists, Otherwise returns
     * an error code. See {@link ErrnoException}.
     */
    public static native int mkdirs(String path, int flags);

    /**
     * Checks the file access mode with the specified <em>path</em>.
     * This operation is supported for both file and directory.
     * @param path The file or directory path, must be absolute file path.
     * @param mode The access mode. May be any combination of {@link #F_OK},
     * {@link #R_OK}, {@link #W_OK}, {@link #X_OK} and {@link #A_OK}.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise
     * returns an error code. See {@link ErrnoException}.
     */
    public static native int access(String path, int mode);

    /**
     * Returns the file status (include mode, uid, gid, size, etc.) with the specified
     * <em>path</em>. This operation is supported for both file and directory.
     * @param path The file or directory path, must be absolute file path.
     * @return A {@link Stat} object if the operation succeeded, <tt>null</tt> otherwise.
     * @see #stat(String, Stat)
     */
    public static Stat stat(String path) {
        final Stat stat = new Stat();
        return (stat(path, stat) == 0 ? stat : null);
    }

    /**
     * Returns the file status (include mode, uid, gid, size, etc.) with the specified
     * <em>path</em>. This operation is supported for both file and directory.
     * @param path The file or directory path, must be absolute file path.
     * @param outStat The {@link Stat} to store the result.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an error
     * code. See {@link ErrnoException}.
     * @see #stat(String)
     */
    public static native int stat(String path, Stat outStat);

    /**
     * Scans all the sub files and directories in the specified <em>dirPath</em>.
     * <p>The entries <tt>.</tt> and <tt>..</tt> representing the current and
     * parent directory are not scanned.</p>
     * @param dirPath The directory path, must be absolute file path.
     * @param callback The {@link ScanCallback} used to scan.
     * @param flags The scan flags. May be <tt>0</tt> or any combination of
     * {@link #FLAG_IGNORE_HIDDEN_FILE}, {@link #FLAG_SCAN_FOR_DESCENDENTS}.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns
     * an error code. See {@link ErrnoException}.
     */
    public static native int scanFiles(String dirPath, ScanCallback callback, int flags);

    /**
     * Equivalent to calling <tt>listFiles(dirPath, flags, Dirent.FACTORY, new ArrayList())</tt>.
     * @param dirPath The directory path, must be absolute file path.
     * @param flags The list flags. Pass 0 or {@link #FLAG_IGNORE_HIDDEN_FILE}.
     * @param factory The {@link Factory} to create the <tt>Dirent</tt> or subclass objects.
     * @return A <tt>List</tt> of <tt>Dirent</tt> or subclass objects if the operation succeeded,
     * <tt>null</tt> otherwise.
     * @see Dirent#FACTORY
     * @see #listFiles(String, int, Factory)
     * @see #listFiles(String, int, Factory, List)
     */
    public static List<Dirent> listFiles(String dirPath, int flags) {
        final List<Dirent> dirents = new ArrayList<Dirent>();
        return (listFiles(dirPath, flags, Dirent.FACTORY, dirents) == 0 ? dirents : null);
    }

    /**
     * Equivalent to calling <tt>listFiles(dirPath, flags, factory, new ArrayList())</tt>.
     * @param dirPath The directory path, must be absolute file path.
     * @param flags The list flags. Pass 0 or {@link #FLAG_IGNORE_HIDDEN_FILE}.
     * @param factory The {@link Factory} to create the <tt>Dirent</tt> or subclass objects.
     * @return A <tt>List</tt> of <tt>Dirent</tt> or subclass objects if the operation succeeded,
     * <tt>null</tt> otherwise.
     * @see Dirent#FACTORY
     * @see #listFiles(String, int)
     * @see #listFiles(String, int, Factory, List)
     */
    public static <T extends Dirent> List<T> listFiles(String dirPath, int flags, Factory<T> factory) {
        final List<T> dirents = new ArrayList<T>();
        return (listFiles(dirPath, flags, factory, dirents) == 0 ? dirents : null);
    }

    /**
     * Returns a <tt>List</tt> of {@link Dirent} or subclass objects with the sub files and
     * directories in the <em>dirPath</em>.<p>The entries <tt>.</tt> and <tt>..</tt> representing
     * the current and parent directory are not returned as part of the list.</p>
     * @param dirPath The directory path, must be absolute file path.
     * @param flags The list flags. Pass 0 or {@link #FLAG_IGNORE_HIDDEN_FILE}.
     * @param factory The {@link Factory} to create the <tt>Dirent</tt> or subclass objects.
     * @param outDirents A <tt>List</tt> to store the <tt>Dirent</tt> or subclass objects.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an error code.
     * See {@link ErrnoException}.
     * @see Dirent#FACTORY
     * @see #listFiles(String, int)
     * @see #listFiles(String, int, Factory)
     */
    public static native <T extends Dirent> int listFiles(String dirPath, int flags, Factory<T> factory, List<? super T> outDirents);

    /**
     * Returns the start index of the filename in the specified <em>path</em>.
     * @param path The file pathname.
     * @return The start index of the filename, or <tt>-1</tt> if there is
     * no name part in the <em>path</em>.
     * @see #findFileExtension(String)
     */
    public static int findFileName(String path) {
        final int length = StringUtils.getLength(path);
        return (length == 1 && path.charAt(0) == '/' ? 0 : (length > 0 && path.charAt(length - 1) != '/' ? path.lastIndexOf('/') + 1 : -1));
    }

    /**
     * Returns the start index of the extension in the specified <em>path</em>.
     * @param path The file pathname.
     * @return The start index of the extension (excluding dot <tt>.</tt>),
     * or <tt>-1</tt> if the extension was not found.
     * @see #findFileName(String)
     */
    public static int findFileExtension(String path) {
        final int end = StringUtils.getLength(path) - 1;
        for (int i = end; i >= 0; --i) {
            final char c = path.charAt(i);
            if (c == '/') {
                break;
            } else if (c == '.') {
                return (i != end ? i + 1 : -1);
            }
        }

        return -1;
    }

    /**
     * Returns the name of the specified file or directory.
     * @param path The file or directory pathname.
     * @return The file name or <tt>null</tt> if there is
     * no name part in the file path.
     * @see #getFileParent(String)
     * @see #getFileExtension(String)
     * @see #getFileMimeType(String)
     */
    public static String getFileName(String path) {
        final int index = findFileName(path);
        return (index != -1 ? path.substring(index) : null);
    }

    /**
     * Returns the pathname of the parent of the specified file or directory.
     * @param path The file or directory pathname.
     * @return The parent pathname, or <tt>null</tt> if there is no parent
     * part in the <em>path</em>.
     * @see #getFileName(String)
     * @see #getFileExtension(String)
     * @see #getFileMimeType(String)
     */
    public static String getFileParent(String path) {
        if (path != null) {
            final int index = path.lastIndexOf('/');
            if (index != -1 && path.length() != 1) {
                return path.substring(0, index == 0 ? 1 : index);
            }
        }

        return null;
    }

    /**
     * Returns the file extension of specified file. The result string
     * excluding dot (<tt>.</tt>)
     * @param path The file pathname.
     * @return The extension, or <tt>null</tt> if the extension was not
     * found.
     * @see #getFileName(String)
     * @see #getFileParent(String)
     * @see #getFileMimeType(String)
     */
    public static String getFileExtension(String path) {
        final int index = findFileExtension(path);
        return (index != -1 ? path.substring(index) : null);
    }

    /**
     * Return the MIME type (such as "text/plain") of specified file.
     * @param path The file pathname.
     * @return The MIME type, or <tt>null</tt> if the MIME type was
     * not found.
     * @see #getFileName(String)
     * @see #getFileParent(String)
     * @see #getFileExtension(String)
     */
    public static String getFileMimeType(String path) {
        return (path != null ? HttpURLConnection.getFileNameMap().getContentTypeFor(path) : null);
    }

    /**
     * Returns the file type of the specified <em>path</em>.
     * @param path The file or directory path, must be absolute file path.
     * @return The file type, one of <tt>Stat.S_IFXXX</tt> constants if the
     * operation succeeded, <tt>0</tt> otherwise.
     * @see #getFileMode(String)
     * @see #getFileLength(String)
     */
    public static int getFileType(String path) {
        return (getFileMode(path) & Stat.S_IFMT);
    }

    /**
     * Returns the file protection with the specified <em>path</em>.
     * This corresponds to the linux structure <tt>stat.st_mode</tt>.
     * @param path The file or directory path, must be absolute file path.
     * @return The file protection if the operation succeeded, <tt>0</tt> otherwise.
     * @see #getFileType(String)
     * @see #getFileLength(String)
     */
    public static native int getFileMode(String path);

    /**
     * Returns the length of the file with the specified <em>filename</em> in bytes.
     * @param filename The filename, must be absolute file path.
     * @return The number of bytes if the operation succeeded, <tt>0</tt> otherwise.
     * @see #getFileMode(String)
     * @see #getFileType(String)
     */
    public static native long getFileLength(String filename);

    /**
     * Returns the number of files in the specified <em>dirPath</em>.
     * @param dirPath The directory path, must be absolute file path.
     * @param flags Pass 0 or {@link #FLAG_IGNORE_HIDDEN_FILE}.
     * @return The number of files if the operation succeeded, <tt>0</tt> otherwise.
     */
    public static native int getFileCount(String dirPath, int flags);

    /**
     * Equivalent to calling <tt>copyStream(src, dst, null, buffer)</tt>.
     * @param src The <tt>InputStream</tt> to read.
     * @param dst The <tt>OutputStream</tt> to write.
     * @param buffer May be <tt>null</tt>. The temporary byte array to store the read bytes.
     * @throws IOException if an error occurs while writing to <em>dst</em>.
     * @see #copyStream(InputStream, OutputStream, Cancelable, byte[])
     */
    public static void copyStream(InputStream src, OutputStream dst, byte[] buffer) throws IOException {
        copyStream(src, dst, null, buffer);
    }

    /**
     * Copies the specified <tt>InputStream</tt> the contents into <tt>OutputStream</tt>.
     * @param src The <tt>InputStream</tt> to read.
     * @param dst The <tt>OutputStream</tt> to write.
     * @param cancelable A {@link Cancelable} that can be cancelled, or <tt>null</tt> if
     * none. If the operation was cancelled before it completed normally then the <em>dst's</em>
     * contents undefined.
     * @param buffer May be <tt>null</tt>. The temporary byte array to store the read bytes.
     * @throws IOException if an error occurs while writing to <em>dst</em>.
     * @see #copyStream(InputStream, OutputStream, byte[])
     */
    public static void copyStream(InputStream src, OutputStream dst, Cancelable cancelable, byte[] buffer) throws IOException {
        if (dst instanceof ByteArrayBuffer) {
            ((ByteArrayBuffer)dst).readFrom(src, cancelable);
        } else if (buffer != null) {
            copyStreamImpl(src, dst, cancelable, buffer);
        } else {
            buffer = ByteArrayPool.sInstance.obtain();
            copyStreamImpl(src, dst, cancelable, buffer);
            ByteArrayPool.sInstance.recycle(buffer);
        }
    }

    /**
     * Copies the <em>src</em> file to <em>dst</em> file. If the <em>dst</em>
     * file already exists, it can be overrided to. <p>Note: This method will
     * be create the necessary directories.</p>
     * @param src The source file to read, must be absolute file path.
     * @param dst The destination file to write, must be absolute file path.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns
     * an error code. See {@link ErrnoException}.
     */
    public static native int copyFile(String src, String dst);

    /**
     * Moves the <em>src</em> file to <em>dst</em> file. If the specified file
     * <em>dst</em> already exists, it can be overrided to. <p>Note: This method
     * will be create the necessary directories.</p>
     * @param src The source file to move, must be absolute file path.
     * @param dst The destination file to move to, must be absolute file path.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns
     * an error code. See {@link ErrnoException}.
     */
    public static native int moveFile(String src, String dst);

    /**
     * Reads the specified file contents into a {@link ByteArrayBuffer}.
     * @param filename The file to read, must be absolute file path.
     * @return A <tt>ByteArrayBuffer</tt> if the operation succeeded,
     * <tt>null</tt> otherwise.
     * @see #readFile(String, OutputStream)
     */
    public static ByteArrayBuffer readFile(String filename) {
        InputStream is = null;
        try {
            is = new FileInputStream(filename);
            final ByteArrayBuffer result = new ByteArrayBuffer();
            result.readFrom(is, null);
            return result;
        } catch (Exception e) {
            Log.e(FileUtils.class.getName(), new StringBuilder("Couldn't read file - ").append(filename).toString(), e);
            return null;
        } finally {
            FileUtils.close(is);
        }
    }

    /**
     * Reads the specified file contents into the specified <em>out</em>.
     * @param filename The file to read, must be absolute file path.
     * @param out The <tt>OutputStream</tt> to write to.
     * @throws IOException if an error occurs while writing to <em>out</em>.
     * @see #readFile(String)
     */
    public static void readFile(String filename, OutputStream out) throws IOException {
        final InputStream is = new FileInputStream(filename);
        try {
            copyStream(is, out, null, null);
        } finally {
            is.close();
        }
    }

    /**
     * Reads the "assets" directory file contents into a {@link ByteArrayBuffer}.
     * @param assetManager The <tt>AssetManager</tt>.
     * @param filename A relative path within the assets, such as <tt>"docs/home.html"</tt>.
     * @return A <tt>ByteArrayBuffer</tt> if the operation succeeded, <tt>null</tt> otherwise.
     * @see #readAssetFile(AssetManager, String, OutputStream)
     */
    public static ByteArrayBuffer readAssetFile(AssetManager assetManager, String filename) {
        InputStream is = null;
        try {
            is = assetManager.open(filename, AssetManager.ACCESS_STREAMING);
            final ByteArrayBuffer result = new ByteArrayBuffer();
            result.readFrom(is, null);
            return result;
        } catch (Exception e) {
            Log.e(FileUtils.class.getName(), new StringBuilder("Couldn't read asset file - ").append(filename).toString(), e);
            return null;
        } finally {
            FileUtils.close(is);
        }
    }

    /**
     * Reads the "assets" directory file contents into the specified <em>out</em>.
     * @param assetManager The <tt>AssetManager</tt>.
     * @param filename A relative path within the assets, such as <tt>"docs/home.html"</tt>.
     * @param out The <tt>OutputStream</tt> to write to.
     * @throws IOException if an error occurs while writing to <em>out</em>.
     * @see #readAssetFile(AssetManager, String)
     */
    public static void readAssetFile(AssetManager assetManager, String filename, OutputStream out) throws IOException {
        final InputStream is = assetManager.open(filename, AssetManager.ACCESS_STREAMING);
        try {
            copyStream(is, out, null, null);
        } finally {
            is.close();
        }
    }

    /**
     * Deletes a file or directory with specified <em>path</em>. if <em>path</em>
     * is a directory, all sub files and directories will be deleted.
     * @param path The file or directory to delete, must be absolute file path.
     * @param deleteSelf Whether to delete the <em>path</em> itself. If the
     * <em>path</em> is a file, this parameter will be ignored.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an
     * error code. See {@link ErrnoException}.
     */
    public static native int deleteFiles(String path, boolean deleteSelf);

    /**
     * Creates a file with the specified <em>filename</em>. If the file was
     * created the file's length is the <em>length</em> and the content is
     * empty. If the specified file already exists, it can be overrided to.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param filename The filename to create, must be absolute file path.
     * @param length The desired file length in bytes.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns
     * an error code. See {@link ErrnoException}.
     * @see #createUniqueFile(String, long)
     */
    public static native int createFile(String filename, long length);

    /**
     * Creates a unique file with the specified <em>filename</em>. If the file
     * was created the file's length is the <em>length</em> and the content is
     * empty. <p>Note: This method will be create the necessary directories.</p>
     * @param filename The original filename to create, must be absolute file path.
     * @param length The desired file length in bytes.
     * @return Returns the unique filename (include file path), or <tt>null</tt>
     * if the file could't be created.
     * @see #createFile(String, long)
     */
    public static native String createUniqueFile(String filename, long length);

    /**
     * Copies the specified <tt>InputStream's</tt> contents into <tt>OutputStream</tt>.
     */
    private static void copyStreamImpl(InputStream src, OutputStream dst, Cancelable cancelable, byte[] buffer) throws IOException {
        cancelable = DummyCancelable.wrap(cancelable);
        for (int readBytes; (readBytes = src.read(buffer, 0, buffer.length)) > 0 && !cancelable.isCancelled(); ) {
            dst.write(buffer, 0, readBytes);
        }
    }

    /**
     * Called by native code.
     */
    @Keep
    private static <T extends Dirent> void addDirent(List<? super T> dirents, String path, int type, Factory<T> factory) {
        final T dirent = factory.newInstance();
        dirent.path = path;
        dirent.type = type;
        dirents.add(dirent);
    }

    /**
     * Called by native code.
     */
    @Keep
    private static void setStat(Stat stat, int mode, int uid, int gid, long size, long blocks, long blksize, long mtime) {
        stat.mode    = mode;
        stat.uid     = uid;
        stat.gid     = gid;
        stat.size    = size;
        stat.mtime   = mtime;
        stat.blocks  = blocks;
        stat.blksize = blksize;
    }

    /**
     * Callback interface used to {@link FileUtils#scanFiles(String, ScanCallback, int)}.
     */
    public static interface ScanCallback {
        /**
         * The returned value of {@link #onScanFile(String, int)}
         * indicates to continue to scan.
         */
        int SC_CONTINUE = 0;

        /**
         * The returned value of {@link #onScanFile(String, int)}
         * indicates to stop all scans.
         */
        int SC_STOP = 1;

        /**
         * The returned value of {@link #onScanFile(String, int)}
         * indicates to stop the current directory scan.
         */
        int SC_BREAK = 2;

        /**
         * The returned value of {@link #onScanFile(String, int)}
         * indicates to stop the current file's parent directory.
         */
        int SC_BREAK_PARENT = 3;

        /**
         * This callback is invoked on the <tt>scanFiles</tt> was called from.
         * @param path The absolute file path.
         * @param type The file type. May be one of <tt>Dirent.DT_XXX</tt> constants.
         * @return One of {@link #SC_CONTINUE}, {@link #SC_STOP}, {@link #SC_BREAK}
         * or {@link #SC_BREAK_PARENT}.
         * @see {@link FileUtils#scanFiles}
         */
        @Keep
        int onScanFile(String path, int type);
    }

    /**
     * Class <tt>Stat</tt> is wrapper for linux structure <tt>stat</tt>.
     */
    public static final class Stat implements Parcelable, Cloneable {
        /**
         * The permission of read, write and execute by all users.
         * Same as <em>S_IRWXU | S_IRWXG | S_IRWXO</em>.
         */
        public static final int S_IRWXA = 00777;

        /**
         * The default permissions.
         * Same as <em>S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH</em>.
         */
        public static final int S_IRWXD = 00775;

        /**
         * The permission of read, write by all users.
         * Same as <em>S_IRWXU | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH</em>.
         */
        public static final int S_IRWDA = 00766;

        /**
         * The permission of read, execute by all users.
         * Same as <em>S_IRWXU | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH</em>.
         */
        public static final int S_IRXDA = 00755;

        /**
         * The permission of read, write and execute by owner.
         */
        public static final int S_IRWXU = 00700;

        /**
         * The permission of read by owner.
         */
        public static final int S_IRUSR = 00400;

        /**
         * The permission of write by owner.
         */
        public static final int S_IWUSR = 00200;

        /**
         * The permission of execute by owner.
         */
        public static final int S_IXUSR = 00100;

        /**
         * The permission of read, write and execute by group.
         */
        public static final int S_IRWXG = 00070;

        /**
         * The permission of read by group.
         */
        public static final int S_IRGRP = 00040;

        /**
         * The permission of write by group.
         */
        public static final int S_IWGRP = 00020;

        /**
         * The permission of execute by group.
         */
        public static final int S_IXGRP = 00010;

        /**
         * The permission of read, write and execute by others.
         */
        public static final int S_IRWXO = 00007;

        /**
         * The permission of read by others.
         */
        public static final int S_IROTH = 00004;

        /**
         * The permission of write by others.
         */
        public static final int S_IWOTH = 00002;

        /**
         * The permission of execute by others.
         */
        public static final int S_IXOTH = 00001;

        /**
         * The permission of set user ID.
         */
        public static final int S_ISUID = 04000;

        /**
         * The permission of set group ID.
         */
        public static final int S_ISGID = 02000;

        /**
         * The permission of sticky bit.
         */
        public static final int S_ISVTX = 01000;

        /**
         * The file type mask.
         */
        public static final int S_IFMT = 0170000;

        /**
         * The file is a named pipe (FIFO).
         */
        public static final int S_IFIFO = 0010000;

        /**
         * The file is a character device.
         */
        public static final int S_IFCHR = 0020000;

        /**
         * The file is a directory.
         */
        public static final int S_IFDIR = 0040000;

        /**
         * The file is a block device.
         */
        public static final int S_IFBLK = 0060000;

        /**
         * The file is a regular file.
         */
        public static final int S_IFREG = 0100000;

        /**
         * The file is a symbolic link.
         */
        public static final int S_IFLNK = 0120000;

        /**
         * The file is a UNIX domain socket.
         */
        public static final int S_IFSOCK = 0140000;

        /**
         * The mode (permissions). This corresponds
         * to the linux <tt>stat.st_mode</tt> field.
         */
        public int mode;

        /**
         * The user ID of owner. This corresponds
         * to the linux <tt>stat.st_uid</tt> field.
         */
        public int uid;

        /**
         * The group ID of owner. This corresponds
         * to the linux <tt>stat.st_gid</tt> field.
         */
        public int gid;

        /**
         * The total size in bytes. This corresponds
         * to the linux <tt>stat.st_size</tt> field.
         */
        public long size;

        /**
         * The last modify time, in milliseconds. This corresponds
         * to the linux <tt>stat.st_mtime</tt> field.
         */
        public long mtime;

        /**
         * The number of 512B blocks allocated. This corresponds
         * to the linux <tt>stat.st_blocks</tt> field.
         */
        public long blocks;

        /**
         * The blocksize for file system I/O in bytes. This
         * corresponds to the linux <tt>stat.st_blksize</tt> field.
         */
        public long blksize;

        /**
         * Constructor
         */
        @Keep
        public Stat() {
        }

        /**
         * Returns the file type of the file.
         * @return The file type. One of <tt>S_IFXXX</tt> constants.
         * @see #isFile()
         * @see #isDirectory()
         */
        public final int getType() {
            return (mode & S_IFMT);
        }

        /**
         * Tests if the file is a regular file.
         * @return <tt>true</tt> if the file is a regular file, <tt>false</tt> otherwise.
         * @see #getType()
         * @see #isDirectory()
         */
        public final boolean isFile() {
            return ((mode & S_IFMT) == S_IFREG);
        }

        /**
         * Tests if the file is a directory.
         * @return <tt>true</tt> if the file is a directory, <tt>false</tt> otherwise.
         * @see #isFile()
         * @see #getType()
         */
        public final boolean isDirectory() {
            return ((mode & S_IFMT) == S_IFDIR);
        }

        /**
         * Returns the user name assigned to a particular {@link #uid}.
         * @return The user name if the operation succeeded, <tt>null</tt> otherwise.
         * @see #getGroupName()
         */
        public final String getUserName() {
            return ProcessUtils.getUserName(uid);
        }

        /**
         * Returns the group name assigned to a particular {@link #gid}.
         * @return The group name if the operation succeeded, <tt>null</tt> otherwise.
         * @see #getUserName()
         */
        public final String getGroupName() {
            return ProcessUtils.getGroupName(gid);
        }

        /**
         * Formats the total size to be in the form of bytes, kilobytes, megabytes, etc.
         * @param context The <tt>Context</tt>.
         * @param resId May be <tt>0</tt>. The resource id for the format string.
         * @return A formatted string with the {@link #size}.
         */
        public final String formatSize(Context context, int resId) {
            final String result = Formatter.formatFileSize(context, size);
            return (resId > 0 ? context.getString(resId, result) : result);
        }

        /**
         * Reads this object from the data stored in the specified parcel. To
         * write this object to a parcel, call {@link #writeToParcel(Parcel, int)}.
         * @param source The parcel to read the data.
         * @see #writeToParcel(Parcel, int)
         */
        public void readFromParcel(Parcel source) {
            mode    = source.readInt();
            uid     = source.readInt();
            gid     = source.readInt();
            size    = source.readLong();
            mtime   = source.readLong();
            blocks  = source.readLong();
            blksize = source.readLong();
        }

        /**
         * @see #readFromParcel(Parcel)
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mode);
            dest.writeInt(uid);
            dest.writeInt(gid);
            dest.writeLong(size);
            dest.writeLong(mtime);
            dest.writeLong(blocks);
            dest.writeLong(blksize);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public Stat clone() {
            try {
                return (Stat)super.clone();
            } catch (CloneNotSupportedException e) {
                throw new Error(e);
            }
        }

        @Override
        public String toString() {
            return new StringBuilder(256)
                .append("Stat { mode = ").append(Integer.toOctalString(mode))
                .append(", uid = ").append(uid)
                .append(", gid = ").append(gid)
                .append(", type = ").append(Integer.toOctalString(mode & S_IFMT))
                .append(", size = ").append(size)
                .append(", perm = ").append(toCharType(mode & S_IFMT))
                .append((mode & S_IRUSR) == S_IRUSR ? 'r' : '-')
                .append((mode & S_IWUSR) == S_IWUSR ? 'w' : '-')
                .append((mode & S_ISUID) == S_ISUID ? 's' : ((mode & S_IXUSR) == S_IXUSR ? 'x' : '-'))
                .append((mode & S_IRGRP) == S_IRGRP ? 'r' : '-')
                .append((mode & S_IWGRP) == S_IWGRP ? 'w' : '-')
                .append((mode & S_ISGID) == S_ISGID ? 's' : ((mode & S_IXGRP) == S_IXGRP ? 'x' : '-'))
                .append((mode & S_IROTH) == S_IROTH ? 'r' : '-')
                .append((mode & S_IWOTH) == S_IWOTH ? 'w' : '-')
                .append((mode & S_IXOTH) == S_IXOTH ? 'x' : '-')
                .append(", blocks = ").append(blocks)
                .append(", blksize = ").append(blksize)
                .append(", mtime = ").append(DateFormat.format("yyyy-MM-dd kk:mm:ss", mtime))
                .append(" }").toString();
        }

        private static char toCharType(int type) {
            switch (type) {
            case S_IFIFO:
                return 'p';

            case S_IFCHR:
                return 'c';

            case S_IFDIR:
                return 'd';

            case S_IFBLK:
                return 'b';

            case S_IFLNK:
                return 'l';

            case S_IFSOCK:
                return 's';

            default:
                return '-';
            }
        }

        public static final Creator<Stat> CREATOR = new Creator<Stat>() {
            @Override
            public Stat createFromParcel(Parcel source) {
                final Stat stat = new Stat();
                stat.readFromParcel(source);
                return stat;
            }

            @Override
            public Stat[] newArray(int size) {
                return new Stat[size];
            }
        };
    }

    /**
     * Class <tt>Dirent</tt> is wrapper for linux structure <tt>dirent</tt>.
     */
    public static class Dirent implements Parcelable, Cloneable, Comparable<Dirent> {
        /**
         * The <tt>Dirent</tt> unknown.
         */
        public static final int DT_UNKNOWN = 0;

        /**
         * The <tt>Dirent</tt> is a named pipe (FIFO).
         */
        public static final int DT_FIFO = 1;

        /**
         * The <tt>Dirent</tt> is a character device.
         */
        public static final int DT_CHR = 2;

        /**
         * The <tt>Dirent</tt> is a directory.
         */
        public static final int DT_DIR = 4;

        /**
         * The <tt>Dirent</tt> is a block device.
         */
        public static final int DT_BLK = 6;

        /**
         * The <tt>Dirent</tt> is a regular file.
         */
        public static final int DT_REG = 8;

        /**
         * The <tt>Dirent</tt> is a symbolic link.
         */
        public static final int DT_LNK = 10;

        /**
         * The <tt>Dirent</tt> is a UNIX domain socket.
         */
        public static final int DT_SOCK = 12;

        /**
         * The absolute file path.
         */
        public String path;

        /**
         * The file type. This corresponds to
         * the linux <tt>dirent.d_type</tt> field.
         */
        public int type;

        /**
         * Constructor
         * @param path The absolute file path. Never <tt>null</tt>.
         * @see #Dirent(String, int)
         * @see #Dirent(String, String, int)
         */
        public Dirent(String path) {
            DebugUtils.__checkError(path == null, "path == null");
            setPath(path);
        }

        /**
         * Constructor
         * @param path The absolute file path. Never <tt>null</tt>.
         * @param type The file type. May be one of <tt>DT_XXX</tt> constants.
         * @see #Dirent(String)
         * @see #Dirent(String, String, int)
         */
        public Dirent(String path, int type) {
            DebugUtils.__checkError(path == null, "path == null");
            this.path = path;
            this.type = type;
        }

        /**
         * Constructor
         * @param dir The path of the directory.
         * @param name The file's name of this <tt>Dirent</tt>.
         * @param type The file type. May be one of <tt>DT_XXX</tt> constants.
         * @see #Dirent(String)
         * @see #Dirent(String, int)
         */
        public Dirent(String dir, String name, int type) {
            DebugUtils.__checkError(dir == null || name == null, "dirPath == null || name == null");
            this.path = buildPath(dir, name);
            this.type = type;
        }

        /**
         * Used by derived class.
         */
        protected Dirent() {
        }

        /**
         * Tests if this <tt>Dirent</tt> is a regular file.
         * @return <tt>true</tt> if this <tt>Dirent</tt> is
         * a regular file, <tt>false</tt> otherwise.
         * @see #isDirectory()
         */
        public boolean isFile() {
            return (type == DT_REG);
        }

        /**
         * Tests if this <tt>Dirent</tt> is a directory.
         * @return <tt>true</tt> if this <tt>Dirent</tt>
         * is a directory, <tt>false</tt> otherwise.
         * @see #isFile()
         */
        public boolean isDirectory() {
            return (type == DT_DIR);
        }

        /**
         * Tests if this <tt>Dirent</tt> is a hidden file.
         * @return <tt>true</tt> if this <tt>Dirent</tt>
         * is a hidden file, <tt>false</tt> otherwise.
         */
        public boolean isHidden() {
            return FileUtils.isHidden(path);
        }

        /**
         * Returns the length of this <tt>Dirent</tt> in bytes.
         * @return The number of bytes if the operation succeeded,
         * <tt>0</tt> otherwise.
         */
        public long length() {
            return getFileLength(path);
        }

        /**
         * Returns the file's name of this <tt>Dirent</tt>.
         * @return The file's name of this <tt>Dirent</tt>.
         * @see #getParent()
         * @see #getExtension()
         */
        public String getName() {
            return getFileName(path);
        }

        /**
         * Returns the parent of this <tt>Dirent</tt>.
         * @return The parent pathname or <tt>null</tt>.
         * @see #getName()
         * @see #getExtension()
         */
        public String getParent() {
            return getFileParent(path);
        }

        /**
         * Returns the extension of this <tt>Dirent</tt>. The result string
         * excluding dot (<tt>.</tt>)
         * @return The extension, or <tt>null</tt> if this <tt>Dirent</tt>
         * is a directory or the extension was not found.
         * @see #getName()
         * @see #getParent()
         */
        public String getExtension() {
            if (type != DT_DIR) {
                final int index = findFileExtension(path);
                if (index != -1) {
                    return path.substring(index);
                }
            }

            return null;
        }

        /**
         * Return the MIME type (such as "text/plain") of this <tt>Dirent</tt>.
         * @return The MIME type of this <tt>Dirent</tt>, or <tt>null</tt> if
         * this <tt>Dirent</tt> is a directory or the MIME type was not found.
         */
        public String getMimeType() {
            return (type != DT_DIR ? HttpURLConnection.getFileNameMap().getContentTypeFor(path) : null);
        }

        /**
         * Equivalent to calling <tt>FileUtils.access(path, mode)</tt>.
         * @see FileUtils#access(String, int)
         */
        public int access(int mode) {
            return FileUtils.access(path, mode);
        }

        /**
         * Equivalent to calling <tt>FileUtils.mkdirs(path, flags)</tt>.
         * @see FileUtils#mkdirs(String, int)
         */
        public int mkdirs(int flags) {
            return FileUtils.mkdirs(path, flags);
        }

        /**
         * Equivalent to calling <tt>FileUtils.stat(path)</tt>.
         * @see #stat(Stat)
         * @see FileUtils#stat(String)
         */
        public Stat stat() {
            final Stat stat = new Stat();
            return (FileUtils.stat(path, stat) == 0 ? stat : null);
        }

        /**
         * Equivalent to calling <tt>FileUtils.stat(path, outStat)</tt>.
         * @see #stat()
         * @see FileUtils#stat(String, Stat)
         */
        public int stat(Stat outStat) {
            return FileUtils.stat(path, outStat);
        }

        /**
         * Equivalent to calling <tt>FileUtils.listFiles(path, 0)</tt>.
         * @see FileUtils#listFiles(String, int)
         */
        public List<Dirent> listFiles() {
            return FileUtils.listFiles(path, 0);
        }

        /**
         * Equivalent to calling <tt>FileUtils.scanFiles(path, callback, flags)</tt>.
         * @see FileUtils#scanFiles(String, ScanCallback, int)
         */
        public int scanFiles(ScanCallback callback, int flags) {
            return FileUtils.scanFiles(path, callback, flags);
        }

        /**
         * Reads this object from the data stored in the specified parcel. To
         * write this object to a parcel, call {@link #writeToParcel(Parcel, int)}.
         * @param source The parcel to read the data.
         * @see #writeToParcel(Parcel, int)
         */
        public void readFromParcel(Parcel source) {
            type = source.readInt();
            path = source.readString();
        }

        /**
         * @see #readFromParcel(Parcel)
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type);
            dest.writeString(path);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public Dirent clone() {
            try {
                return (Dirent)super.clone();
            } catch (CloneNotSupportedException e) {
                throw new Error(e);
            }
        }

        @Override
        public String toString() {
            return path;
        }

        @Override
        public int hashCode() {
            return 31 * type + path.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }

            if (object instanceof Dirent) {
                final Dirent dirent = (Dirent)object;
                return (type == dirent.type && path.equals(dirent.path));
            }

            return false;
        }

        /**
         * @see #compareToIgnoreCase(Dirent)
         * @see #caseInsensitiveOrder()
         */
        @Override
        public int compareTo(Dirent another) {
            if (type != another.type) {
                if (type == DT_DIR) {
                    return -1;
                } else if (another.type == DT_DIR) {
                    return 1;
                }
            }

            return path.compareTo(another.path);
        }

        /**
         * Compares the specified <tt>Dirent</tt> to this <tt>Dirent</tt>, ignoring
         * the {@link #path} field case differences.
         * @param another The <tt>Dirent</tt> to compare.
         * @return <tt>0</tt> if the <tt>Dirent</tt>s are equal; a negative integer
         * if this <tt>Dirent</tt> is less than <em>another</em>; a positive integer
         * if this <tt>Dirent</tt> is greater than <em>another</em>.
         * @see #compareTo(Dirent)
         * @see #caseInsensitiveOrder()
         */
        public int compareToIgnoreCase(Dirent another) {
            if (type != another.type) {
                if (type == DT_DIR) {
                    return -1;
                } else if (another.type == DT_DIR) {
                    return 1;
                }
            }

            return path.compareToIgnoreCase(another.path);
        }

        /**
         * Returns the file type of the specified <em>mode</em>.
         * @param mode The file protection. May be any combination
         * of <tt>Stat.S_IXXXX</tt> constants.
         * @return The file type. One of <tt>DT_XXX</tt> constants.
         * @see Stat#mode
         * @see FileUtils#getFileMode(String)
         */
        public static int getType(int mode) {
            return (mode & Stat.S_IFMT) >> 12;
        }

        /**
         * Returns a <tt>Comparator</tt> ignoring the {@link #path} field case differences.
         * @return The <tt>Comparator</tt>.
         * @see #compareTo(Dirent)
         * @see #compareToIgnoreCase(Dirent)
         */
        public static Comparator<Dirent> caseInsensitiveOrder() {
            return DirentComparator.sInstance;
        }

        public final void dump(Printer printer) {
            final String parent = getParent();
            final String mimeType  = getMimeType();
            final String extension = getExtension();
            printer.println(new StringBuilder(256).append(getClass().getSimpleName())
                   .append(" { path = ").append(path)
                   .append(", type = ").append(type).append('(').append(toString(type)).append(')')
                   .append(", parent = ").append(parent != null ? parent : "N/A")
                   .append(", name = ").append(getName())
                   .append(", extension = ").append(extension != null ? extension : "N/A")
                   .append(", mimeType = ").append(mimeType != null ? mimeType : "N/A")
                   .append(", hidden = ").append(isHidden())
                   .append(" }").toString());
        }

        /* package */ final void setPath(String path) {
            this.path = path;
            this.type = getType(getFileMode(path));
            __checkDirentType(this);
        }

        private static String toString(int type) {
            switch (type) {
            case DT_FIFO:
                return "DT_FIFO";

            case DT_CHR:
                return "DT_CHR";

            case DT_DIR:
                return "DT_DIR";

            case DT_BLK:
                return "DT_BLK";

            case DT_REG:
                return "DT_REG";

            case DT_LNK:
                return "DT_LNK";

            case DT_SOCK:
                return "DT_SOCK";

            default:
                return "DT_UNKNOWN";
            }
        }

        private static void __checkDirentType(Dirent dirent) {
            final int type;
            switch (getFileType(dirent.path)) {
            case Stat.S_IFIFO:
                type = DT_FIFO;
                break;

            case Stat.S_IFCHR:
                type = DT_CHR;
                break;

            case Stat.S_IFDIR:
                type = DT_DIR;
                break;

            case Stat.S_IFBLK:
                type = DT_BLK;
                break;

            case Stat.S_IFREG:
                type = DT_REG;
                break;

            case Stat.S_IFLNK:
                type = DT_LNK;
                break;

            case Stat.S_IFSOCK:
                type = DT_SOCK;
                break;

            default:
                type = DT_UNKNOWN;
                break;
            }

            if (type != dirent.type) {
                throw new AssertionError("Couldn't convert Stat type to Dirent type");
            }
        }

        /**
         * The {@link Factory} to create a new {@link Dirent}.
         * @see FileUtils#listFiles(String, int, Factory)
         */
        public static final Factory<Dirent> FACTORY = new Factory<Dirent>() {
            @Override
            public Dirent newInstance() {
                return new Dirent();
            }
        };

        public static final Creator<Dirent> CREATOR = new Creator<Dirent>() {
            @Override
            public Dirent createFromParcel(Parcel source) {
                final Dirent dirent = new Dirent();
                dirent.readFromParcel(source);
                return dirent;
            }

            @Override
            public Dirent[] newArray(int size) {
                return new Dirent[size];
            }
        };
    }

    /**
     * Class <tt>Properties</tt> is an implementation of a {@link java.util.Properties}.
     */
    public static class Properties extends java.util.Properties {
        private static final long serialVersionUID = -927424267767072217L;
        private final String mPath;

        /**
         * Constructor
         * @see #Properties(String)
         * @see #Properties(java.util.Properties)
         */
        public Properties() {
            mPath = null;
        }

        /**
         * Constructor
         * @param filename The properties filename,
         * must be absolute file path.
         * @see #Properties()
         * @see #Properties(java.util.Properties)
         */
        public Properties(String filename) {
            mPath = filename;
        }

        /**
         * Constructor
         * @param properties The default properties.
         * @see #Properties()
         * @see #Properties(String)
         */
        public Properties(java.util.Properties properties) {
            super(properties);
            mPath = null;
        }

        /**
         * Loads properties, assumed to be default charset.
         * @return <tt>true</tt> if the operation succeeded,
         * <tt>false</tt> otherwise.
         * @see #store()
         */
        public final boolean load() {
            DebugUtils.__checkError(mPath == null, "path == null, Must be invoke 'new Properties(filename)' constructor.");
            return load(mPath);
        }

        /**
         * Stores the mappings in this {@link Properties} object.
         * @return <tt>true</tt> if the operation succeeded,
         * <tt>false</tt> otherwise.
         * @see #load()
         */
        public final boolean store() {
            DebugUtils.__checkError(mPath == null, "path == null, Must be invoke 'new Properties(filename)' constructor.");
            return store(mPath);
        }

        /**
         * Loads properties from the specified file, assumed to be default charset.
         * @param filename The properties file to load, must be absolute file path.
         * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
         * @see #load(AssetManager, String)
         * @see #store(String)
         */
        public boolean load(String filename) {
            Reader reader = null;
            try {
                load(reader = new InputStreamReader(new FileInputStream(filename)));
                return true;
            } catch (Exception e) {
                Log.e(getClass().getName(), new StringBuilder("Couldn't load properties from - ").append(filename).append('\n').append(e).toString());
                return false;
            } finally {
                FileUtils.close(reader);
            }
        }

        /**
         * Stores the mappings in this {@link Properties} object to the specified
         * file, using default charset. <p>Note: This method will be create the
         * necessary directories.</p>
         * @param filename The properties file to store, must be absolute file path.
         * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
         * @see #load(String)
         * @see #load(AssetManager, String)
         */
        public boolean store(String filename) {
            Writer writer = null;
            try {
                mkdirs(filename, FLAG_IGNORE_FILENAME);
                store(writer = new OutputStreamWriter(new FileOutputStream(filename)), null);
                return true;
            } catch (Exception e) {
                Log.e(getClass().getName(), new StringBuilder("Couldn't store properties to - ").append(filename).toString(), e);
                return false;
            } finally {
                FileUtils.close(writer);
            }
        }

        /**
         * Loads properties from the "assets" directory's file, assumed to be default charset.
         * @param assetManager The <tt>AssetManager</tt>.
         * @param filename A relative path within the assets, such as <tt>"docs/home.html"</tt>.
         * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
         * @see #load(String)
         * @see #store(String)
         */
        public boolean load(AssetManager assetManager, String filename) {
            Reader reader = null;
            try {
                load(reader = new InputStreamReader(assetManager.open(filename, AssetManager.ACCESS_STREAMING)));
                return true;
            } catch (Exception e) {
                Log.e(getClass().getName(), new StringBuilder("Couldn't load properties from - ").append(filename).toString(), e);
                return false;
            } finally {
                FileUtils.close(reader);
            }
        }
    }

    /**
     * Regular expression for valid filenames : no spaces or metacharacters.
     */
    private static final class RegexPattern {
        public static final Pattern sInstance = Pattern.compile("[\\w%+,./=_-]+");
    }

    /**
     * Class <tt>DirentComparator</tt> compares {@link Dirent} ignoring
     * the {@link Dirent#path path} field case differences.
     */
    private static final class DirentComparator implements Comparator<Dirent> {
        public static final DirentComparator sInstance = new DirentComparator();

        @Override
        public int compare(Dirent one, Dirent another) {
            return one.compareToIgnoreCase(another);
        }
    }

    /**
     * Class <tt>ByteArrayPool</tt> used to obtain the byte array.
     */
    /* package */ static final class ByteArrayPool extends SimplePool<byte[]> {
        public static final ByteArrayPool sInstance = new ByteArrayPool();

        @Override
        public byte[] newInstance() {
            return new byte[8192];
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private FileUtils() {
    }
}
