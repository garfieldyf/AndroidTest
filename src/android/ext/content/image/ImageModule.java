package android.ext.content.image;

import java.util.concurrent.Executor;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.ext.cache.BitmapPool;
import android.ext.cache.Cache;
import android.ext.cache.Caches;
import android.ext.cache.FileCache;
import android.ext.cache.LruBitmapCache2;
import android.ext.cache.LruFileCache;
import android.ext.cache.LruImageCache;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.XmlResources;
import android.ext.content.image.BitmapDecoder.Parameters;
import android.ext.graphics.GIFImage;
import android.ext.util.ClassFactory;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.util.Printer;

/**
 * Class ImageModule
 * @author Garfield
 * @version 2.0
 */
public class ImageModule<URI, Image> implements ComponentCallbacks2 {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;
    public final Executor mExecutor;

    protected final FileCache mFileCache;
    protected final Cache<URI, Image> mImageCache;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to executing load task.
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the loaded images.
     * @param fileCache May be <tt>null</tt>. The {@link FileCache} to store the loaded image files.
     */
    public ImageModule(Context context, Executor executor, Cache<URI, Image> imageCache, FileCache fileCache) {
        mContext  = context.getApplicationContext();
        mExecutor = executor;
        mFileCache  = fileCache;
        mImageCache = imageCache;
        mContext.registerComponentCallbacks(this);
    }

    /**
     * Creates a new {@link Bitmap} module.
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to execute load task.
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this application maximum
     * memory of the current device. Pass <tt>0</tt> that the module has no memory cache.
     * @param maxFileSize The maximum number of files in the file cache. Pass <tt>0</tt> that the module has no file cache.
     * @return The {@link ImageModule}.
     * @see #createBitmapModule(Context, Executor, float, int, int)
     */
    public static <URI> ImageModule<URI, Bitmap> createBitmapModule(Context context, Executor executor, float scaleMemory, int maxFileSize) {
        return new ImageModule<URI, Bitmap>(context, executor, Caches.<URI>createBitmapCache(scaleMemory, 0), createFileCache(context, maxFileSize));
    }

    /**
     * Creates a new {@link Bitmap} module.
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to execute load task.
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this application maximum
     * memory of the current device. Pass <tt>0</tt> that the module has no memory cache.
     * @param maxFileSize The maximum number of files in the file cache. Pass <tt>0</tt> that the module has no file cache.
     * @param maxBitmapSize The maximum number of bitmaps in the bitmap pool.
     * @return The {@link ImageModule}.
     * @see #createBitmapModule(Context, Executor, float, int)
     */
    public static <URI> ImageModule<URI, Bitmap> createBitmapModule(Context context, Executor executor, float scaleMemory, int maxFileSize, int maxBitmapSize) {
        return new ImageModule<URI, Bitmap>(context, executor, Caches.<URI>createBitmapCache(scaleMemory, maxBitmapSize), createFileCache(context, maxFileSize));
    }

    /**
     * Creates a new image module.
     * @param context The <tt>Context</tt>.
     * @param executor The {@link Executor} to execute load task.
     * @param scaleMemory The scale of memory of the bitmap cache, expressed as a percentage of this application maximum memory
     * of the current device. Pass <tt>0</tt> that the module has no bitmap cache.
     * @param maxImageSize The maximum number of images in the cache. Pass <tt>0</tt> that the module has no image cache.
     * @param maxFileSize The maximum number of files in the file cache. Pass <tt>0</tt> that the module has no file cache.
     * @return The {@link ImageModule}.
     */
    public static <URI> ImageModule<URI, Object> createImageModule(Context context, Executor executor, float scaleMemory, int maxImageSize, int maxFileSize) {
        return new ImageModule<URI, Object>(context, executor, new LruImageCache<URI, GIFImage>(scaleMemory, maxImageSize, 0), createFileCache(context, maxFileSize));
    }

    /**
     * Returns the {@link FileCache} associated with this object.
     * @return The <tt>FileCache</tt> or <tt>null</tt>.
     */
    public final FileCache getFileCache() {
        return mFileCache;
    }

    /**
     * Returns the image cache associated with this object.
     * @return The {@link Cache} or <tt>null</tt>.
     */
    public final Cache<URI, Image> getImageCache() {
        return mImageCache;
    }

    /**
     * Returns a {@link Builder} to creates an {@link ImageLoader}.
     * @return The <tt>Builder</tt>.
     */
    public final Builder<URI, Image> createImageLoader() {
        return new Builder<URI, Image>(this);
    }

    public final void dump(Printer printer) {
        Caches.dumpCache(mImageCache, mContext, printer);
        Caches.dumpCache(mFileCache, mContext, printer);
    }

    @Override
    public void onLowMemory() {
        if (mImageCache != null) {
            mImageCache.clear();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        onLowMemory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    /**
     * Creates a new {@link FileCache} instance.
     */
    private static FileCache createFileCache(Context context, int maxSize) {
        return (maxSize > 0 ? new LruFileCache(context, ".image_cache", maxSize) : null);
    }

    /**
     * Class <tt>Builder</tt> to creates an {@link ImageLoader}.
     * <h2>Usage</h2>
     * <p>Here is an example:</p><pre>
     * final ImageLoader&lt;String, Bitmap&gt; loader = mModule.createImageLoader()
     *     .setParameters(R.xml.decode_params)
     *     .setBinder(R.xml.image_binder)
     *     .create();</pre>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final class Builder<URI, Image> {
        private static final int FLAG_NO_FILE_CACHE   = 0x01;
        private static final int FLAG_NO_MEMORY_CACHE = 0x02;

        private int mFlags;
        private Class mClass;
        private Object mBinder;
        private Object mDecoder;
        private Object mParameters;
        private FileCache mFileCache;
        private final ImageModule mModule;

        /**
         * Constructor
         * @param The {@link ImageModule}.
         */
        /* package */ Builder(ImageModule<URI, Image> module) {
            mModule = module;
        }

        /**
         * Sets the image loader has no file cache.
         * @return This builder.
         * @see #noMemoryCache()
         */
        public final Builder<URI, Image> noFileCache() {
            mFlags |= FLAG_NO_FILE_CACHE;
            return this;
        }

        /**
         * Sets the image loader has no memory cache.
         * @return This builder.
         * @see #noFileCache()
         */
        public final Builder<URI, Image> noMemoryCache() {
            mFlags |= FLAG_NO_MEMORY_CACHE;
            return this;
        }

        /**
         * Sets the {@link FileCache} used to store the image files.
         * @param cache The <tt>FileCache</tt>.
         * @return This builder.
         */
        public final Builder<URI, Image> setFileCache(FileCache cache) {
            mFileCache = cache;
            return this;
        }

        /**
         * Sets the {@link Parameters} used to decode the image.
         * @param id The xml resource id of the <tt>Parameters</tt>.
         * @return This builder.
         * @see #setParameters(Parameters)
         */
        public final Builder<URI, Image> setParameters(int id) {
            mParameters = id;
            return this;
        }

        /**
         * Sets the {@link Parameters} used to decode the image.
         * @param parameters The <tt>Parameters</tt>.
         * @return This builder.
         * @see #setParameters(int)
         */
        public final Builder<URI, Image> setParameters(Parameters parameters) {
            mParameters = parameters;
            return this;
        }

        /**
         * Sets the {@link Binder} used to binder the image to target.
         * @param id The xml resource id of the <tt>Binder</tt>.
         * @return This builder.
         * @see #setBinder(Binder)
         */
        public final Builder<URI, Image> setBinder(int id) {
            mBinder = id;
            return this;
        }

        /**
         * Sets the {@link Binder} used to binder the image to target.
         * @param binder The <tt>Binder</tt>.
         * @return This builder.
         * @see #setBinder(int)
         */
        public final Builder<URI, Image> setBinder(Binder<URI, Object, Image> binder) {
            mBinder = binder;
            return this;
        }

        /**
         * Sets the <tt>ImageDecoder</tt> to decode the image data.
         * @param decoder The <tt>ImageDecoder</tt>.
         * @return This builder.
         * @see #setImageDecoder(Class)
         */
        public final Builder<URI, Image> setImageDecoder(ImageLoader.ImageDecoder<Image> decoder) {
            mDecoder = decoder;
            return this;
        }

        /**
         * Sets the <tt>ImageDecoder</tt> to decode the image data.
         * @param clazz The <tt>ImageDecoder</tt> subclass.
         * @return This builder.
         * @see #setImageDecoder(ImageLoader.ImageDecoder)
         */
        public final Builder<URI, Image> setImageDecoder(Class<? extends ImageLoader.ImageDecoder<Image>> clazz) {
            mDecoder = clazz;
            return this;
        }

        /**
         * Sets the {@link ImageLoader} subclass to create.
         * @param clazz The <tt>ImageLoader</tt> subclass.
         * @return This builder.
         */
        public final Builder<URI, Image> setClass(Class<? extends ImageLoader<URI, Image>> clazz) {
            mClass = clazz;
            return this;
        }

        /**
         * Creates an {@link ImageLoader} with the arguments supplied to this builder.
         * @return The instance of <tt>ImageLoader</tt>.
         */
        public final ImageLoader<URI, Image> create() {
            // Retrieves the image cache and file cache from image module, may be null.
            DebugUtils.__checkWarning((mFlags & FLAG_NO_FILE_CACHE) != 0 && mFileCache != null, Builder.class.getName(), "The builder has no file cache, setFileCache will be ignore.");
            final Cache imageCache = ((mFlags & FLAG_NO_MEMORY_CACHE) == 0 ? mModule.mImageCache : null);
            final FileCache fileCache = ((mFlags & FLAG_NO_FILE_CACHE) == 0 ? (mFileCache != null ? mFileCache : mModule.mFileCache) : null);

            // Creates the binder.
            final Binder binder;
            if (mBinder instanceof Binder) {
                binder = (Binder)mBinder;
            } else if (mBinder instanceof Integer) {
                binder = XmlResources.loadBinder(mModule.mContext, (int)mBinder);
            } else {
                binder = null;
            }

            // Creates the image loader.
            final ImageLoader.ImageDecoder decoder = (ImageLoader.ImageDecoder)createImageDecoder(imageCache);
            if (mClass == null) {
                return new ImageLoader(mModule.mContext, mModule.mExecutor, imageCache, fileCache, decoder, binder);
            } else {
                return (ImageLoader)newInstance(mClass, new Class[] { Context.class, Executor.class, Cache.class, FileCache.class, ImageLoader.ImageDecoder.class, Binder.class }, mModule.mContext, mModule.mExecutor, imageCache, fileCache, decoder, binder);
            }
        }

        private static Object newInstance(Class clazz, Class[] parameterTypes, Object... args) {
            try {
                return ClassFactory.getConstructor(clazz, parameterTypes).newInstance(args);
            } catch (Throwable e) {
                throw new IllegalArgumentException("Couldn't create " + clazz.getName() + " instance", e);
            }
        }

        private Object createImageDecoder(Cache imageCache) {
            if (mDecoder instanceof ImageLoader.ImageDecoder) {
                return mDecoder;
            }

            final Parameters parameters;
            if (mParameters instanceof Parameters) {
                parameters = (Parameters)mParameters;
            } else if (mParameters instanceof Integer) {
                parameters = XmlResources.loadParameters(mModule.mContext, (int)mParameters);
            } else {
                parameters = Parameters.defaultParameters();
            }

            final int maxPoolSize = ImageLoader.computeMaximumPoolSize(mModule.mExecutor);
            if (mDecoder instanceof Class) {
                return createImageDecoder(mModule.mContext, imageCache, parameters, maxPoolSize, (Class)mDecoder);
            } else if (imageCache instanceof LruBitmapCache2) {
                return new CacheBitmapDecoder(mModule.mContext, parameters, maxPoolSize, ((LruBitmapCache2)imageCache).getBitmapPool());
            } else if (imageCache instanceof LruImageCache) {
                final BitmapPool bitmapPool = ((LruImageCache)imageCache).getBitmapPool();
                return (bitmapPool != null ? new CacheImageDecoder(mModule.mContext, parameters, maxPoolSize, bitmapPool) : new ImageDecoder(mModule.mContext, parameters, maxPoolSize));
            } else {
                return new BitmapDecoder(mModule.mContext, parameters, maxPoolSize);
            }
        }

        private static Object createImageDecoder(Context context, Cache imageCache, Parameters parameters, int maxPoolSize, Class clazz) {
            BitmapPool bitmapPool = null;
            if (imageCache instanceof LruBitmapCache2) {
                bitmapPool = ((LruBitmapCache2)imageCache).getBitmapPool();
            } else if (imageCache instanceof LruImageCache) {
                bitmapPool = ((LruImageCache)imageCache).getBitmapPool();
            }

            return (bitmapPool != null ? newInstance(clazz, new Class[] { Context.class, Parameters.class, int.class, BitmapPool.class }, context, parameters, maxPoolSize, bitmapPool) : newInstance(clazz, new Class[] { Context.class, Parameters.class, int.class }, context, parameters, maxPoolSize));
        }
    }
}
