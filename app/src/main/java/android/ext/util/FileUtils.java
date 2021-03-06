package android.ext.util;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Printer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Class FileUtils
 * @author Garfield
 */
public final class FileUtils {
    /**
     * This flag use for {@link #mkdirs(String, int)}. If
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
     * This flag use for {@link #scanFiles}. If set the
     * <tt>scanFiles</tt> will scan the descendent files.
     */
    public static final int FLAG_SCAN_FOR_DESCENDENTS = 0x02;

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
                DebugUtils.__checkLogError(true, FileUtils.class.getName(), "Couldn't close - " + c.getClass().getName(), e);
            }
        }
    }

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
            final int errno = mkdirs(cacheDir.getPath(), 0);
            if (errno != 0) {
                Log.e(FileUtils.class.getName(), "mkdirs '" + cacheDir + "' failed: errno = " + errno);
            }
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
            final int errno = mkdirs(cacheDir.getPath(), 0);
            if (errno != 0) {
                Log.e(FileUtils.class.getName(), "mkdirs '" + cacheDir + "' failed: errno = " + errno);
            }
        }

        return cacheDir;
    }

    /**
     * Tests if the <em>path</em> is an absolute path.
     * @param path The path to test.
     * @return <tt>true</tt> if the path is an absolute path, <tt>false</tt> otherwise.
     */
    public static boolean isAbsolutePath(String path) {
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
     * @return The start index of the extension (excluding dot <tt>.</tt>), or
     * <tt>-1</tt> if the extension was not found.
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
     * @return The extension, or <tt>null</tt> if the extension was not found.
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
     * @return The MIME type, or <tt>null</tt> if the MIME type was not found.
     * @see #getFileName(String)
     * @see #getFileParent(String)
     * @see #getFileExtension(String)
     */
    public static String getFileMimeType(String path) {
        return (path != null ? URLConnection.getFileNameMap().getContentTypeFor(path) : null);
    }

    /**
     * Formats a content size to be in the form of bytes, kilobytes, megabytes, etc.
     * @param sizeBytes The size value to be formatted, in bytes.
     * @return A formatted string with the <em>sizeBytes</em>.
     */
    public static String formatFileSize(long sizeBytes) {
        float result = sizeBytes;
        char suffix = 'B';
        if (result > 900) {
            suffix = 'K';
            result = result / 1024;
        }

        if (result > 900) {
            suffix = 'M';
            result = result / 1024;
        }

        if (result > 900) {
            suffix = 'G';
            result = result / 1024;
        }

        if (result > 900) {
            suffix = 'T';
            result = result / 1024;
        }

        if (result > 900) {
            suffix = 'P';
            result = result / 1024;
        }

        final StringBuilder format = new StringBuilder(8).append("%.0f %c");
        if (suffix != 'B') {
            format.append('B');
        }

        if (result < 100) {
            format.setCharAt(2, '2');
        }

        return String.format(format.toString(), result, suffix);
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
     * Returns the <em>file's</em> last modified time in milliseconds.
     * @param file The file, must be absolute file path.
     * @return The last modified time in milliseconds or <tt>0</tt>
     * if the file does not exist.
     */
    public static native long getLastModified(String file);

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
     * Moves the <em>src</em> file to <em>dst</em> file. If the <em>dst</em>
     * file already exists, it will be override to. <p>Note: This method
     * will be create the necessary directories.</p>
     * @param src The source file to move, must be absolute file path.
     * @param dst The destination file to move to, must be absolute file path.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns
     * an error code. See {@link ErrnoException}.
     */
    public static native int moveFile(String src, String dst);

    /**
     * Returns the total number of bytes with specified <em>file</em>. if <em>file</em>
     * is a directory, all sub files will be computed.
     * @param file The file or directory to compute, must be absolute file path.
     * @return The total number of bytes or <tt>0</tt> if the file does not exist.
     */
    public static native long computeFileSize(String file);

    /**
     * Compares the two specified file's contents are equal.
     * @param file1 The first file to compare, must be absolute file path.
     * @param file2 The second file to compare, must be absolute file path.
     * @return <tt>true</tt> if file1's contents and file2's contents are equal,
     * <tt>false</tt> otherwise.
     */
    public static native boolean compareFile(String file1, String file2);

    /**
     * Creates a file with the specified <em>filename</em>. If the file was
     * created the file's length is the <em>length</em> and the content is
     * empty. If the specified file already exists, it can be overrided to.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param filename The filename to create, must be absolute file path.
     * @param length The desired file length in bytes.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns
     * an error code. See {@link ErrnoException}.
     */
    public static native int createFile(String filename, long length);

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
     * Creates a unique file with the specified <em>filename</em>. If the file
     * was created the file's length is the <em>length</em> and the content is
     * empty. <p>Note: This method will be create the necessary directories.</p>
     * @param filename The original filename to create, must be absolute file path.
     * @param length The desired file length in bytes.
     * @return Returns the unique filename (include file path), or <tt>null</tt>
     * if the file could't be created.
     */
    public static native String createUniqueFile(String filename, long length);

    /**
     * Returns a <tt>List</tt> of {@link Dirent} objects with the sub files and directories in the <em>dirPath</em>.
     * <p>The entries <tt>.</tt> and <tt>..</tt> representing the current and parent directory are not returned as
     * part of the list.</p>
     * @param dirPath The directory path, must be absolute file path.
     * @param flags The flags. May be <tt>0</tt> or any combination of {@link #FLAG_IGNORE_HIDDEN_FILE} and
     * {@link #FLAG_SCAN_FOR_DESCENDENTS}.
     * @return A <tt>List</tt> of {@link Dirent} objects if the operation succeeded, <tt>null</tt> otherwise.
     * @see #listFiles(String, int, Collection)
     */
    public static List<Dirent> listFiles(String dirPath, int flags) {
        final List<Dirent> result = new ArrayList<Dirent>();
        return (scanFiles(dirPath, FileUtils::onScanFile, flags, result) == 0 ? result : null);
    }

    /**
     * Returns a <tt>Collection</tt> of {@link Dirent} objects with the sub files and directories in the <em>dirPath</em>.
     * <p>The entries <tt>.</tt> and <tt>..</tt> representing the current and parent directory are not returned as part
     * of the list.</p>
     * @param dirPath The directory path, must be absolute file path.
     * @param flags The flags. May be <tt>0</tt> or any combination of {@link #FLAG_IGNORE_HIDDEN_FILE} and
     * {@link #FLAG_SCAN_FOR_DESCENDENTS}.
     * @param outDirents A <tt>Collection</tt> to store the {@link Dirent} objects.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an error code. See {@link ErrnoException}.
     * @see #listFiles(String, int)
     */
    public static int listFiles(String dirPath, int flags, Collection<Dirent> outDirents) {
        return scanFiles(dirPath, FileUtils::onScanFile, flags, outDirents);
    }

    /**
     * Scans all subfiles and directories in the specified <em>dirPath</em>. <p>The entries <tt>.</tt>
     * and <tt>..</tt> representing the current and parent directory are not scanned.</p>
     * @param dirPath The directory path, must be absolute file path.
     * @param callback The {@link ScanCallback} used to scan.
     * @param flags The scan flags. May be <tt>0</tt> or any combination of {@link #FLAG_IGNORE_HIDDEN_FILE}
     * and {@link #FLAG_SCAN_FOR_DESCENDENTS}.
     * @param cookie An object by user-defined that gets passed into {@link ScanCallback#onScanFile}.
     * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an error code. See {@link ErrnoException}.
     */
    public static native int scanFiles(String dirPath, ScanCallback callback, int flags, Object cookie);

    /**
     * Copies the specified file contents to the specified <em>outFile</em>.
     * <p>Note: This method will be create the necessary directories.</p>
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>{@link File} (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to read.
     * @param outFile The destination file to write, must be absolute file path.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled,
     * or <tt>null</tt> if none. If the operation was cancelled before it completed
     * normally the <em>outFile's</em> contents is undefined.
     * @throws IOException if an error occurs while writing to <em>outFile</em>.
     * @see UriUtils#openInputStream(Context, Object)
     */
    public static void copyFile(Context context, Object uri, File outFile, Cancelable cancelable) throws IOException {
        FileUtils.mkdirs(outFile.getPath(), FLAG_IGNORE_FILENAME);
        try (final OutputStream os = new FileOutputStream(outFile)) {
            readFile(context, uri, os, cancelable);
        }
    }

    /**
     * Copies the specified <tt>InputStream</tt> the contents to <tt>outFile</tt>.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param is The <tt>InputStream</tt> to read.
     * @param outFile The destination file to write, must be absolute file path.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled,
     * or <tt>null</tt> if none. If the operation was cancelled before it completed
     * normally the <em>outFile's</em> contents is undefined.
     * @throws IOException if an error occurs while writing to <em>outFile</em>.
     * @see #copyStream(InputStream, OutputStream, Cancelable, byte[])
     */
    public static void copyStream(InputStream is, File outFile, Cancelable cancelable) throws IOException {
        FileUtils.mkdirs(outFile.getPath(), FLAG_IGNORE_FILENAME);
        try (final OutputStream os = new FileOutputStream(outFile)) {
            copyStream(is, os, cancelable, null);
        }
    }

    /**
     * Copies the specified <tt>InputStream</tt> the contents into <tt>OutputStream</tt>.
     * @param is The <tt>InputStream</tt> to read.
     * @param out The <tt>OutputStream</tt> to write.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or
     * <tt>null</tt> if none. If the operation was cancelled before it completed normally
     * the <em>out's</em> contents is undefined.
     * @param buffer May be <tt>null</tt>. The temporary byte array to store the read bytes.
     * @throws IOException if an error occurs while writing to <em>out</em>.
     * @see #copyStream(InputStream, File, Cancelable)
     */
    public static void copyStream(InputStream is, OutputStream out, Cancelable cancelable, byte[] buffer) throws IOException {
        if (out instanceof ByteArrayBuffer) {
            ((ByteArrayBuffer)out).readFrom(is, is.available(), cancelable);
        } else if (is instanceof FileInputStream && out instanceof FileOutputStream) {
            copyStreamImpl((FileInputStream)is, (FileOutputStream)out, Cancelable.ofNullable(cancelable));
        } else if (buffer == null) {
            copyStreamImpl(is, out, Cancelable.ofNullable(cancelable));
        } else {
            copyStreamImpl(is, out, Cancelable.ofNullable(cancelable), buffer);
        }
    }

    /**
     * Equivalent to calling <tt>readFile(context, uri, new ByteArrayBuffer(), cancelable)</tt>.
     * @param context The <tt>Context</tt>.
     * @param uri The uri to read.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or
     * <tt>null</tt> if none. If the operation was cancelled before it completed normally
     * the returned <tt>ByteArrayBuffer</tt> is undefined.
     * @return A <tt>ByteArrayBuffer</tt> contains the file contents.
     * @throws IOException if an error occurs while writing to <tt>ByteArrayBuffer</tt>.
     * @see #readFile(Context, Object, OutputStream, Cancelable)
     */
    public static ByteArrayBuffer readFile(Context context, Object uri, Cancelable cancelable) throws IOException {
        final ByteArrayBuffer result = new ByteArrayBuffer();
        readFile(context, uri, result, cancelable);
        return result;
    }

    /**
     * Reads the specified file contents into the specified <em>out</em>.
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>{@link File} (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to read.
     * @param out The <tt>OutputStream</tt> to write to.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or
     * <tt>null</tt> if none. If the operation was cancelled before it completed normally
     * the <em>out's</em> contents is undefined.
     * @throws IOException if an error occurs while writing to <em>out</em>.
     * @see #readFile(Context, Object, Cancelable)
     * @see UriUtils#openInputStream(Context, Object)
     */
    public static void readFile(Context context, Object uri, OutputStream out, Cancelable cancelable) throws IOException {
        try (final InputStream is = UriUtils.openInputStream(context, uri)) {
            copyStream(is, out, cancelable, null);
        }
    }

    /**
     * Copies the specified <tt>InputStream's</tt> contents into the <tt>OutputStream</tt>.
     */
    /* package */ static void copyStreamImpl(InputStream is, OutputStream out, Cancelable cancelable, byte[] buffer) throws IOException {
        DebugUtils.__checkError(buffer == null, "Invalid parameter - buffer == null");
        for (int readBytes, offset = 0; !cancelable.isCancelled(); ) {
            if ((readBytes = is.read(buffer, offset, buffer.length - offset)) == -1) {
                // Writes the last remaining bytes of the buffer.
                out.write(buffer, 0, offset);
                break;
            }

            offset += readBytes;
            if (offset == buffer.length) {
                // The buffer full, write to OutputStream and reset the offset.
                offset = 0;
                out.write(buffer, 0, buffer.length);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static int onScanFile(String path, int type, Object cookie) {
        ((Collection<Object>)cookie).add(new Dirent(path, type));
        return ScanCallback.SC_CONTINUE;
    }

    /**
     * Called by native code.
     */
    @Keep
    @SuppressWarnings("unused")
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
     * Copies the specified <tt>InputStream's</tt> contents into the <tt>OutputStream</tt>.
     */
    private static void copyStreamImpl(InputStream is, OutputStream out, Cancelable cancelable) throws IOException {
        final byte[] buffer = Pools.BYTE_ARRAY_POOL.obtain();
        try {
            copyStreamImpl(is, out, cancelable, buffer);
        } finally {
            Pools.BYTE_ARRAY_POOL.recycle(buffer);
        }
    }

    /**
     * Copies the specified <tt>InputStream's</tt> contents into the <tt>OutputStream</tt>.
     */
    private static void copyStreamImpl(FileInputStream is, FileOutputStream out, Cancelable cancelable) throws IOException {
        try (final FileChannel src = is.getChannel(); final FileChannel dst = out.getChannel()) {
            DebugUtils.__checkStartMethodTracing();
            long writtenBytes, position = 0, size = src.size();
            while (size > 0 && !cancelable.isCancelled()) {
                writtenBytes = src.transferTo(position, size, dst);
                size -= writtenBytes;
                position += writtenBytes;
            }
            DebugUtils.__checkStopMethodTracing("FileUtils", "transferTo");
        }
    }

    /**
     * Callback interface used to {@link FileUtils#scanFiles(String, ScanCallback, int, Object)}.
     */
    public static interface ScanCallback {
        /**
         * The returned value of {@link #onScanFile}
         * indicates to continue to scan.
         */
        public int SC_CONTINUE = 0;

        /**
         * The returned value of {@link #onScanFile}
         * indicates to stop all scans.
         */
        public int SC_STOP = 1;

        /**
         * The returned value of {@link #onScanFile}
         * indicates to stop the current directory scan.
         */
        public int SC_BREAK = 2;

        /**
         * The returned value of {@link #onScanFile}
         * indicates to stop the current file's parent directory.
         */
        public int SC_BREAK_PARENT = 3;

        /**
         * This callback is invoked on the {@link FileUtils#scanFiles} was called from.
         * @param path The absolute file path.
         * @param type The file type. May be one of <tt>Dirent.DT_XXX</tt> constants.
         * @param cookie An object, passed earlier by {@link FileUtils#scanFiles}.
         * @return One of {@link #SC_CONTINUE}, {@link #SC_STOP}, {@link #SC_BREAK}
         * or {@link #SC_BREAK_PARENT}.
         */
        @Keep
        public int onScanFile(String path, int type, Object cookie);
    }

    /**
     * Class <tt>Stat</tt> is wrapper for linux structure <tt>stat</tt>.
     */
    public static final class Stat implements Parcelable {
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
         * The last modify time, in milliseconds since
         * January 1st, 1970, midnight.
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
         * @see #Stat(Stat)
         */
        @Keep
        public Stat() {
        }

        /**
         * Copy constructor
         * @param from The <tt>Stat</tt> to copy.
         * @see #Stat()
         */
        public Stat(Stat from) {
            DebugUtils.__checkError(from == null, "Invalid parameter - from == null");
            this.mode    = from.mode;
            this.uid     = from.uid;
            this.gid     = from.gid;
            this.size    = from.size;
            this.mtime   = from.mtime;
            this.blocks  = from.blocks;
            this.blksize = from.blksize;
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
         * Formats the total size to be in the from of bytes, kilobytes, megabytes, etc.
         * @param context The <tt>Context</tt>.
         * @param resId The resource id for the format string.
         * @return A formatted string with the {@link #size}.
         */
        public final String formatSize(Context context, int resId) {
            return context.getString(resId, formatFileSize(size));
        }

        public final void dump(Printer printer) {
            final String[] type = toType(mode);
            printer.println(new StringBuilder(256).append(getClass().getSimpleName())
                .append(" { mode = ").append(Integer.toOctalString(mode))
                .append(", user = ").append(getUserName()).append('(').append(uid).append(')')
                .append(", group = ").append(getGroupName()).append('(').append(gid).append(')')
                .append(", type = ").append(type[0]).append('(').append(Integer.toOctalString(mode & S_IFMT)).append(')')
                .append(", size = ").append(size).append('(').append(formatFileSize(size)).append(')')
                .append(", perm = ").append(type[1])
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
                .append(" }").toString());
        }

        /**
         * Reads this object from the data stored in the specified parcel. To
         * write this object to a parcel, call {@link #writeToParcel(Parcel, int)}.
         * @param source The parcel to read the data.
         * @see #writeToParcel(Parcel, int)
         */
        public void readFromParcel(Parcel source) {
            this.mode    = source.readInt();
            this.uid     = source.readInt();
            this.gid     = source.readInt();
            this.size    = source.readLong();
            this.mtime   = source.readLong();
            this.blocks  = source.readLong();
            this.blksize = source.readLong();
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

        private static String[] toType(int mode) {
            switch (mode & S_IFMT) {
            case S_IFIFO:
                return new String[] { "S_IFIFO", "p" };

            case S_IFCHR:
                return new String[] { "S_IFCHR", "c" };

            case S_IFDIR:
                return new String[] { "S_IFDIR", "d" };

            case S_IFBLK:
                return new String[] { "S_IFBLK", "b" };

            case S_IFLNK:
                return new String[] { "S_IFLNK", "l" };

            case S_IFSOCK:
                return new String[] { "S_IFSOCK", "s" };

            default:
                return new String[] { "S_IFREG", "-" };
            }
        }
    }

    /**
     * Class <tt>Dirent</tt> is wrapper for linux structure <tt>dirent</tt>.
     */
    public static final class Dirent implements Comparable<Dirent> {
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
        public final String path;

        /**
         * The file type. This corresponds to
         * the linux <tt>dirent.d_type</tt> field.
         */
        public final int type;

        /**
         * Constructor
         * @param path The absolute file path.
         * @param type The file type. May be one of <tt>DT_XXX</tt> constants.
         * @see #Dirent(String, String, int)
         */
        public Dirent(String path, int type) {
            DebugUtils.__checkError(StringUtils.getLength(path) == 0, "Invalid parameter - path == null || path.length() == 0");
            Dirent.__checkType(type);
            this.path = path;
            this.type = type;
        }

        /**
         * Constructor
         * @param dir The absolute path of the directory.
         * @param name The name of the file.
         * @param type The file type. May be one of <tt>DT_XXX</tt> constants.
         * @see #Dirent(String, int)
         */
        public Dirent(String dir, String name, int type) {
            DebugUtils.__checkError(StringUtils.getLength(dir) == 0, "Invalid parameter - dir == null || dir.length() == 0");
            Dirent.__checkType(type);
            this.path = new File(dir, name).getPath();
            this.type = type;
        }

        /**
         * Tests if this <tt>Dirent</tt> is a regular file.
         * @return <tt>true</tt> if this <tt>Dirent</tt> is
         * a regular file, <tt>false</tt> otherwise.
         * @see #isDirectory()
         */
        public final boolean isFile() {
            return (type == DT_REG);
        }

        /**
         * Tests if this <tt>Dirent</tt> is a directory.
         * @return <tt>true</tt> if this <tt>Dirent</tt>
         * is a directory, <tt>false</tt> otherwise.
         * @see #isFile()
         */
        public final boolean isDirectory() {
            return (type == DT_DIR);
        }

        /**
         * Tests if this <tt>Dirent</tt> is a hidden file.
         * @return <tt>true</tt> if this <tt>Dirent</tt>
         * is a hidden file, <tt>false</tt> otherwise.
         */
        public final boolean isHidden() {
            return FileUtils.isHidden(path);
        }

        /**
         * Returns the file's name of this <tt>Dirent</tt>.
         * @return The file's name of this <tt>Dirent</tt>.
         * @see #getParent()
         * @see #getExtension()
         */
        public final String getName() {
            return getFileName(path);
        }

        /**
         * Returns the parent of this <tt>Dirent</tt>.
         * @return The parent pathname or <tt>null</tt>.
         * @see #getName()
         * @see #getExtension()
         */
        public final String getParent() {
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
        public final String getExtension() {
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
        public final String getMimeType() {
            return (type != DT_DIR ? URLConnection.getFileNameMap().getContentTypeFor(path) : null);
        }

        /**
         * Equivalent to calling <tt>FileUtils.mkdirs(path, flags)</tt>.
         * @see FileUtils#mkdirs(String, int)
         */
        public final int mkdirs(int flags) {
            return FileUtils.mkdirs(path, flags);
        }

        /**
         * Equivalent to calling <tt>FileUtils.stat(path)</tt>.
         * @see FileUtils#stat(String)
         */
        public final Stat stat() {
            final Stat stat = new Stat();
            return (FileUtils.stat(path, stat) == 0 ? stat : null);
        }

        /**
         * Equivalent to calling <tt>FileUtils.listFiles(path, flags)</tt>.
         * @see FileUtils#listFiles(String, int)
         */
        public final List<Dirent> listFiles(int flags) {
            return FileUtils.listFiles(path, flags);
        }

        @Override
        public String toString() {
            return path;
        }

        @Override
        public int hashCode() {
            return 31 * type + path.hashCode();
        }

        /**
         * @see #equalsIgnoreCase(Dirent)
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }

            if (object instanceof Dirent) {
                final Dirent dirent = (Dirent)object;
                return (type == dirent.type && path.equals(dirent.path));
            }

            return false;
        }

        /**
         * Compares this <tt>Dirent</tt> to another <tt>Dirent</tt>, ignoring
         * the {@link #path} field case differences.
         * @param another The <tt>Dirent</tt> to compare.
         * @return <tt>true</tt> if the given the <tt>Dirent</tt> equivalent
         * to this <tt>Dirent</tt>, <tt>false</tt> otherwise.
         * @see #equals(Object)
         */
        public boolean equalsIgnoreCase(Dirent another) {
            return (another != null && type == another.type && path.equalsIgnoreCase(another.path));
        }

        /**
         * @see #compareToIgnoreCase(Dirent)
         * @see #caseInsensitiveOrder()
         */
        @Override
        public int compareTo(Dirent another) {
            DebugUtils.__checkError(another == null, "Invalid parameter - another == null");
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
         * Compares this <tt>Dirent</tt> to another <tt>Dirent</tt>, ignoring the
         * {@link #path} field case differences.
         * @param another The <tt>Dirent</tt> to compare.
         * @return <tt>0</tt> if the <tt>Dirent</tt>s are equal; a negative integer
         * if this <tt>Dirent</tt> is less than <em>another</em>; a positive integer
         * if this <tt>Dirent</tt> is greater than <em>another</em>.
         * @see #compareTo(Dirent)
         * @see #caseInsensitiveOrder()
         */
        public int compareToIgnoreCase(Dirent another) {
            DebugUtils.__checkError(another == null, "Invalid parameter - another == null");
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
         * Returns a {@link Dirent} type from the specified <em>mode</em>.
         * @param mode The mode of the {@link Stat}.
         * @return One of <tt>Dirent.DT_XXX</tt> constants.
         */
        public static int getFileType(int mode) {
            Dirent.__checkType((mode & Stat.S_IFMT) >> 12);
            return ((mode & Stat.S_IFMT) >> 12);
        }

        /**
         * Returns a <tt>Comparator</tt> ignoring the {@link #path} field case differences.
         * @return The <tt>Comparator</tt>.
         * @see #compareTo(Dirent)
         * @see #compareToIgnoreCase(Dirent)
         */
        public static Comparator<Dirent> caseInsensitiveOrder() {
            return Dirent::compareToIgnoreCase;
        }

        public final void dump(Printer printer) {
            final String parent = getParent();
            final String mimeType  = getMimeType();
            final String extension = getExtension();
            printer.println(new StringBuilder(256).append(getClass().getSimpleName())
                .append(" { path = ").append(path)
                .append(", type = ").append(toString(type))
                .append(", parent = ").append(parent != null ? parent : "N/A")
                .append(", name = ").append(getName())
                .append(", extension = ").append(extension != null ? extension : "N/A")
                .append(", mimeType = ").append(mimeType != null ? mimeType : "N/A")
                .append(", hidden = ").append(isHidden())
                .append(" }").toString());
        }

        private static String toString(int type) {
            switch (type) {
            case DT_FIFO:
                return type + "(DT_FIFO)";

            case DT_CHR:
                return type + "(DT_CHR)";

            case DT_DIR:
                return type + "(DT_DIR)";

            case DT_BLK:
                return type + "(DT_BLK)";

            case DT_REG:
                return type + "(DT_REG)";

            case DT_LNK:
                return type + "(DT_LNK)";

            case DT_SOCK:
                return type + "(DT_SOCK)";

            default:
                return type + "(DT_UNKNOWN)";
            }
        }

        private static void __checkType(int type) {
            switch (type) {
            case DT_UNKNOWN:
            case DT_FIFO:
            case DT_CHR:
            case DT_DIR:
            case DT_BLK:
            case DT_REG:
            case DT_LNK:
            case DT_SOCK:
                break;

            default:
                throw new AssertionError("Unknown Dirent type - " + type);
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private FileUtils() {
    }
}
