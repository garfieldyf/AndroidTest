package android.ext.image;

import static java.net.HttpURLConnection.HTTP_OK;
import android.ext.cache.Cache;
import android.ext.cache.FileCache;
import android.ext.content.Task;
import android.ext.net.DownloadRequest;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import java.io.File;

/**
 * Class <tt>ImageLoader</tt> allows to load the image from the URI on a background
 * thread and bind it to target on the UI thread.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ ImageLoader | loader ]
 *      xmlns:android="http://schemas.android.com/apk/res/android"
 *      xmlns:app="http://schemas.android.com/apk/res-auto"
 *      class="classFullName"
 *      app:flags="[ none | noFileCache | noMemoryCache ]"&gt;
 *
 *      &lt;!-- Optional ImageDecoder --&gt;
 *      &lt;[ BitmapDecoder | ImageDecoder | ContactPhotoDecoder | decoder ]
 *          class="classFullName"
 *          app:attribute1="value1"
 *          app:attribute2="value2"
 *          ... ... /&gt;
 * &lt;/[ ImageLoader | loader ]&gt;</pre>
 * @author Garfield
 */
public class ImageLoader<Image> extends AbsImageLoader<Image> {
    /**
     * The {@link Loader} to load image data.
     */
    private final Loader<Image> mLoader;

    /**
     * The {@link ImageDecoder} to decode image.
     */
    protected final ImageDecoder<Image> mDecoder;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded image.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     * @param decoder The {@link ImageDecoder} to decode the image data.
     */
    protected ImageLoader(ImageModule module, Cache<Object, Image> imageCache, FileCache fileCache, ImageDecoder<Image> decoder) {
        super(module, imageCache);

        mDecoder = decoder;
        mLoader  = (fileCache != null ? new FileCacheLoader(fileCache) : new URLLoader());
    }

    @Override
    public Image remove(Object uri) {
        mLoader.remove(uri);
        return super.remove(uri);
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Image value, int state) {
        final ImageView view = (ImageView)target;
        if (value != null) {
            view.setImageBitmap((Bitmap)value);
        } else if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            ImageModule.setPlaceholder(view, params);
        }
    }

    @Override
    protected Image loadInBackground(Task task, Object uri, Object[] params, int flags) {
        final byte[] buffer = mModule.mBufferPool.obtain();
        try {
            final Object target = getTarget(task);
            final String uriString = uri.toString();
            return (matchScheme(uriString) ? mLoader.load(task, uriString, target, params, flags, buffer) : mDecoder.decodeImage(uri, target, params, flags, buffer));
        } finally {
            mModule.mBufferPool.recycle(buffer);
        }
    }

    @Override
    protected void onShutdown() {
        mDecoder.releaseResources();
    }

    /**
     * Matches the scheme of the specified <em>uri</em>. The default
     * implementation match the scheme "http", "https" and "ftp".
     */
    protected boolean matchScheme(String uri) {
        return ("http://".regionMatches(true, 0, uri, 0, 7) || "https://".regionMatches(true, 0, uri, 0, 8) || "ftp://".regionMatches(true, 0, uri, 0, 6));
    }

    /**
     * Called on a background thread to load an image from the specified <em>url</em>.
     * @param task The current {@link Task} whose executing this method.
     * @param url The url to load.
     * @param imageFile The image file to store the image data.
     * @param target The <tt>Object</tt> to bind, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param flags Loading flags, passed earlier by {@link #load}.
     * @param buffer The temporary byte array to used for loading image data.
     * @return The image, or <tt>null</tt> if the load failed or cancelled.
     */
    protected Image loadImage(Task task, String url, File imageFile, Object target, Object[] params, int flags, byte[] buffer) {
        try {
            final DownloadRequest request = new DownloadRequest(url).connectTimeout(30000).readTimeout(30000);
            request.__checkDumpHeaders = false;
            return (request.download(imageFile, task, buffer) == HTTP_OK && !isTaskCancelled(task) ? mDecoder.decodeImage(imageFile, target, params, flags, buffer) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't load image data from - " + url + "\n" + e);
            return null;
        }
    }

    /**
     * Interface <tt>Loader</tt> used to load image from the specified url.
     */
    private static interface Loader<Image> {
        /**
         * Removes the cache file for the specified
         * <em>uri</em> from the cache of this loader.
         * @param uri The uri to remove.
         */
        default void remove(Object uri) {
        }

        /**
         * Called on a background thread to load an image from the specified <em>url</em>.
         * @param task The current {@link Task} whose executing this method.
         * @param url The url to load.
         * @param target The <tt>Object</tt> to bind, passed earlier by {@link ImageLoader#load}.
         * @param params The parameters, passed earlier by {@link ImageLoader#load}.
         * @param flags Loading flags, passed earlier by {@link ImageLoader#load}.
         * @param buffer The temporary byte array to use for loading image data.
         * @return The image, or <tt>null</tt> if the load failed or cancelled.
         */
        Image load(Task task, String url, Object target, Object[] params, int flags, byte[] buffer);
    }

    /**
     * Class <tt>URLLoader</tt> is an implementation of a {@link Loader}.
     */
    /* package */ final class URLLoader implements Loader<Image> {
        @Override
        public Image load(Task task, String url, Object target, Object[] params, int flags, byte[] buffer) {
            final File imageFile = new File(mModule.mCacheDir, Integer.toString(Thread.currentThread().hashCode()));
            try {
                return loadImage(task, url, imageFile, target, params, flags, buffer);
            } finally {
                imageFile.delete();
            }
        }
    }

    /**
     * Class <tt>FileCacheLoader</tt> is an implementation of a {@link Loader}.
     */
    private final class FileCacheLoader implements Loader<Image> {
        private final FileCache mCache;

        /**
         * Constructor
         * @param cache The {@link FileCache} to store the loaded image files.
         */
        public FileCacheLoader(FileCache cache) {
            mCache = cache;
        }

        @Override
        public void remove(Object uri) {
            final String uriString = uri.toString();
            if (matchScheme(uriString)) {
                DebugUtils.__checkStartMethodTracing();
                mCache.remove(StringUtils.toHexString(MessageDigests.computeString(uriString, Algorithm.SHA1)));
                DebugUtils.__checkStopMethodTracing("ImageLoader", "FileCacheLoader.remove");
            }
        }

        @Override
        public Image load(Task task, String url, Object target, Object[] params, int flags, byte[] buffer) {
            final String hashKey = StringUtils.toHexString(buffer, 0, MessageDigests.computeString(url, buffer, 0, Algorithm.SHA1));
            final File imageFile = mCache.get(hashKey);
            Image result = null;

            if (imageFile.exists()) {
                // Decodes the image file, If exists.
                if ((result = mDecoder.decodeImage(imageFile, target, params, flags, buffer)) != null) {
                    return result;
                }

                // Removes the image file from file cache, If decode failed.
                mCache.remove(hashKey);
            }

            // Loads the image from url, If the image file is not exists or decode failed.
            final File tempFile = new File(mModule.mCacheDir, Integer.toString(Thread.currentThread().hashCode()));
            if ((result = loadImage(task, url, tempFile, target, params, flags, buffer)) != null && FileUtils.moveFile(tempFile.getPath(), imageFile.getPath()) == 0) {
                // Saves the image file to file cache, If load succeeded.
                mCache.put(hashKey, imageFile);
            } else {
                // Deletes the temp file, If load failed.
                tempFile.delete();
                DebugUtils.__checkWarning(result == null, "ImageLoader", "loadImage failed - delete tempFile = " + tempFile + ", url = " + url);
            }

            return result;
        }
    }

    /**
     * The <tt>ImageDecoder</tt> class used to decode the image data.
     */
    public static interface ImageDecoder<Image> {
        /**
         * Called when the {@link ImageLoader} has been shut down, for
         * subclasses to release any other resources associated with it.
         */
        default void releaseResources() {
        }

        /**
         * Decodes an image from the specified <em>uri</em>.
         * @param uri The uri to decode.
         * @param target The target, passed earlier by {@link ImageLoader#load}.
         * @param params The parameters, passed earlier by {@link ImageLoader#load}.
         * @param flags The flags, passed earlier by {@link ImageLoader#load}.
         * @param tempStorage The temporary storage to use for decoding. Suggest 16K.
         * @return The image, or <tt>null</tt> if the image data cannot be decode.
         */
        Image decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage);
    }
}
