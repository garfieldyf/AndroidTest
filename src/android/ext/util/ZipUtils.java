package android.ext.util;

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
import android.ext.util.FileUtils.Dirent;

/**
 * Class ZipUtils
 * @author Garfield
 * @version 1.5
 */
public final class ZipUtils {
    /**
     * Compresses the data from the {@link InputStream} to {@link OutputStream}.
     * @param is The <tt>InputStream</tt> to read data.
     * @param out The <tt>OutputStream</tt> to write the compressed data.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or
     * <tt>null</tt> if none. If the operation was cancelled before it completed normally
     * then the <em>out's</em> contents is undefined.
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
     * then the <em>out's</em> contents is undefined.
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
     * Compresses the specified <em>files</em> contents to ZIP file.
     * @param zipFile The ZIP filename.
     * @param compressionLevel The compression level to be used for writing entry data.
     * See {@link ZipOutputStream#setLevel(int)}.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or
     * <tt>null</tt> if none. If the operation was cancelled before it completed normally
     * then the <em>zipFile's</em> contents is undefined.
     * @param files An array of filenames to compress, must be absolute file path.
     * @throws IOException if an error occurs while compressing <em>files</em> contents.
     * @see #compress(String, int, List, Cancelable)
     * @see #uncompress(String, String, Cancelable)
     */
    public static void compress(String zipFile, int compressionLevel, Cancelable cancelable, String... files) throws IOException {
        compress(zipFile, compressionLevel, Arrays.asList(files), cancelable);
    }

    /**
     * Compresses the specified <em>files</em> contents to ZIP file.
     * @param zipFile The ZIP filename.
     * @param compressionLevel The compression level to be used for writing entry data.
     * See {@link ZipOutputStream#setLevel(int)}.
     * @param files A <tt>List</tt> of filenames to compress, must be absolute file path.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or
     * <tt>null</tt> if none. If the operation was cancelled before it completed normally
     * then the <em>zipFile's</em> contents is undefined.
     * @throws IOException if an error occurs while compressing <em>files</em> contents.
     * @see #compress(String, int, Cancelable, String[])
     * @see #uncompress(String, String, Cancelable)
     */
    public static void compress(String zipFile, int compressionLevel, List<String> files, Cancelable cancelable) throws IOException {
        // Creates the necessary directories.
        DebugUtils.__checkError(ArrayUtils.getSize(files) == 0, "Invalid parameter - The files is null or 0-size");
        FileUtils.mkdirs(zipFile, FileUtils.FLAG_IGNORE_FILENAME);

        // Creates the ZipOutputStream.
        final ZipOutputStream os = new ZipOutputStream(new FileOutputStream(zipFile));
        os.setLevel(compressionLevel);

        try {
            // Compresses the files.
            final Dirent dirent = new Dirent();
            for (int i = 0, size = ArrayUtils.getSize(files); i < size; ++i) {
                dirent.setPath(files.get(i));
                compress(os, dirent, dirent.getName(), cancelable);
            }
        } finally {
            os.close();
        }
    }

    /**
     * Uncompresses the ZIP file. <p>Note that this method creates the necessary directories.</p>
     * @param zipFile The ZIP filename to uncompress.
     * @param outPath The uncompressed path, must be absolute path.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt>
     * if none. If the operation was cancelled before it completed normally then the uncompressed files
     * in <em>outPath</em> is undefined.
     * @throws IOException if an error occurs while uncompressing ZIP file.
     * @see #compress(String, int, List, Cancelable)
     * @see #compress(String, int, Cancelable, String[])
     */
    public static void uncompress(String zipFile, String outPath, Cancelable cancelable) throws IOException {
        final ZipFile file = new ZipFile(zipFile);
        try {
            // Creates the necessary directories.
            FileUtils.mkdirs(outPath, 0);

            // Enumerates the ZIP file entries.
            final byte[] buf = new byte[8192];
            final CRC32 crc  = new CRC32();
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry  = entries.nextElement();
                final String pathName = FileUtils.buildPath(outPath, entry.getName());

                // Creates the sub directory.
                if (entry.isDirectory()) {
                    FileUtils.mkdirs(pathName, 0);
                } else {
                    uncompress(file, entry, pathName, crc, buf, cancelable);
                }
            }
        } finally {
            file.close();
        }
    }

    private static void compress(ZipOutputStream os, Dirent dirent, String name, Cancelable cancelable) throws IOException {
        if (dirent.isDirectory()) {
            // Adds the directory ZipEntry.
            name += '/';
            os.putNextEntry(new ZipEntry(name));

            // lists the sub files from path.
            final List<Dirent> dirents = dirent.listFiles();
            for (int i = 0, size = dirents.size(); i < size; ++i) {
                final Dirent child = dirents.get(i);
                compress(os, child, name + child.getName(), cancelable);
            }
        } else {
            // Adds the file ZipEntry.
            os.putNextEntry(new ZipEntry(name));

            // Reads the file's contents to ZipOutputStream.
            final InputStream is = new FileInputStream(dirent.path);
            try {
                FileUtils.copyStream(is, os, cancelable, null);
            } finally {
                is.close();
            }
        }
    }

    private static void uncompress(ZipFile file, ZipEntry entry, String pathName, CRC32 crc, byte[] buffer, Cancelable cancelable) throws IOException {
        InputStream is  = null;
        OutputStream os = null;
        try {
            // Opens the InputStream and OutputStream.
            is = file.getInputStream(entry);
            os = new FileOutputStream(pathName);

            final long crcValue = entry.getCrc();
            if (crcValue <= 0) {
                // Uncompress the ZIP entry.
                FileUtils.copyStream(is, os, cancelable, buffer);
            } else {
                // Uncompress the ZIP entry with check CRC32.
                uncompress(is, os, entry, crc, crcValue, buffer, cancelable);
            }
        } finally {
            FileUtils.close(is);
            FileUtils.close(os);
        }
    }

    private static void uncompress(InputStream is, OutputStream os, ZipEntry entry, CRC32 crc, long crcValue, byte[] buffer, Cancelable cancelable) throws IOException {
        // Uncompress the ZIP entry with check CRC32.
        cancelable = DummyCancelable.wrap(cancelable);
        crc.reset();

        for (int readBytes; (readBytes = is.read(buffer, 0, buffer.length)) > 0 && !cancelable.isCancelled(); ) {
            os.write(buffer, 0, readBytes);
            crc.update(buffer, 0, readBytes);
        }

        // Checks the uncompressed content CRC32.
        final long checksum = crc.getValue();
        if (crcValue != checksum) {
            throw new IOException(new StringBuilder("Checked the '").append(entry.getName()).append("' CRC32 failed [ Entry CRC32 = ").append(crcValue).append(", Computed CRC32 = ").append(checksum).append(" ]").toString());
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ZipUtils() {
    }
}
