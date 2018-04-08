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
import android.util.Log;

/**
 * Class ZipUtils
 * @author Garfield
 * @version 1.5
 */
public final class ZipUtils {
    /**
     * Compresses the data from the {@link InputStream}.
     * @param is The <tt>InputStream</tt> to read data.
     * @return The {@link ByteArrayBuffer} to store compressed
     * data if the operation succeeded, <tt>null</tt> otherwise.
     * @see #compress(InputStream, OutputStream)
     * @see #compress(byte[], int, int)
     * @see #compress(byte[], int, int, OutputStream)
     */
    public static ByteArrayBuffer compress(InputStream is) {
        try {
            final ByteArrayBuffer result = new ByteArrayBuffer();
            compress(is, result);
            return result;
        } catch (Exception e) {
            Log.e(ZipUtils.class.getName(), "Couldn't compress data.", e);
            return null;
        }
    }

    /**
     * Compresses the specified byte array <em>data</em>.
     * @param data The byte array to compress.
     * @param offset The start position in the <em>data</em>.
     * @param count The number of bytes from <em>data</em> to compress.
     * @return The {@link ByteArrayBuffer} to store compressed data
     * if the operation succeeded, <tt>null</tt> otherwise.
     * @see #compress(InputStream)
     * @see #compress(InputStream, OutputStream)
     * @see #compress(byte[], int, int, OutputStream)
     */
    public static ByteArrayBuffer compress(byte[] data, int offset, int count) {
        try {
            final ByteArrayBuffer result = new ByteArrayBuffer();
            compress(data, offset, count, result);
            return result;
        } catch (Exception e) {
            Log.e(ZipUtils.class.getName(), "Couldn't compress data.", e);
            return null;
        }
    }

    /**
     * Compresses the data from the {@link InputStream} to {@link OutputStream}.
     * @param is The <tt>InputStream</tt> to read data.
     * @param out The <tt>OutputStream</tt> to write the compressed data.
     * @throws IOException if an error occurs while compressing data.
     * @see #compress(InputStream)
     * @see #compress(byte[], int, int)
     * @see #compress(byte[], int, int, OutputStream)
     */
    public static void compress(InputStream is, OutputStream out) throws IOException {
        final GZIPOutputStream gzip = new GZIPOutputStream(out);
        try {
            FileUtils.copyStream(is, gzip, null);
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
     * @see #compress(byte[], int, int)
     * @see #compress(InputStream)
     * @see #compress(InputStream, OutputStream)
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
     * Compresses the specified <em>files</em> to ZIP file.
     * @param zipFile The ZIP filename.
     * @param compressionLevel The compression level to be used for writing entry data.
     * See {@link ZipOutputStream#setLevel(int)}.
     * @param files An array of filenames to compress, must be absolute file path.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     * @see #compress(String, int, List)
     */
    public static boolean compress(String zipFile, int compressionLevel, String... files) {
        return compress(zipFile, compressionLevel, Arrays.asList(files));
    }

    /**
     * Compresses the specified <em>files</em> to ZIP file.
     * @param zipFile The ZIP filename.
     * @param compressionLevel The compression level to be used for writing entry data.
     * See {@link ZipOutputStream#setLevel(int)}.
     * @param files A <tt>List</tt> of filenames to compress, must be absolute file path.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     * @see #compress(String, int, String[])
     */
    public static boolean compress(String zipFile, int compressionLevel, List<String> files) {
        if (ArrayUtils.getSize(files) == 0) {
            Log.e(ZipUtils.class.getName(), "Invalid parameter - The files is null or 0-length");
            return false;
        }

        OutputStream os = null;
        try {
            // Creates the necessary directories.
            FileUtils.mkdirs(zipFile, FileUtils.FLAG_IGNORE_FILENAME);
            os = new FileOutputStream(zipFile);
            return compress(os, compressionLevel, files);
        } catch (Exception e) {
            Log.e(ZipUtils.class.getName(), new StringBuilder("Couldn't compress file - ").append(zipFile).toString(), e);
            FileUtils.deleteFiles(zipFile, false);
            return false;
        } finally {
            FileUtils.close(os);
        }
    }

    /**
     * Compresses the specified <em>files</em> into <tt>OutputStream</tt> <em>out</em>.
     * @param out The <tt>OutputStream</tt> to store.
     * @param compressionLevel The compression level to be used for writing entry data.
     * See {@link ZipOutputStream#setLevel(int)}.
     * @param files An array of filenames to compress, must be absolute file path.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     * @see #compress(OutputStream, int, List)
     */
    public static boolean compress(OutputStream out, int compressionLevel, String... files) {
        return compress(out, compressionLevel, Arrays.asList(files));
    }

    /**
     * Compresses the specified <em>files</em> into <tt>OutputStream</tt> <em>out</em>.
     * @param out The <tt>OutputStream</tt> to store.
     * @param compressionLevel The compression level to be used for writing entry data.
     * See {@link ZipOutputStream#setLevel(int)}.
     * @param files A <tt>List</tt> of filenames to compress, must be absolute file path.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     * @see #compress(OutputStream, int, String[])
     */
    public static boolean compress(OutputStream out, int compressionLevel, List<String> files) {
        final int size = ArrayUtils.getSize(files);
        if (size == 0) {
            Log.e(ZipUtils.class.getName(), "Invalid parameter - The files is null or 0-length");
            return false;
        }

        ZipOutputStream os = null;
        try {
            // Creates the ZipOutputStream.
            os = new ZipOutputStream(out);
            os.setLevel(compressionLevel);

            // Compresses the files.
            final Dirent dirent = new Dirent();
            final byte[] buffer = new byte[8192];
            for (int i = 0; i < size; ++i) {
                dirent.setPath(files.get(i));
                compress(os, dirent, dirent.getName(), buffer);
            }
            return true;
        } catch (Exception e) {
            Log.e(ZipUtils.class.getName(), new StringBuilder("Couldn't compress file - ").append(out).toString(), e);
            return false;
        } finally {
            FileUtils.close(os);
        }
    }

    /**
     * Uncompresses the GZIP data from the {@link InputStream}.
     * @param is The <tt>InputStream</tt> to read data.
     * @return The {@link ByteArrayBuffer} to store uncompressed
     * data if the operation succeeded, <tt>null</tt> otherwise.
     * @see #uncompress(InputStream, OutputStream)
     */
    public static ByteArrayBuffer uncompress(InputStream is) {
        try {
            final ByteArrayBuffer result = new ByteArrayBuffer();
            uncompress(is, result);
            return result;
        } catch (Exception e) {
            Log.e(ZipUtils.class.getName(), "Couldn't uncompress data.", e);
            return null;
        }
    }

    /**
     * Uncompresses the GZIP data from the {@link InputStream} to {@link OutputStream}.
     * @param is The <tt>InputStream</tt> to read data.
     * @param out The <tt>OutputStream</tt> to write the uncompressed data.
     * @throws IOException if an error occurs while compressing data.
     * @see #uncompress(InputStream)
     */
    public static void uncompress(InputStream is, OutputStream out) throws IOException {
        final GZIPInputStream gzip = new GZIPInputStream(is);
        try {
            FileUtils.copyStream(gzip, out, null);
        } finally {
            gzip.close();
        }
    }

    /**
     * Uncompresses the ZIP file. <p>Note that this method creates the necessary
     * directories.</p>
     * @param filename The ZIP filename to uncompress.
     * @param outPath The uncompressed path, must be absolute path.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static boolean uncompress(String filename, String outPath) {
        ZipFile file = null;
        try {
            // Creates the necessary directories.
            FileUtils.mkdirs(outPath, 0);
            file = new ZipFile(filename);

            // Creates the CRC32.
            final CRC32 crc  = new CRC32();
            final byte[] buf = new byte[8192];

            // Enumerates the ZIP file entries.
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry  = entries.nextElement();
                final String pathName = FileUtils.buildPath(outPath, entry.getName());

                if (entry.isDirectory()) {
                    // Creates the sub directory.
                    FileUtils.mkdirs(pathName, 0);
                } else {
                    uncompress(file, entry, pathName, buf, crc);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(ZipUtils.class.getName(), new StringBuilder("Couldn't uncompress ZIP file - ").append(filename).toString(), e);
            return false;
        } finally {
            FileUtils.close(file);
        }
    }

    private static void compress(ZipOutputStream os, Dirent dirent, String name, byte[] buffer) throws IOException {
        if (dirent.isDirectory()) {
            // Adds the directory ZipEntry.
            name += '/';
            os.putNextEntry(new ZipEntry(name));

            // lists the sub files from path.
            final List<Dirent> dirents = dirent.listFiles();
            for (int i = 0, size = dirents.size(); i < size; ++i) {
                final Dirent child = dirents.get(i);
                compress(os, child, name + child.getName(), buffer);
            }
        } else {
            // Adds the file ZipEntry.
            os.putNextEntry(new ZipEntry(name));

            // Reads the file's contents to ZipOutputStream.
            final InputStream is = new FileInputStream(dirent.path);
            try {
                FileUtils.copyStream(is, os, buffer);
            } finally {
                is.close();
            }
        }
    }

    private static void uncompress(ZipFile file, ZipEntry entry, String pathName, byte[] buf, CRC32 crc) throws IOException {
        InputStream is  = null;
        OutputStream os = null;
        try {
            // Opens the InputStream and OutputStream.
            is = file.getInputStream(entry);
            os = new FileOutputStream(pathName);

            final long crcValue = entry.getCrc();
            if (crcValue <= 0) {
                // Uncompress the ZIP entry.
                FileUtils.copyStream(is, os, buf);
            } else {
                // Uncompress the ZIP entry with check CRC32.
                crc.reset();
                for (int readBytes = 0; (readBytes = is.read(buf, 0, buf.length)) > 0; ) {
                    os.write(buf, 0, readBytes);
                    crc.update(buf, 0, readBytes);
                }

                // Checks the uncompressed content CRC32.
                final long computedValue = crc.getValue();
                if (crcValue != computedValue) {
                    throw new IOException(new StringBuilder("Checked the '").append(entry.getName()).append("' CRC32 failed [ Entry CRC32 = ").append(crcValue).append(", Computed CRC32 = ").append(computedValue).append(" ]").toString());
                }
            }
        } finally {
            FileUtils.close(is);
            FileUtils.close(os);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ZipUtils() {
    }
}
