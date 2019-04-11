package android.ext.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import android.ext.util.ArrayUtils.ByteArrayPool;
import android.ext.util.FileUtils.Dirent;

/**
 * Class ZipUtils
 * @author Garfield
 */
public final class ZipUtils {
    /**
     * Compresses the data from the {@link InputStream} to {@link OutputStream}.
     * @param is The <tt>InputStream</tt> to read data.
     * @param out The <tt>OutputStream</tt> to write the compressed data.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or
     * <tt>null</tt> if none. If the operation was cancelled before it completed normally
     * the <em>out's</em> contents is undefined.
     * @throws IOException if an error occurs while compressing data.
     * @see #compress(byte[], int, int, OutputStream)
     * @see #uncompress(InputStream, OutputStream, Cancelable)
     */
    public static void compress(InputStream is, OutputStream out, Cancelable cancelable) throws IOException {
        final GZIPOutputStream gzip = new GZIPOutputStream(out);
        try {
            FileUtils.copyStream(is, gzip, cancelable, null);
            gzip.finish();
        } finally {
            gzip.close();
        }
    }

    /**
     * Compresses the specified byte array <em>data</em> to {@link OutputStream}.
     * @param data The byte array to compress.
     * @param offset The start position in the <em>data</em>.
     * @param count The number of bytes from <em>data</em> to compress.
     * @param out The <tt>OutputStream</tt> to write the compressed data.
     * @throws IOException if an error occurs while compressing data.
     * @see #compress(InputStream, OutputStream, Cancelable)
     * @see #uncompress(InputStream, OutputStream, Cancelable)
     */
    public static void compress(byte[] data, int offset, int count, OutputStream out) throws IOException {
        final GZIPOutputStream gzip = new GZIPOutputStream(out);
        try {
            gzip.write(data, offset, count);
            gzip.finish();
        } finally {
            gzip.close();
        }
    }

    /**
     * Uncompresses the GZIP data from the {@link InputStream} to {@link OutputStream}.
     * @param is The <tt>InputStream</tt> to read data.
     * @param out The <tt>OutputStream</tt> to write the uncompressed data.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or
     * <tt>null</tt> if none. If the operation was cancelled before it completed normally
     * the <em>out's</em> contents is undefined.
     * @throws IOException if an error occurs while compressing data.
     * @see #compress(byte[], int, int, OutputStream)
     * @see #compress(InputStream, OutputStream, Cancelable)
     */
    public static void uncompress(InputStream is, OutputStream out, Cancelable cancelable) throws IOException {
        final GZIPInputStream gzip = new GZIPInputStream(is);
        try {
            FileUtils.copyStream(gzip, out, cancelable, null);
        } finally {
            gzip.close();
        }
    }

    /**
     * Equivalent to calling <tt>compress(zipFile, compressionLevel, Arrays.asList(files), cancelable)</tt>.
     * @param zipFile The ZIP filename, must be absolute file path.
     * @param compressionLevel The compression level to be used for writing entry data.
     * See {@link ZipOutputStream#setLevel(int)}.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt> if none.
     * If the operation was cancelled before it completed normally the <em>zipFile's</em> contents is undefined.
     * @param files An array of pathnames to compress, must be absolute file path.
     * @throws IOException if an error occurs while compressing <em>files</em> contents.
     * @see #compress(String, int, List, Cancelable)
     * @see #uncompress(String, String, Cancelable)
     */
    public static void compress(String zipFile, int compressionLevel, Cancelable cancelable, String... files) throws IOException {
        DebugUtils.__checkError(files == null, "Invalid parameter - The files is null");
        compress(zipFile, compressionLevel, Arrays.asList(files), cancelable);
    }

    /**
     * Compresses the specified <em>files</em> contents to ZIP file.
     * @param zipFile The ZIP filename, must be absolute file path.
     * @param compressionLevel The compression level to be used for writing entry data.
     * See {@link ZipOutputStream#setLevel(int)}.
     * @param files A <tt>List</tt> of pathnames to compress, must be absolute file path.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or
     * <tt>null</tt> if none. If the operation was cancelled before it completed normally
     * the <em>zipFile's</em> contents is undefined.
     * @throws IOException if an error occurs while compressing <em>files</em> contents.
     * @see #compress(String, int, Cancelable, String[])
     * @see #uncompress(String, String, Cancelable)
     */
    public static void compress(String zipFile, int compressionLevel, List<String> files, Cancelable cancelable) throws IOException {
        DebugUtils.__checkError(files == null, "Invalid parameter - The files is null");
        // Creates the necessary directories.
        FileUtils.mkdirs(zipFile, FileUtils.FLAG_IGNORE_FILENAME);
        cancelable = FileUtils.wrap(cancelable);

        // Creates the ZipOutputStream.
        final ZipOutputStream os = new ZipOutputStream(new FileOutputStream(zipFile));
        os.setLevel(compressionLevel);

        final byte[] buffer = ByteArrayPool.obtain();
        try {
            // Compresses the files.
            final int size = ArrayUtils.getSize(files);
            for (int i = 0; i < size && !cancelable.isCancelled(); ++i) {
                final Dirent dirent = new Dirent(files.get(i));
                compress(os, dirent, dirent.getName(), cancelable, buffer);
            }
        } finally {
            ByteArrayPool.recycle(buffer);
            os.close();
        }
    }

    /**
     * Uncompresses the ZIP file. <p>Note: that this method creates the necessary directories.</p>
     * @param zipFile The ZIP filename to uncompress, must be absolute file path.
     * @param outPath The uncompressed path, must be absolute path.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt>
     * if none. If the operation was cancelled before it completed normally the uncompressed files in
     * <em>outPath</em> is undefined.
     * @throws IOException if an error occurs while uncompressing ZIP file.
     * @see #compress(String, int, List, Cancelable)
     * @see #compress(String, int, Cancelable, String[])
     */
    public static void uncompress(String zipFile, String outPath, Cancelable cancelable) throws IOException {
        final ZipFile file  = new ZipFile(zipFile);
        final byte[] buffer = ByteArrayPool.obtain();
        try {
            // Creates the necessary directories.
            FileUtils.mkdirs(outPath, 0);
            cancelable = FileUtils.wrap(cancelable);

            // Enumerates the ZIP file entries.
            final CRC32 crc = new CRC32();
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements() && !cancelable.isCancelled()) {
                final ZipEntry entry = entries.nextElement();
                final File pathName  = new File(outPath, entry.getName());

                // Creates the sub directory.
                if (entry.isDirectory()) {
                    FileUtils.mkdirs(pathName.getPath(), 0);
                } else {
                    uncompress(file, entry, pathName, crc, cancelable, buffer);
                }
            }
        } finally {
            ByteArrayPool.recycle(buffer);
            file.close();
        }
    }

    private static void compress(ZipOutputStream os, Dirent dirent, String name, Cancelable cancelable, byte[] buffer) throws IOException {
        if (dirent.isDirectory()) {
            // Adds the directory ZipEntry.
            name += "/";
            os.putNextEntry(new ZipEntry(name));

            // Lists the sub files.
            final List<Dirent> dirents = dirent.listFiles(0);
            final int size = ArrayUtils.getSize(dirents);
            for (int i = 0; i < size && !cancelable.isCancelled(); ++i) {
                final Dirent child = dirents.get(i);
                compress(os, child, name + child.getName(), cancelable, buffer);
            }
        } else {
            // Adds the file ZipEntry.
            os.putNextEntry(new ZipEntry(name));

            // Copy the file's contents to ZipOutputStream.
            final InputStream is = new FileInputStream(dirent.path);
            try {
                FileUtils.copyStreamImpl(is, os, cancelable, buffer);
            } finally {
                is.close();
            }
        }
    }

    private static void uncompress(ZipFile file, ZipEntry entry, File filename, CRC32 crc, Cancelable cancelable, byte[] buffer) throws IOException {
        InputStream is  = null;
        OutputStream os = null;
        try {
            // Opens the InputStream and OutputStream.
            is = file.getInputStream(entry);
            os = new FileOutputStream(filename);

            if (entry.getCrc() <= 0) {
                // Uncompress the ZIP entry.
                FileUtils.copyStreamImpl(is, os, cancelable, buffer);
            } else {
                // Uncompress the ZIP entry with check CRC32.
                uncompress(entry, is, os, crc, cancelable, buffer);
            }
        } finally {
            FileUtils.close(is);
            FileUtils.close(os);
        }
    }

    private static void uncompress(ZipEntry entry, InputStream is, OutputStream os, CRC32 crc, Cancelable cancelable, byte[] buffer) throws IOException {
        // Uncompress the ZIP entry with check CRC32.
        crc.reset();
        int readBytes;
        while ((readBytes = is.read(buffer, 0, buffer.length)) > 0) {
            if (cancelable.isCancelled()) {
                return;
            }

            os.write(buffer, 0, readBytes);
            crc.update(buffer, 0, readBytes);
        }

        // Checks the uncompressed content CRC32.
        final long crcValue = entry.getCrc();
        final long checksum = crc.getValue();
        if (crcValue != checksum) {
            final String name = entry.getName();
            throw new IOException("Checked the '" + name + "' CRC32 failed [ " + name + " CRC32 = " + crcValue + ", Computed CRC32 = " + checksum + " ]");
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ZipUtils() {
    }
}
